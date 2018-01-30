package com.example.david.bibleapp;

import android.app.ProgressDialog;
import android.content.Context;
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

import android.widget.ProgressBar;
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
import java.util.Objects;

public class TranslationActivity extends AppCompatActivity {

    TextView title_txt, translation_txt;
    Button translation_btn;
    Toolbar toolbar;
    TranslationDatabase theDatabase;
    JSONArray BOOKSjson;
    Integer chapters, translation, given_book_int, load_chapter = 0;
    String add_factor, given_book, book_url;
    ProgressBar spinner;
    ProgressDialog progress_dialog;
    ArrayList <ChapterClass> bible_book = new ArrayList<ChapterClass>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);
        spinner = findViewById(R.id.progressBar1);

        // creating references
        translation_btn = findViewById(R.id.transBTN);
        title_txt = findViewById(R.id.titleTXT);
        translation_txt = findViewById(R.id.transTXT);
        toolbar = findViewById(R.id.toolbar);

        // create the toolbar
        setSupportActionBar(toolbar);

        // add backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        theDatabase = TranslationDatabase.getInstance(this.getApplicationContext());


        // layout function
        spinner.setVisibility(View.GONE);
        // when pressing back the user will be brought back to select between all books
        // or back to UserActivity
        toolbar.setNavigationOnClickListener(new navigationBackClicked());


        // get the variables needed from the intent
        Intent intent = getIntent();
        given_book = intent.getStringExtra("book");
        given_book_int = intent.getIntExtra("book_int", 0);
        translation = intent.getIntExtra("translation", 0);
        String jsonArray = intent.getStringExtra("jsonArray");

        //  make BOOKSjson to enable getting the right values again
        // and make the given book to a good url
        try {
            BOOKSjson = new JSONArray(jsonArray);
            book_url = URLEncoder.encode(given_book, "utf-8");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        showDownload();
    }


    /*
    will handle the onclick of the download button
     */
    public void download_btn (View view){
        if (translation == 0){
            add_factor = "";
            try {
                volleyTranslation0Books();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if (translation == 1){
            add_factor = "?translation=kjv";
            translation = 1;
            try {
                volleyTranslation0Books();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    dialogonclicklisteer to switch translation
     */
    DialogInterface.OnClickListener translationSwitchListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int choice) {
            switch (choice) {
                case DialogInterface.BUTTON_POSITIVE:
                    switchTranslation();
                    break;
                case DialogInterface.BUTTON_NEUTRAL:
                    break;
            }
        }
    };
    /*
    will switch translation depending on the selected part
     */
    public void beforeSwitchTranslation(){
        String translation_txt = "WEB";
        if (translation == 0){
            translation_txt ="KJV";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(TranslationActivity.this);
        builder.setMessage("do you want to switch to " + translation_txt)
                .setNeutralButton("no", translationSwitchListener)
                .setPositiveButton("yes", translationSwitchListener).show();
    }
    /*
    actually switches the translation
     */
    private void switchTranslation() {
        String translation_txt;
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
    enables custom menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
        title_txt.setVisibility(View.VISIBLE);
        setText();
    }
    /*
    will set the layout depending on which translations are present

    if the translation is already downloaded do not show a download button
    else show the download button and adapt the text accordingly
    todo test this function
     */
    private void setText() {

        if (theDatabase.checkChapter1Existence(given_book, translation)){
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
        title_txt.setText("KJV = King James Version and WEB = World English Bible");

        translation_txt.setVisibility(View.VISIBLE);

    }

    /*
    handles which menu item is clicked and what to do
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                beforeLogout();
                break;
            case R.id.favorites:
                goToFavorites();
                break;

            case R.id.switch_translation:
                beforeSwitchTranslation();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*

    asks the user if the user really wants to logout, if yes logout
     */
    public void beforeLogout(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choice) {
                switch (choice) {
                    case DialogInterface.BUTTON_POSITIVE:

                        logout();

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(TranslationActivity.this);
        builder.setMessage("Do you want to log out?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
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
    preperation of the volley which will get the biblebook
     */
    public void volleyTranslation0Books() throws JSONException {
        progress_dialog = new ProgressDialog(this);
        progress_dialog.setMessage("loading");
        progress_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress_dialog.setIndeterminate(false);
        chapters = BOOKSjson.getJSONObject(given_book_int).getInt("val");
        progress_dialog.setMax(chapters);
        progress_dialog.show();
        volleyTranslation1Chapters();
    }
    /*
    function that will volley all chapters of the book one by one
     */
    public void volleyTranslation1Chapters( ) throws JSONException {

        for (int i = 0; i < chapters; i++) {
            int chapter = i + 1;
            volleyTranslation2Chapter(chapter);
        }
    }



    /*
    TODO can crash while downloading and only having half of the book
    will volley the chapter given by volleyTranslation1
     */
    public void volleyTranslation2Chapter( final int chapter) {
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
                            load_chapter += 1;
                            progress_dialog.setProgress(load_chapter);

                            JSONArray jsonArray = response.getJSONArray("verses");
                            volleyTranslation3Database(jsonArray, chapter);
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
    public void volleyTranslation3Database(JSONArray jsonArray, int chapter)throws JSONException{

        ArrayList<String> verses = new ArrayList<>();

        String text;
        for (int i = 0; i < jsonArray.length(); i++) {
            text =  jsonArray.getJSONObject(i).getString("text");
            verses.add(text);
        }
        ChapterClass chapter_text = new ChapterClass(chapter, verses);
        bible_book.add(chapter_text);
        if (load_chapter == chapters){
            setInDatabase(bible_book);
        }
    }

    /*
    will set the whole class in the SQL database
     */
    private void setInDatabase(ArrayList<ChapterClass> book) {
        Integer size_book = book.size() + 1;
        Toast.makeText(TranslationActivity.this, "Finished downloading" + size_book.toString(), Toast.LENGTH_SHORT).show();
        for (int i = 0 ; i < book.size(); i ++) {
            for (int j = 0 ; j < book.get(i).verses.size(); j ++) {
                theDatabase.addItem(given_book, book.get(i).chapter,j + 1, book.get(i).verses.get(j), translation );
            }
        }
        progress_dialog.dismiss();
        goBack();
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
    if the navigation backButton is pressed go one step back
     */
    private class navigationBackClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            goBack();
        }
    }
}

