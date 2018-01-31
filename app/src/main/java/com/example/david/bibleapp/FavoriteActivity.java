package com.example.david.bibleapp;

/*
This Activity allows the user to read their subjects they have added.
By clicking on a subject the belonging verses will be shown and from where the verses are (book, chapter, begin verse, end verse).
The user is able go back to UserActivity to continue reading
*/

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

    // iniatializing references
    ListView listview;
    Toolbar toolbar;
    Integer subject_length, clicked_subject, position_to_delete;

    // references for firebase use
    DatabaseReference the_database;
    FirebaseAuth the_auth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // intializing references
        toolbar = findViewById(R.id.toolbar);
        listview = findViewById(R.id.ListView);
        the_auth = FirebaseAuth.getInstance();
        the_database = FirebaseDatabase.getInstance().getReference();
        user = the_auth.getCurrentUser();

        // add backbutton
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // set some layout for the custom toolbar
        toolbar.setTitle("Favories");
        toolbar.setSubtitle("long tap an item te delete it");

        // get the current user from Firebase
        getUserFirebase();

        // set onclicklisteners
        listview.setOnItemLongClickListener(new LongClickListener());
        toolbar.setNavigationOnClickListener(new navigationBackClicked());
    }


    /*
    to use a custom menu this is needed
    some items of the custom toolbar are made invisible
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // needed for showing the custom actionbar
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // to find a menuitem it has to be set here
        MenuItem translation = menu.findItem(R.id.switch_translation);
        MenuItem favorites = menu.findItem(R.id.favorites);
        MenuItem logout = menu.findItem(R.id.logout);

        logout.setVisible(false);
        translation.setVisible(false);
        favorites.setVisible(false);
        return true;
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
    gets the current UserClass from the user from firebase and adapt layout accordingly
    */
    public void getUserFirebase () {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot db) {

                // get the current user
                current_user = db.child("users").child(user.getUid()).getValue(UserClass.class);

                // check if there are any subjects, if so display them else give a toast
                if (current_user.subjects != null) {refreshSubjects();}
                else{
                    Toast.makeText(FavoriteActivity.this, "Nothing added to favorites",
                                   Toast.LENGTH_SHORT).show();
                    list_text.clear();
                    fillList();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // if not possible toast it
                Toast.makeText(FavoriteActivity.this, " please restart app with" +
                               " internet connection", Toast.LENGTH_SHORT).show();
            }
        };
        the_database.addListenerForSingleValueEvent(postListener);
    }

    /*
    shows the subjects that are present in the UserClass from the current user
    */
    private void refreshSubjects() {

        // clear the text and add the subjects one by one
        list_text.clear();
        for (int i = 0; i < current_user.subjects.size(); i ++){
            list_text.add(current_user.subjects.get(i).name);
        }

        // set an onclicklistener
        listview.setOnItemClickListener(new FavoriteActivity.clicklistener());

        // show the subjects
        fillList();
    }

    /*
    will show the verses in the UserClass with use of a custom adapter(VerseAdapter)
    and removes the onclick function
    subject_length is set here (needed for the verseAdapter
    */
    public void refreshVerses(){
        subject_length = current_user.subjects.get(clicked_subject).verses.size();
        listview.setOnItemClickListener(null);
        VerseAdapter verse_adapter = new VerseAdapter();
        listview.setAdapter(verse_adapter);
    }

    /*
    will fill the list with what is currently in list_text only used for subjects
    this is done with a custom adapter used to get a better layout
    */
    public void fillList() {
        SubjectAdapter the_adapter = new SubjectAdapter();
        listview.setAdapter(the_adapter);
    }

    /*
    deletes subject from firebase by asking first if the user wants to with
    */
    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteActivity.this);
        builder.setMessage("Do you really want to delete this from your Favorites?"  )
                .setPositiveButton("Yes", deleteDialogListener)
                .setNegativeButton("No", deleteDialogListener).show();
    }

    /*
    delete verse/subject from firebase by adding the changed UserClass back to Firebase
    */
    public void deleteVerses(int position){
        UserClass to_change = current_user;

        // if there is only one VerseClass delete the whole subject
        // else delete the clicked VerseClass from the subject and toast it
        if (to_change.subjects.get(clicked_subject).verses.size() == 1){

            // needed beacause remove does not accept an Integer
            int temp = clicked_subject;
            to_change.subjects.remove(temp);
        }
        else {
            to_change.subjects.get(clicked_subject).verses.remove(position);
            Toast.makeText(FavoriteActivity.this, "deleted", Toast.LENGTH_SHORT).show();
        }

        // set the new UserClass in Firebase
        addToFirebase(to_change);
    }

    /*
    deletes a subject from the UserClass
    */
    public void deleteSubject(int position){
        UserClass to_change = current_user;
        to_change.subjects.remove(position);
        addToFirebase(to_change);
    }

    /*
    will add the custom userclass to firebase
    */
    private void addToFirebase(final UserClass to_change) {
        the_database.child("users").child(user.getUid()).setValue(to_change);

        // get the new user from firebase and update the layout
        getUserFirebase();
    }

    /*
    custom adapter for seeing Listview items
    only used to show the verses of a clicked subject
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

            // set the custom layout for the adapter
            convertView = getLayoutInflater().inflate(R.layout.row_favorites, null);

            // create references
            TextView description_txt = convertView.findViewById(R.id.chapterTXT);
            TextView verses_txt = convertView.findViewById(R.id.textTXT);

            // change the description accordingly and set it on the description textview
            VerseClass verse_class = current_user.subjects.get(clicked_subject).verses.get(position);
            if (verse_class.end_verse == verse_class.begin_verse){
                description_txt.setText(verse_class.book + " " + verse_class.chapter + " : " +
                                        verse_class.begin_verse + verse_class.translation);
            }
            else {
                description_txt.setText(verse_class.book + " " + verse_class.chapter + " : " +
                                        verse_class.begin_verse + " - " + verse_class.end_verse
                                        + verse_class.translation);
            }

            // set the text of all verses in the verse textview
            String text = "";

            // add verse by verse to the text
            for (int i = 0;  i < verse_class.text.size(); i ++){

                // calculate the current verse number
                Integer verse_number = i + verse_class.begin_verse;
                text +=" " + verse_number.toString() +": " + verse_class.text.get(i);
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

            // if there are no subjects return 0 to create an empty listview
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

            // use of a custom row layout
            convertView = getLayoutInflater().inflate(R.layout.row_user, null);

            // create references and set the text accordingly
            TextView verse_txt = convertView.findViewById(R.id.list_item);
            String text = current_user.subjects.get(position).name;
            verse_txt.setText(text);
            return convertView;
        }
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

    /*
    the onclick listener for the deleteDialog
    if the positive button is clicked the subject will get deleted
    */
    DialogInterface.OnClickListener deleteDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int choice) {
            switch (choice) {
                case DialogInterface.BUTTON_POSITIVE:

                    // if clicked_subject is null a subject is long tapped and will be deleted
                    if (clicked_subject == null)
                    {
                        deleteSubject(position_to_delete);
                    }
                    else {

                        // a subject is selected so the long tapped verses will be deleted
                        deleteVerses(position_to_delete);
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

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
}
