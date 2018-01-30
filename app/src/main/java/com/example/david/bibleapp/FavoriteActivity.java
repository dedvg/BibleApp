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
    //the displayed text in the list
    ArrayList<String> list_text = new ArrayList<>();

    // the information the current user has in his favorites
    UserClass current_user;

    // iniatializing
    ListView listview;
    Toolbar toolbar;
    Integer subject_length, clicked_subject, position_to_delete;

    // references for firebase use
    DatabaseReference the_database;
    FirebaseAuth the_auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // intializing references
        toolbar = findViewById(R.id.toolbar);
        listview = findViewById(R.id.ListView);
        the_auth = FirebaseAuth.getInstance();
        the_database = FirebaseDatabase.getInstance().getReference();

        // add backbutton
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // set some layout for the custom toolbar and an onclick listener on the back button
        toolbar.setTitle("Favories");
        toolbar.setSubtitle("long tap an item te delete it");
        toolbar.setNavigationOnClickListener(new navigationBackClicked());

        getUserFirebase();
        listview.setOnItemLongClickListener(new LongClickListener());

    }
    /*
    dependent whether a subject has been clicked it will go one step back
     */
    private void goBack() {
        if (clicked_subject == null)
        {
            // if there is no clicked subject go back to UserActivity
            finish();
        }
        else {

            // else set clicked subject to null and get the listview filled with the subjects
            clicked_subject = null;
            getUserFirebase();
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
    gets the current UserClass from the user from firebase
     */
    public void getUserFirebase () {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser user = the_auth.getCurrentUser();
                current_user = dataSnapshot.child("users").child(user.getUid()).getValue(UserClass.class);

                // check if there are any subjects, if so display them else give a toast
                if (current_user.subjects != null) {refreshSubjects();}
                else{
                    Toast.makeText(FavoriteActivity.this, "Nothing added to favorites", Toast.LENGTH_SHORT).show();
                    list_text.clear();
                    fillList();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // if not possible toast it
                Toast.makeText(FavoriteActivity.this, " please restart app with internet connection",
                        Toast.LENGTH_SHORT).show();
            }
        };
        the_database.addListenerForSingleValueEvent(postListener);
    }

    /*
    shows the subjects that are present in the UserClass from the current user
     */
    private void refreshSubjects() {
        list_text.clear();
        for (int i = 0; i < current_user.subjects.size(); i ++){
            list_text.add(current_user.subjects.get(i).name);
        }
        listview.setOnItemClickListener(new FavoriteActivity.clicklistener());
        fillList();
    }
    /*
    will show the verses in the UserClass with use of a custom adapter(verseAdapter)
    and removes the onclick function
     */
    public void refreshVerses(){
        subject_length = current_user.subjects.get(clicked_subject).verses.size();
        listview.setOnItemClickListener(null);
        VerseAdapter verseAdaper = new VerseAdapter();
        listview.setAdapter(verseAdaper);
    }
    /*
    handles the onclick events from the list by setting a subjects and showing the verses
     */
    private class clicklistener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            clicked_subject = position;
            refreshVerses();
        }
    }

    /*
   long click listener to delete item from firebase
   if long clicked a delete message will be shown to ask whether the user really
   wants to delete this
    */
    private class LongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            position_to_delete = position;
            deleteDialog();
            return true;
        }
    }

    /*
  custom adapter for seeing Listview items
  only used to show the verses in the UserClass in a proper way
   */
    class VerseAdapter extends BaseAdapter{

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

            // change the description accordingly and set it on the description textview
            VerseClass current_verse = current_user.subjects.get(clicked_subject).verses.get(position);
            if (current_verse.end_verse == current_verse.begin_verse){
                description_txt.setText(current_verse.book + " " + current_verse.chapter + " : " + current_verse.begin_verse + current_verse.translation);
            }
            else {
                description_txt.setText(current_verse.book + " " + current_verse.chapter + " : " + current_verse.begin_verse + " - " + current_verse.end_verse + current_verse.translation);
            }

            // set the text of all verses in the verse textview
            String text = "";
            for (int i = 0;  i < current_verse.text.size(); i ++){
                Integer verse_number = i + current_verse.begin_verse;
                text +=" " + verse_number.toString() +": " + current_verse.text.get(i);
            }
            verses_txt.setText(text);
            return convertView;
        }
    }
    /*
    custom adapter used for the subjects to give it a proper size
     */
    class SubjectAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            if (current_user.subjects == null){
                return 0;
            }
            return current_user.subjects.size();
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
            convertView = getLayoutInflater().inflate(R.layout.row_user, null);

            TextView verse_txt = convertView.findViewById(R.id.list_item);
           String text = current_user.subjects.get(position).name;
            verse_txt.setText(text);
            return convertView;
        }
    }

    /*
    will fill the list with what is currently in list_text only used for subjects
     */
    public void fillList() {
        SubjectAdapter theAdapter = new SubjectAdapter();
        listview.setAdapter(theAdapter);
        listview.setVisibility(View.VISIBLE);

    }
    /*
    the onclick listener for the deleteDialog
    if the positive button is clicked the subject will get deleted
     */
    DialogInterface.OnClickListener deleteClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int choice) {
            switch (choice) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (clicked_subject == null)
                    {
                        deleteSubject(position_to_delete);
                    }
                    else {
                        delete_verses(position_to_delete);
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };
    /*
    deletes subject from firebase by asking first if the user wants to
     */
    private void deleteDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteActivity.this);
        builder.setMessage("Do you really want to delete this from your Favorites?"  )
                .setPositiveButton("Yes", deleteClickListener)
                .setNegativeButton("No", deleteClickListener).show();
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

        // set the new UserClass in Firebase
        add_to_firebase(to_change);
    }
    /*
    deletes a subject from the UserClass
     */
    public void deleteSubject(int position){

        UserClass to_change = current_user;
        to_change.subjects.remove(position);
        add_to_firebase(to_change);
    }

    /*
    will add the custom userclass to firebase
    TODO controleren of het zonder ondatachange kan
     */
    private void add_to_firebase(final UserClass to_change) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser user = the_auth.getCurrentUser();
                the_database.child("users").child(user.getUid()).setValue(to_change);
                getUserFirebase();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // if not possible toast it
                Toast.makeText(FavoriteActivity.this, " please restart app with internet connection",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        };
        the_database.addListenerForSingleValueEvent(postListener);

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
