package com.example.david.bibleapp;

import android.content.DialogInterface;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
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

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {
    ArrayList<String> ListText = new ArrayList<>();
    UserClass current_user;

    ListView listView;
    Toolbar toolbar;
    DatabaseReference mDatabase;
    FirebaseAuth authTest;
    Integer subject_length, clicked_subject;
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

        toolbar.setTitle("Favories");
        toolbar.setSubtitle("long tap an item te delete it");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             go_back();
            }
        });
        get_user();
    }

    private void go_back() {
        if (clicked_subject == null)
        {
            finish();
        }
        else {
            clicked_subject = null;
            get_user();
        }
    }

    /*
    to use a custom menu this is needed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // needed for showing the custom actionbar
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // to find a menuitem it has to be set here
        MenuItem translation = menu.findItem(R.id.switch_translation);
        MenuItem favorites = menu.findItem(R.id.favorites);
        translation.setVisible(false);
        favorites.setVisible(false);
        return true;
    }

    /*
    gets the current user from firebase
     */
    public void get_user () {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser user = authTest.getCurrentUser();
                current_user = dataSnapshot.child("users").child(user.getUid()).getValue(UserClass.class);
                if (current_user.subjects != null) {refresh_subjects();}
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

    /*
    shows the subjects that are present
     */
    private void refresh_subjects() {
        ListText.clear();
        for (int i = 0; i < current_user.subjects.size(); i ++){
            ListText.add(current_user.subjects.get(i).name);
        }
        listView.setOnItemClickListener(new FavoriteActivity.clicklistener());
        fill_list();
    }
    /*
    refresh the user data
     */
    public void refresh_verses(){
        subject_length = current_user.subjects.get(clicked_subject).verses.size();
        listView.setOnItemClickListener(null);
        listView.setOnItemLongClickListener(new LongClickListener());
        VerseAdaper verseAdaper = new VerseAdaper();
        listView.setAdapter(verseAdaper);
    }
    /*
    make a clicklistener on the ListView
     */
    private class clicklistener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            clicked_subject = position;
           refresh_verses();
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

            TextView description_txt = convertView.findViewById(R.id.chapterTXT);
            TextView verses_txt = convertView.findViewById(R.id.textTXT);

            VerseClass current_verse = current_user.subjects.get(clicked_subject).verses.get(position);
            if (current_verse.end_verse == current_verse.begin_verse){
                description_txt.setText(current_verse.book + " " + current_verse.chapter + " : " + current_verse.begin_verse + current_verse.translation);
            }
            else {
                description_txt.setText(current_verse.book + " " + current_verse.chapter + " : " + current_verse.begin_verse + " - " + current_verse.end_verse + current_verse.translation);
            }
            String text = "";
            for (int i = 0;  i < current_verse.text.size(); i ++){
                Integer verse_number = i + 1;
                text +=" " + verse_number.toString() +": " + current_verse.text.get(i);
            }
            verses_txt.setText(text);
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

    /*
    long click listener to delete item from firebase
     */
    private class LongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ask_delete_verse(position);
            return false;
        }
    }
    /*
    asks the user if the user really wants to delete the verses from his favorits
     */
    public void ask_delete_verse(final int position){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choice) {
                switch (choice) {
                    case DialogInterface.BUTTON_POSITIVE:
                        delete_verses(position);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteActivity.this);
        builder.setMessage("Do you really want to delete this from your Favorites?"  )
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    /*
    delete verse/subject from firebase by adding the changed UserClass back to Firebase
     */
    public void delete_verses(int position){
        UserClass to_change = current_user;
        if (to_change.subjects.get(clicked_subject).verses.size() == 1){
            // needed beacause remove does not accept an Integer
            // if the length is 1 verse delete the subject
            int temp = clicked_subject;
            to_change.subjects.remove(temp);
        }
        else {
            to_change.subjects.get(clicked_subject).verses.remove(position);
            Toast.makeText(FavoriteActivity.this, "deleted", Toast.LENGTH_SHORT).show();
        }
        add_to_firebase(to_change);
    }

    /*
    will add the custom userclass to firebase
     */
    private void add_to_firebase(final UserClass to_change) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser user = authTest.getCurrentUser();
                mDatabase.child("users").child(user.getUid()).setValue(to_change);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // if not possible toast it
                Toast.makeText(FavoriteActivity.this, " please restart app with internet connection",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        };
        mDatabase.addListenerForSingleValueEvent(postListener);
        get_user();
    }
}
