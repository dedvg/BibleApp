package com.example.david.bibleapp;

/*
This Activity allows the user to download a book they do not own yet.
The user is able to go back to UserActivity with use of the back button provided,
is able to switch translation and if needed logout.
*/

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class TranslationActivity extends AppCompatActivity {

    TextView translation_txt;
    Button translation_btn;
    Toolbar toolbar;
    TranslationDatabase sql_database;
    JSONArray books_json;
    Integer chapters, translation, given_book_int, load_chapter = 0;
    String add_factor, given_book, book_url;
    ProgressDialog progress_dialog;
    ArrayList <ChapterClass> bible_book = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);

        // get the variables needed from the intent
        Intent intent = getIntent();
        given_book = intent.getStringExtra("book");
        given_book_int = intent.getIntExtra("book_int", 0);
        translation = intent.getIntExtra("translation", 0);
        String json_array = intent.getStringExtra("json_array");

        //  make books_json to enable getting the right values again
        // and make the given book to a good url
        try {
            books_json = new JSONArray(json_array);
            book_url = URLEncoder.encode(given_book, "utf-8");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // creating references
        translation_btn = findViewById(R.id.transBTN);
        translation_txt = findViewById(R.id.transTXT);
        toolbar = findViewById(R.id.toolbar);
        sql_database = TranslationDatabase.getInstance(this.getApplicationContext());

        // create the toolbar
        setSupportActionBar(toolbar);

        // add backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // sets the layout ready for use
        showDownload();

        // set an onclicklistener on the navigationbackbutton
        toolbar.setNavigationOnClickListener(new navigationBackClicked());
    }

    /*
    enables custom menu
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
    handles which menu item is clicked and what to do
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logoutDialog();
                break;
            case R.id.favorites:
                goToFavorites();
                break;
            case R.id.switch_translation:
                switchTranslationDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    will go to the favoritesActivity
    */
    private void goToFavorites() {
        Intent intent = new Intent(this, FavoriteActivity.class);
        intent.putExtra("translation", translation);

        // starts the new activity
        startActivity(intent);
        finish();
    }

    /*
    handle the actual logout
    */
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        // starts the new activity
        startActivity(intent);
        finish();
    }

    /*
    will go back to userActivity
    */
    private void goBack() {
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("translation", translation);

        // starts the new activity
        startActivity(intent);
        finish();
    }

    /*
    will handle the onclick of the download button
    changes some parameters depending on the translation used
    */
    public void download (View view){
        if (translation == 0){
            add_factor = "";
        }
        else if (translation == 1){
            add_factor = "?translation=kjv";
            translation = 1;
        }

        // start preperations for the volley
        try {
            volleyPreperations();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
    will create a dialog which enables the user to swithc translation
    */
    public void switchTranslationDialog(){
        String translation_txt = "WEB";
        if (translation == 0){
            translation_txt ="KJV";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(TranslationActivity.this);
        builder.setMessage("do you want to switch to " + translation_txt)
                .setNeutralButton("no", switchTranslationListener)
                .setPositiveButton("yes", switchTranslationListener).show();
    }

    /*
    actually switches the translation
    */
    private void switchTranslation() {
        if( translation == 0){
            translation = 1;
        }
        else {
            translation = 0;
        }

        // layout needs to be adapted
        showDownload();
    }

    /*
    shows the layout to enabling a download of a book
    */
    public void showDownload(){
        if (translation == 1) {
            getSupportActionBar().setTitle("Current translation = KJV");
        } else {
            getSupportActionBar().setTitle("Current translation = WEB");
        }
        translation_txt.setVisibility(View.VISIBLE);
        setLayout();
    }

    /*
    will set the layout depending on which translations are present
    if the translation is already downloaded do not show a download button
    else show the download button and adapt the text accordingly
    */
    private void setLayout() {

        if (sql_database.checkChapter1Existence(given_book, translation)){
            translation_btn.setVisibility(View.INVISIBLE);
            if (translation == 0){
                translation_txt.setText(" WEB version of " + given_book + " is already downloaded");
            }
            else {
                translation_txt.setText(" KJV version of " + given_book + " is already downloaded");
            }
        }
        else{
            translation_btn.setVisibility(View.VISIBLE);
            if (translation == 0){
                translation_txt.setText(given_book + "(WEB)");
            }
            else {
                translation_txt.setText(given_book + "(KJV)");
            }
        }

        translation_txt.setVisibility(View.VISIBLE);

    }

    /*
    asks the user if the user really wants to logout, if yes logout
    */
    public void logoutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(TranslationActivity.this);
        builder.setMessage("Do you want to log out?")
                .setPositiveButton("Yes", logoutListener)
                .setNegativeButton("No", logoutListener).show();
    }

    /*
    preperation of the volley which will get the biblebook
    */
    public void volleyPreperations() throws JSONException {
        progress_dialog = new ProgressDialog(this);
        progress_dialog.setMessage("loading");
        progress_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress_dialog.setIndeterminate(false);

        // get the amount of chapters and set this as the max of the progressbar
        chapters = books_json.getJSONObject(given_book_int).getInt("val");
        progress_dialog.setMax(chapters);
        progress_dialog.show();

        // start volleying the whole book
        volleyChapterByChapter();
    }

    /*
    function that will volley all chapters of the book one by one
    */
    public void volleyChapterByChapter( ) throws JSONException {
        for (int i = 0; i < chapters; i++) {
            int chapter = i + 1;
            volleyChapter(chapter);
        }
    }

    /*
    will volley the chapter of the book and save it in json_array
    each volley sets the progressbar one further
    each chapter is stored with use of the function setChapterInDatabase
    */
    public void volleyChapter( final int chapter) {
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String mJSONURLString = "https://bible-api.com/" + book_url + "%20" + chapter + add_factor;

        // Initialize a new JsonObjectRequest instance
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, mJSONURLString, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            // set the current progress
                            load_chapter += 1;
                            progress_dialog.setProgress(load_chapter);

                            // saves the chapter in a JSONArray and sets it in the database
                            JSONArray json_array = response.getJSONArray("verses");
                            setChapterInDatabase(json_array, chapter);
                        } catch (JSONException ignored) {}
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        // prompt the user to start again
                        Toast.makeText(TranslationActivity.this,
                                "No connection please restart the app with internet acces",
                                Toast.LENGTH_SHORT).show();
                        goBack();
                    }
                }
        );

        // Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    /*
    will set the textresult in the database verse by verse
    chapter needs to be passed on because chapter 1 does not need to get here first
    */
    public void setChapterInDatabase(JSONArray json_array, int chapter)throws JSONException{

        // make a list to store all verses
        ArrayList<String> verses = new ArrayList<>();

        // set all verses in the list
        String text;
        for (int i = 0; i < json_array.length(); i++) {
            text =  json_array.getJSONObject(i).getString("text");
            verses.add(text);
        }

        // saves the chapter and adds it to the bible book
        ChapterClass chapter_text = new ChapterClass(chapter, verses);
        bible_book.add(chapter_text);

        // if all chapters are downloaded put it in the database
        if (load_chapter == chapters){
            setBookInDatabase(bible_book);
        }
    }

    /*
    will set the whole book at once in the SQL database
    */
    private void setBookInDatabase(ArrayList<ChapterClass> book) {
        Toast.makeText(TranslationActivity.this, "Finished downloading",
                       Toast.LENGTH_SHORT).show();

        // first loop over the chapters then over the verses and add each verse one by one in
        // the database
        for (int i = 0 ; i < book.size(); i ++) {
            for (int j = 0 ; j < book.get(i).verses.size(); j ++) {
                sql_database.addItem(given_book, book.get(i).chapter,j + 1,
                                     book.get(i).verses.get(j), translation );
            }
        }

        // dismiss the progressbar and go back to userActivity to enable reading again
        progress_dialog.dismiss();
        goBack();
    }



    /*
    if the navigation backButton is pressed go one step back
    */
    private class navigationBackClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            goBack();
        }
    }

    /*
    the click listener for the beforeLogout function
    if clicked yes the user will logout
    */
    DialogInterface.OnClickListener logoutListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int choice) {
            switch (choice) {
                case DialogInterface.BUTTON_POSITIVE:
                    logout();
                    break;
            }
        }
    };

    /*
    dialogonclicklisteer to switch translation
    */
    DialogInterface.OnClickListener switchTranslationListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int choice) {
            switch (choice) {
                case DialogInterface.BUTTON_POSITIVE:
                    switchTranslation();
                    break;
            }
        }
    };
}

