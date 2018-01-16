package com.example.david.bibleapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class TranslationActivity extends AppCompatActivity {
    Toolbar toolbar;
    TranslationDatabase theDatabase;
    JSONArray BOOKSjson;
    Integer chapters, translation;
    String add_factor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Download Bible translations");
        toolbar.setSubtitle("test");
        theDatabase = TranslationDatabase.getInstance(this.getApplicationContext());

        // add backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go_back();
            }
        });
        try {
            load_booksJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
// TODO stuff needs to be ordered
        switch (item.getItemId()) {
            case R.id.logout:
                before_logout();
                break;
            case R.id.favorites:
                break;
            case R.id.get_table:

                break;
            case R.id.test_function:

                break;

            case R.id.switch_translation:
                break;
            case R.id.new_text:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void load_booksJSON() throws JSONException, IOException {
        // done with use of https://www.youtube.com/watch?v=h71Ia9iFWfI
        JSONObject jsonObject;
        InputStream is = getAssets().open("books.json");
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, "UTF-8");
        jsonObject = new JSONObject(json);
        BOOKSjson = jsonObject.getJSONObject("sections").getJSONArray("whole_bible");
        Toast.makeText(TranslationActivity.this, "booksjson made", Toast.LENGTH_SHORT).show();
    }

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
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        // starts the new activity
        startActivity(intent);
        finish();
    }

    private void go_back() {
        Intent intent = new Intent(this, UserActivity.class);
        // starts the new activity
        startActivity(intent);
        finish();
    }
    public void volley_translation_0_books() throws JSONException {
        for (int i = 0; i < 20; i++) {
            String book = null;
            try {
                book = BOOKSjson.getJSONObject(i).getString("key");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            volley_translatio_1_chapters(book, i);

        }

    }

    public void volley_translatio_1_chapters(String book, Integer book_number) throws JSONException {
//        for now only genesis
        chapters = BOOKSjson.getJSONObject(book_number).getInt("val");
        if (!theDatabase.check_chapter_existence_WEB(book, 1)) {
            for (int i = 0; i < chapters; i++) {
                int chapter = i + 1;
                volley_translation_2_chapter(book, chapter);
            }

        }
        else{
            Toast.makeText(TranslationActivity.this, book  + " already exist in Database", Toast.LENGTH_SHORT).show();
        }
    }

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

                            volley_translation_3_db(book, chapter, jsonArray, 1);

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

    public void volley_translation_3_db(String Book, int chapter, JSONArray jsonArray, Integer translation)throws JSONException{


            String text;
            for (int i = 0; i < jsonArray.length(); i++) {
                text =  jsonArray.getJSONObject(i).getString("text");

                if (translation == 1){
                    theDatabase.addItem(Book, chapter,i + 1,text);

                }

                System.out.println(Book + chapter);
            }



    }

    public void KJV_download(View view) {
        add_factor = "?translation=kjv";
        translation = 1;
        try {
            volley_translation_0_books();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void WEB_download(View view) {
        add_factor = "";
        translation = 0;
        try {
            volley_translation_0_books();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

