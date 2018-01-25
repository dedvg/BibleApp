package com.example.david.bibleapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TranslationActivity extends AppCompatActivity {

    TextView title_txt, translation_txt;
    Button translation_btn;
    List<String> ListText = new ArrayList<String>();
    Toolbar toolbar;
    TranslationDatabase theDatabase;
    JSONArray BOOKSjson;
    Integer chapters, translation, given_book_int, clicked_book, load_chapter = 0;
    String add_factor, given_book ,book;
    ProgressBar spinner;


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


        theDatabase = TranslationDatabase.getInstance(this.getApplicationContext());
        // add backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // layout function
        spinner.setVisibility(View.GONE);
        // when pressing back the user will be brought back to select between all books
        // or back to UserActivity
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    go_back();

            }
        });


        // get the variables needed from the intent
        Intent intent = getIntent();
        given_book = intent.getStringExtra("book");
        given_book_int = intent.getIntExtra("book_int", 0);
        translation = intent.getIntExtra("translation", 0);

        if (translation == 1){
            getSupportActionBar().setTitle("Current translation = KJV");
        }
        else {
            getSupportActionBar().setTitle("Current translation = WEB");
        }

        show_download();

        String jsonArray = intent.getStringExtra("jsonArray");

        //  make BOOKSjson to enable getting the right values again
        try {
            BOOKSjson = new JSONArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        check_downloadreference(given_book_int, given_book);
    }

    /*
    checks if the intent gave a book to download, if so enable the user to download the selected book
     */
    private void check_downloadreference(Integer given_book_int, String given_book) {
        if (!Objects.equals(given_book, "")) {
            book = given_book;
            clicked_book = given_book_int;
            show_download();
        } else {
            go_back();
        }
    }
    /*
    will handle the onclick of the download button
     */
    public void download_btn (View view){
        if (translation == 0){
            add_factor = "";
            try {
                volley_translation_0_books();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if (translation == 1){
            add_factor = "?translation=kjv";
            translation = 1;
            try {
                volley_translation_0_books();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    will switch translation depending on the selected part
     */
    public void switch_translation(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choice) {
                switch (choice) {
                    case DialogInterface.BUTTON_POSITIVE:
                        String translation_txt;
                        if( translation == 0){
                            translation = 1;
                            translation_txt = "KJV";
                        }
                        else {
                            translation = 0;
                            translation_txt = "WEB";
                        }
                        getSupportActionBar().setTitle("Current translation = " + translation_txt);
                        show_download();
                        break;

                    case DialogInterface.BUTTON_NEUTRAL:
                        break;
                }
            }
        };

        String translation_txt;
        if (translation == 0){
            translation_txt ="KJV";
        }
        else {
            translation_txt = "WEB";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(TranslationActivity.this);
        builder.setMessage("do you want to switch to " + translation_txt)
                .setNeutralButton("no", dialogClickListener)
                .setPositiveButton("yes", dialogClickListener).show();
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
    public void show_download(){
            translation_txt.setVisibility(View.VISIBLE);
            title_txt.setVisibility(View.VISIBLE);
            set_text();
    }
    /*
    will set the layout depending on which translations are present

     */
    private void set_text() {
        title_txt.setText("KJV = King James Version and WEB = World English Bible");
        if (translation == 0 ){
            if (theDatabase.check_chapter1_existence_WEB(book)) {
                translation_txt.setText(" WEB version of " + book + " is already downloaded");
            }
            else{
                translation_txt.setText(book + "(WEB)");
            }
        }
        else if (translation == 1)
        {
            if (theDatabase.check_chapter1_existence_KJV(book)) {
                translation_txt.setText(" KJV version of " + book + " is already downloaded");
            }
            else{
                translation_txt.setText(book + "(KJV)");
            }
        }
        translation_txt.setVisibility(View.VISIBLE);
        translation_btn.setVisibility(View.VISIBLE);

    }

    /*
    handles which menu item is clicked and what to do
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                before_logout();
                break;
            case R.id.favorites:
                GoToFavorites();
                break;

            case R.id.switch_translation:
                switch_translation();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*

    asks the user if the user really wants to logout, if yes logout
     */
    public void before_logout(){
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
    private void go_back() {
        Intent intent = new Intent(this, UserActivity.class);
        // starts the new activity
        startActivity(intent);
        finish();
    }

    /*
    preperation of the volley which will get the biblebook
     */
    public void volley_translation_0_books() throws JSONException {
        spinner.setVisibility(View.VISIBLE);
        chapters = BOOKSjson.getJSONObject(clicked_book).getInt("val");
        volley_translation_1_chapters(book);
    }
    /*
    function that will volley all chapters of the book one by one
     */
    public void volley_translation_1_chapters(String book) throws JSONException {

        for (int i = 0; i < chapters; i++) {
            int chapter = i + 1;
            volley_translation_2_chapter(book, chapter);
        }
    }



    /*
    will volley the chapter given by volley_translatio_1_chapters
     */
    public void volley_translation_2_chapter(final String book, final int chapter) {

        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String mJSONURLString = "https://bible-api.com/" + book + "%20" + chapter + add_factor;

        // Initialize a new JsonObjectRequest instance
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, mJSONURLString, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray jsonArray = response.getJSONArray("verses");
                            volley_translation_3_db(book, chapter, jsonArray);
                        } catch (JSONException e) {
                            // if this shows something changed in the JSON
                            Toast.makeText(TranslationActivity.this,
                                    "problem with accesing the JSON",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // prompt the user to start again
                        Toast.makeText(TranslationActivity.this,
                                "No connection please restart the app with internet acces",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }
    /*
    will set the textresult in the database verse by verse
     */
    public void volley_translation_3_db(String Book, int chapter, JSONArray jsonArray)throws JSONException{
        load_chapter += 1;
        if (load_chapter == chapters){
            spinner.setVisibility(View.GONE);
            Toast.makeText(TranslationActivity.this, "Finished downloading", Toast.LENGTH_SHORT).show();
        }
            String text;
            for (int i = 0; i < jsonArray.length(); i++) {
                text =  jsonArray.getJSONObject(i).getString("text");
                    theDatabase.addItem(Book, chapter,i + 1,text, translation);
            }
    }


    /*
   will go to the favoritesActivity
   */
    private void GoToFavorites() {
        Intent intent = new Intent(this, FavoriteActivity.class);
        intent.putExtra("translation", translation);

        // starts the new activity
        startActivity(intent);
        finish();
    }
}

