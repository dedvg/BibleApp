package com.example.david.bibleapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.View;

import android.widget.Toast;



public class FavoriteActivity extends AppCompatActivity {


    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // add backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             go_back();
            }
        });
    }

    private void go_back() {
        Intent intent = new Intent(this, UserActivity.class);
        // starts the new activity
        startActivity(intent);
        finish();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // needed for showing the custom actionbar
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



}
