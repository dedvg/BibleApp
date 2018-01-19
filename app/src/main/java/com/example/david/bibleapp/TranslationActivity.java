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

    TextView title_txt, translation_txt, translation1_txt, translation2_txt;
    Button translation_btn, translation1_btn, translation2_btn;
    ListView listView;
    List<String> ListText = new ArrayList<String>();
    Toolbar toolbar;
    TranslationDatabase theDatabase;
    JSONArray BOOKSjson;
    Integer chapters, translation, given_book_int, layer, clicked_book;
    String add_factor, given_book ,book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);

        translation_btn = findViewById(R.id.transBTN);
        translation1_btn = findViewById(R.id.transBTN1);
        translation2_btn = findViewById(R.id.transBTN2);

        title_txt = findViewById(R.id.titleTXT);
        translation_txt = findViewById(R.id.transTXT);
        translation1_txt = findViewById(R.id.transTXT1);
        translation2_txt = findViewById(R.id.transTXT2);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Current translation = WEB");
        toolbar.setSubtitle("test");
        theDatabase = TranslationDatabase.getInstance(this.getApplicationContext());
        listView = findViewById(R.id.ListView);
        // add backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        show_download(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layer == 0){
                    go_back();
                }
                else{
                    show_download(false);
                    get_books();
                    layer = 0;
                }
            }
        });
        try {
            load_booksJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        given_book = intent.getStringExtra("book");
        given_book_int = intent.getIntExtra("book_int", 0);
        get_books();
        listView.setOnItemClickListener(new TranslationActivity.clicklistener());
        check_downloadreference(given_book_int, given_book);


    }

    private void check_downloadreference(Integer given_book_int, String given_book) {
        if (!Objects.equals(given_book, "")) {
            book = given_book;
            clicked_book = given_book_int;
            Toast.makeText(TranslationActivity.this, clicked_book.toString(), Toast.LENGTH_SHORT).show();
            show_download(true);
        } else {
            return;
        }
    }

    public void BTN_WEB(View view) {
        WEB_download();
    }

    public void BTN1_KJV(View view) {
        KJV_download();
    }

    private class clicklistener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            try {
                book = BOOKSjson.getJSONObject(position).getString("key");
                clicked_book = position;
                show_download(true);

                System.out.println("the book is" + book);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void show_download(Boolean show){

        if(show  == true){
            translation_txt.setVisibility(View.VISIBLE);
            translation1_txt.setVisibility(View.VISIBLE);
            translation2_txt.setVisibility(View.VISIBLE);

            title_txt.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
            set_text();
            layer = 1;
        }
        else{
            translation_btn.setVisibility(View.INVISIBLE);
            translation1_btn.setVisibility(View.INVISIBLE);
            translation2_btn.setVisibility(View.INVISIBLE);

            translation_txt.setVisibility(View.INVISIBLE);
            translation1_txt.setVisibility(View.INVISIBLE);
            translation2_txt.setVisibility(View.INVISIBLE);

            title_txt.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            layer = 0;
        }
    }

    private void set_text() {
        if (theDatabase.check_chapter1_existence_WEB(book)) {
            translation_txt.setText(book + " WEB already downloaded");
        }
        else{
            translation_txt.setText(book + " does not exist in WEB");
            translation_btn.setVisibility(View.VISIBLE);
        }
        if (theDatabase.check_chapter1_existence_KJV(book)) {
            translation1_txt.setText(book + " KJV already downloaded");
        }
        else{
            translation1_txt.setText(book + " does not exist in KJV");
            translation1_btn.setVisibility(View.VISIBLE);
        }
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
        chapters = BOOKSjson.getJSONObject(clicked_book).getInt("val");
        volley_translation_1_chapters(book);
    }

    public void volley_translation_1_chapters(String book) throws JSONException {
        //        for now only genesis

        for (int i = 0; i < chapters; i++) {
            int chapter = i + 1;
            volley_translation_2_chapter(book, chapter);
        }
    }
    public void get_books() {
        Integer upper_bound;
        ListText.clear();
        for (int i = 0; i < 66; i++) {
            String verse = null;
            try {
                verse = BOOKSjson.getJSONObject(i).getString("key");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ListText.add(verse);
        }
        fill_list();
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

    public void volley_translation_3_db(String Book, int chapter, JSONArray jsonArray)throws JSONException{


            String text;
            for (int i = 0; i < jsonArray.length(); i++) {
                text =  jsonArray.getJSONObject(i).getString("text");
                    theDatabase.addItem(Book, chapter,i + 1,text, translation);
                System.out.println(Book + chapter);
            }
    }
    /*
    done with https://www.youtube.com/watch?v=ZEEYYvVwJGY
    makes the custom adapter to enable a listview with a download button
     */

    public void KJV_download() {
        add_factor = "?translation=kjv";
        translation = 1;
        try {
            volley_translation_0_books();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void WEB_download() {
        add_factor = "";
        translation = 0;
        try {
            volley_translation_0_books();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void fill_list() {

        ArrayAdapter theAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ListText);
        listView.setAdapter(theAdapter);
        listView.setVisibility(View.VISIBLE);
    }
}

