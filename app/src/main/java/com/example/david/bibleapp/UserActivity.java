package com.example.david.bibleapp;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView testT;
    ListView listView;
    List<String> ListText = new ArrayList<String>();
    Integer layer = 0, chapters = 0, clicked_pos, selected_chapter;
    Boolean old = true;
    JSONArray BOOKSjson;
    String selected_book;
    TranslationDatabase theDatabase;
    ArrayList<String> booksList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Matthew");
        toolbar.setSubtitle("chapter 3");
        testT = findViewById(R.id.testTXT);
        listView = findViewById(R.id.listView);
        theDatabase = TranslationDatabase.getInstance(this.getApplicationContext());
//        toolbar.setLogo(R.drawable.common_google_signin_btn_icon_dark);

        // add backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layer <= 0)
                {
                    before_logout();
                }
                else {
                    layer -= 1;
                    select_layer();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                before_logout();
                break;
            case R.id.favorites:
                GoToFavorites();
                break;
            case R.id.get_table:
                click_listener(false);
                read_data();
                break;
            case R.id.test_function:
                test_function_1();
                break;

            case R.id.test_function2:
                try {
                    volley_biblebook();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.new_text:
                click_listener(true);

                layer = 1;
                select_layer();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void test_function_1() {
    }

    public void click_listener(Boolean set){
        if (set == true){
            listView.setOnItemClickListener(new clicklistener());
        }
        else{
            listView.setOnItemClickListener(null);
        }
    }

    public void volley_biblebook() throws JSONException {
        chapters = BOOKSjson.getJSONObject(1).getInt("val");
        String book = "Genesis";

        Toast.makeText(UserActivity.this, chapters.toString(), Toast.LENGTH_SHORT).show();
        for (int i = 0; i < chapters; i ++){
            int chapter = i + 1;
            volley_chapter(book , chapter);
        }

    }


    public void add_chapter_toDB(String Book, int chapter, JSONArray jsonArray, Integer translation)throws JSONException{

        if (!theDatabase.check_chapter_existence_WEB(Book, chapter)){
            String text;
            for (int i = 0; i < jsonArray.length(); i++) {
                text =  jsonArray.getJSONObject(i).getString("text");
                ListText.add(text);

                if (translation == 1){
                    theDatabase.addItem(Book, chapter,i + 1,text);

                }

            }
        }
        else {
            Toast.makeText(UserActivity.this, "hiiiiier ", Toast.LENGTH_SHORT).show();
        }

    }

    public void read_data(){
        ListText.clear();
        Cursor theCursor = theDatabase.getData();

        while (theCursor.moveToNext()){
            String book = theCursor.getString(0);

            String chapter = theCursor.getString(1);
            String number = theCursor.getString(2);
            String verse = theCursor.getString(3);
            ListText.add(book + "  "+ chapter+ "  " + number + "  "+ verse);
        }
        fill_list();


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

        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
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


    private void GoToFavorites() {
        Intent intent = new Intent(this, FavoriteActivity.class);
        // starts the new activity
        startActivity(intent);
    }
    public void volley_chapter(final String book, final int chapter) {

        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String mJSONURLString = "https://bible-api.com/" + book + "%20" + chapter;

        // Initialize a new JsonObjectRequest instance
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, mJSONURLString, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray jsonArray = response.getJSONArray("verses");

                            add_chapter_toDB(book, chapter, jsonArray, 1);

                        } catch (JSONException e) {
                            // if this shows something changed in the JSON
                            Toast.makeText(UserActivity.this,
                                    "problem with accesing the JSON",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // prompt the user to start again
                        Toast.makeText(UserActivity.this,
                                "No connection please restart the app with internet acces",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    public void volley() {

        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String mJSONURLString = "https://bible-api.com/" + selected_book + "%20" + selected_chapter;

        // Initialize a new JsonObjectRequest instance
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, mJSONURLString, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray jsonArray = response.getJSONArray("verses");

                            read_chapter(jsonArray);

                        } catch (JSONException e) {
                            // if this shows something changed in the JSON
                            Toast.makeText(UserActivity.this,
                                    "problem with accesing the JSON",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // prompt the user to start again
                        Toast.makeText(UserActivity.this,
                                "No connection please restart the app with internet acces",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    public void read_chapter(JSONArray jsonArray) throws JSONException {
        ListText.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            String verse = i + 1 + ": " + jsonArray.getJSONObject(i).getString("text");
            ListText.add(verse);

        }
        fill_list();
    }

    public void fill_list() {
        ArrayAdapter theAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ListText);
        listView.setAdapter(theAdapter);
        listView.setVisibility(View.VISIBLE);

    }

    public void load_booksJSON() throws JSONException, IOException {
        // done with use of https://www.youtube.com/watch?v=h71Ia9iFWfI
        JSONObject jsonObject = null;


            InputStream is = getAssets().open("books2.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");

            jsonObject = new JSONObject(json);
            String search;
            if (old == true) {
                search = "Old";
            } else {
                search = "New";
            }

            BOOKSjson = jsonObject.getJSONObject("sections").getJSONArray(search);



    }
    public void show_books() {

            ListText.clear();
            for (int i = 0; i < BOOKSjson.length(); i++) {
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

    private class clicklistener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (layer <4){
                layer += 1;
            }
            clicked_pos = position;
            select_layer();
        }
    }

    public void show_chapters(Integer chapters){
        ListText.clear();
        for (int i = 0; i < chapters; i ++){
            ListText.add(String.valueOf(i + 1));
        }
        fill_list();
    }

    public void select_layer()  {
        if (layer <= 0){
            ListText.clear();
            fill_list();
            // to make sure the layer is never below 0
            layer = 0;
        }
        else if (layer == 1 ){
            ListText.clear();
            ListText.add("Old");
            ListText.add("New");
            fill_list();
        }
        else if (layer == 2){
            if (clicked_pos == 0)
            {
                old = true;
            }
            else
            {
                old = false;
            }
            show_books();
        }
        if (layer == 3){

            try {
                chapters = BOOKSjson.getJSONObject(clicked_pos).getInt("val");
                selected_book = BOOKSjson.getJSONObject(clicked_pos).getString("key");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            show_chapters(chapters);
        }
        if (layer == 4){
            click_listener(false);

            selected_chapter = clicked_pos + 1;
            volley();
        }
    }
}






