package com.example.david.bibleapp;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.View;

import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class FavoriteActivity extends AppCompatActivity {
    ArrayList<String> ListText = new ArrayList<>();
    UserClass current_user;

    ListView listView;
    Toolbar toolbar;
    DatabaseReference mDatabase;
    FirebaseAuth authTest;
    Integer subject_length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = findViewById(R.id.ListView);
        authTest = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // add backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             go_back();
            }
        });
        get_user();
    }

    private void go_back() {
        finish();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // needed for showing the custom actionbar
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void get_user () {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser user = authTest.getCurrentUser();
                current_user = dataSnapshot.child("users").child(user.getUid()).getValue(UserClass.class);
                Show_Subjects();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // if not possible toast it
                Toast.makeText(FavoriteActivity.this, " please restart app with internet connection",
                        Toast.LENGTH_SHORT).show();
            }
        };
        mDatabase.addListenerForSingleValueEvent(postListener);
    }

    private void Show_Subjects() {
        ListText.clear();
        for (int i = 0; i < current_user.subjects.size(); i ++){
            ListText.add(current_user.subjects.get(i).name);
        }
        listView.setOnItemClickListener(new FavoriteActivity.clicklistener());
        fill_list();
    }

    private class clicklistener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            ListText.clear();
//            SubjectClass selected_subject = current_user.subjects.get(position);
//            subject_length = selected_subject.verses.size();
//            for (int i = 0; i < selected_subject.verses.size(); i ++) {
//                ArrayList<String> verses = selected_subject.verses.get(i).text;
//
//                for (int j = 0; j < verses.size(); j ++) {
//                    Integer verse_number = j + selected_subject.verses.get(i).begin_verse;
//                    ListText.add(verse_number.toString() + ": " + verses.get(j));
//                }
//            }
//            listView.setOnItemClickListener(null);
//
//            fill_list();
            VerseAdaper verseAdaper = new VerseAdaper();
            listView.setAdapter(verseAdaper);
        }
    }
    /*
    Custom adapter for seeing Listview items
     */
    class VerseAdaper extends BaseAdapter{

        @Override
        public int getCount() {

            
            return subject_length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.row_favorites, null);

            TextView description = convertView.findViewById(R.id.chapterTXT);
            ListView versesLV = convertView.findViewById(R.id.ListView_row);
            ListText.add("hoi");

            description.setText("hoi");
            versesLV.setAdapter(new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, ListText));

            return convertView;
        }
    }


    /*
    will fill the list with what is currently in ListText
     */
    public void fill_list() {
        ArrayAdapter theAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ListText);
        listView.setAdapter(theAdapter);
        listView.setVisibility(View.VISIBLE);

    }
}
