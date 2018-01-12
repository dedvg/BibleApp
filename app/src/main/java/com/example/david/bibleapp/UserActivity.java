package com.example.david.bibleapp;

import android.content.ClipData;
import android.content.Intent;
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

//        toolbar.setLogo(R.drawable.common_google_signin_btn_icon_dark);

        // add backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layer -= 2;
                select_layer();
            }
        });

        set_click_listener();
    }

    public void set_click_listener(){
        listView.setOnItemClickListener(new clicklistener());
    }

    public void remove_click_listener(){
        listView.setOnItemClickListener(null);

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

        switch (item.getItemId()) {
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
        layer = 1;
        select_layer();

    }

    private void GoToFavorites() {
        Intent intent = new Intent(this, FavoriteActivity.class);
        // starts the new activity
        startActivity(intent);
    }


    public void volley() {

        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String mJSONURLString = "https://bible-api.com/" + selected_book + "%20" + selected_chapter;
        System.out.println("HIIIIER");
        System.out.println(mJSONURLString);

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

    public void show_books() {
        // done with use of https://www.youtube.com/watch?v=h71Ia9iFWfI
        JSONObject jsonObject = null;
        JSONArray jsonArray;
        String testString;

        try {
            InputStream is = getAssets().open("books2.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");

            jsonObject = new JSONObject(json);
            String search;
            if (old == true)
            {
                search = "Old";
            }
            else{
                search = "New";
            }
            System.out.println(search);
            System.out.println(old);
            System.out.println("HIIIIIIIIIIIIIIIER");

            BOOKSjson = jsonObject.getJSONObject("sections").getJSONArray(search);

            ListText.clear();
            for (int i = 0; i < BOOKSjson.length(); i++) {
                String verse = BOOKSjson.getJSONObject(i).getString("key");

//                verse += "   " + BOOKSjson.getJSONObject(i).getString("val");

                ListText.add(verse);
            }
            fill_list();


        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(UserActivity.this, "Fault1", Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(UserActivity.this, "Fault2", Toast.LENGTH_SHORT).show();
        }


    }

    private class clicklistener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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

    getSupportActionBar().setTitle("layer = " + layer);

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

       selected_chapter = clicked_pos + 1;
        volley();

    }

   if (layer <4){
        layer += 1;
   }

}
}






