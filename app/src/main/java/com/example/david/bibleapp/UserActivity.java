package com.example.david.bibleapp;

import android.content.ClipData;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView testT;
    ListView listView;
    List<String> ListText = new ArrayList<String>();

    ArrayList<String > booksList = new ArrayList<String>();
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
        toolbar.setLogo(R.drawable.common_google_signin_btn_icon_dark);

        // add backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UserActivity.this, "clearing text", Toast.LENGTH_SHORT).show();
                ListText.clear();
                fill_list();
            }
        });
    }


    public void logout() {

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        // starts the new activity
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.logout:
                logout();
                break;
            case R.id.favorites:
                GoToFavorites();
                break;
            case R.id.new_text:
                new_text();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void new_text() {
        // TODO: 10-1-2018
       Toast.makeText(UserActivity.this, "For now only matthew 3 is available", Toast.LENGTH_SHORT).show();
        volley();
    }

    private void GoToFavorites() {
        Intent intent = new Intent(this, FavoriteActivity.class);
        // starts the new activity
        startActivity(intent);
    }


    public void volley() {

        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String mJSONURLString = "https://bible-api.com/matthew%203";

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
        for (int i = 0; i < jsonArray.length(); i ++){
            String verse = i + 1 +  ": " + jsonArray.getJSONObject(i).getString("text");
            ListText.add(verse);
        }
        fill_list();
    }

    public void fill_list ()
    {
        ArrayAdapter theAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ListText);
        listView.setAdapter(theAdapter);
        listView.setVisibility(View.VISIBLE);

    }

    public void show_books(){
        // done with use of https://www.youtube.com/watch?v=h71Ia9iFWfI
        try {
            InputStream booksIn = getAssets().open("books.json");
            int size = booksIn.available();
            byte[] buffer = new byte[size];
            booksIn.read();
            booksIn.close();

            String json = new String(buffer, "UTF-8");

            JSONArray jsonArray = new JSONArray(json);

            testT.setText(jsonArray.toString());
        } catch (IOException e) {
            e.printStackTrace();
            testT.setText("hello");
        }
        catch (JSONException e) {
            e.printStackTrace();
            testT.setText("bye");

        }

    }
}






