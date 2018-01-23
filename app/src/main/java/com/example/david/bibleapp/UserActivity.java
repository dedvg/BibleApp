package com.example.david.bibleapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;

import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.security.auth.Subject;

public class UserActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView testT;
    ListView listView ,row_list ;
    List<String> ListText = new ArrayList<String>();
    Integer layer = 0, chapters = 0, translation = 0, clicked_pos, selected_chapter, add_factor, selected_book_int, upper_bound;
    Boolean old = true;
    Button download_btn;
    JSONArray BOOKSjson;
    String selected_book;
    TranslationDatabase theDatabase;
    AlertDialog dialog_verses;
    FirebaseAuth authTest;
    UserClass to_change;
    DatabaseReference mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        // create references
        download_btn = findViewById(R.id.downloadBTN);
        toolbar = findViewById(R.id.toolbar);
        testT = findViewById(R.id.testTXT);
        listView = findViewById(R.id.listView);
        theDatabase = TranslationDatabase.getInstance(this.getApplicationContext());
        authTest = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        // set up the action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bible App");
        toolbar.setSubtitle("made by David");

        // initialize some varioables
        download_btn.setVisibility(View.INVISIBLE);

        // add backbutton to the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        // Todo set a nice image for this part
        //        toolbar.setLogo(R.drawable.common_google_signin_btn_icon_dark);


        /*
        Determines what happens when the back button is clicked.
        If the back button is pressed when the layer is 1 it will ask the user to logout.
         */
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layer > 0){
                    layer -= 1;
                    select_layer();
                    click_listener(true);
                }
            }
        });

        // will make all info available from the local JSON
        try {
            load_booksJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    will enable the use of a custom toolbar
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    /*
    handeles the on click events from the custom toolbar
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                before_logout();
                break;
            case R.id.favorites:
                GoToFavorites();
                break;
            case R.id.test_function:
                break;
            case R.id.switch_translation:
                switch_translation();
              break;
            case R.id.new_text:
                click_listener(true);
                layer = 1;
                select_layer();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    /*
    will bring the user to translationActivity to download the new book the user does not have
     */
    public void download_book(View view) {

        go_to_translation(selected_book, selected_book_int);
    }
    /*
    will create a clicklistener on the listview
    it keeps track of the layer and changes variables to make navigation possible

    add factor and upper bound determine which books need to be shown

    old testament = 0 -39
    new testament = 40 - 66

     */

    private class clicklistener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            clicked_pos = position;

            // to remember wheteher old or new is clicked
            if (layer == 1 && clicked_pos == 0)
            {
                old = true;
                add_factor = 0;
                upper_bound = 39;
            }
            else if(layer == 1){
                add_factor = 39;
                upper_bound = 27;
                old = false;
            }
            if (layer == 2){
                selected_book_int = clicked_pos + add_factor;
                try {
                    selected_book = BOOKSjson.getJSONObject(selected_book_int).getString("key");
                    chapters = BOOKSjson.getJSONObject(selected_book_int).getInt("val");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (layer == 3){
                selected_chapter = clicked_pos + 1;
            }

            // something is clicked so the layer will become 1 higher and select layer will handle the layout
            layer += 1;

            select_layer();
        }
    }
    /*
    this function will enable adding the clicked text to the favorites of the user
    TODO enable adding to favorites
    */
    private class LongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Integer verse = position + 1;
            Add_To_Favorites_Verses(verse);
            Toast.makeText(UserActivity.this, "add " + selected_book + " " + selected_chapter.toString() + ":  " + verse.toString() , Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    /*
    test dialog made with use of https://stackoverflow.com/questions/10903754/input-text-dialog-android
     */
    public void Add_To_Favortes(final Integer begin_verse, final Integer end_verse){
        final String[] m_Text = {""};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Under which name do you want to add this to firebase?");
        builder.setTitle("Selected " + selected_book + " " + selected_chapter + " verse :" + begin_verse + " - " + end_verse);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ArrayList<String> VersesList = new ArrayList<String>();
                m_Text[0] = input.getText().toString();
                Toast.makeText(UserActivity.this, m_Text[0], Toast.LENGTH_SHORT).show();
                Integer row = translation + 3;
//                TODO verse by verse is added, but the verses are stored seperatly in the SQL need to fix this
                Cursor theCursor = theDatabase.get_verses(selected_book, selected_chapter, begin_verse, end_verse, translation);
                String verse;
                while (theCursor.moveToNext()){
                    verse = theCursor.getString(row);
                    System.out.println(verse);
                    VersesList.add(verse);
                }
                add_text_to_firebase2(m_Text[0],selected_book,selected_chapter,begin_verse, end_verse,VersesList);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    /*
    helper function for adding to favorites, done with:
    https://www.youtube.com/watch?v=0gTXUHDz6BM
     */

    public void Add_To_Favorites_Verses(final Integer verse ){
        Integer max_verse = 0;
        List<String> verses = new ArrayList<>();
        System.out.println("BOOK " +  selected_book + " " + selected_chapter + " vers " + verse);
        Cursor theCursor = theDatabase.get_max_verse(selected_book, selected_chapter, verse, translation);
        while (theCursor.moveToNext()){
            max_verse = theCursor.getInt(0);
            System.out.println(max_verse.toString());
        }

        for (int i = verse; i <= max_verse ; i ++)
        {
            verses.add(String.valueOf(i));
        }
        row_list = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_custom_dialog, R.id.list_item_text, verses);
        row_list.setAdapter(adapter);
        row_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewGroup vg = (ViewGroup)view;
                TextView txt = vg.findViewById(R.id.list_item_text);
                Integer option_clicked = verse + position;
                Toast.makeText(UserActivity.this, "clicked " + option_clicked.toString(), Toast.LENGTH_SHORT).show();
                Add_To_Favortes(verse, option_clicked);
                dialog_verses.cancel();
            }
        });
        show_dialog_listview();
    }
    /*
    actually showing the dialog
     */
    public void show_dialog_listview(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Add to Favorties");
        builder.setMessage("till which verse do you want to add? ");

        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_NUMBER);
        builder.setView(row_list);
        dialog_verses = builder.create();
        dialog_verses.show();


    }
    /*
    function to set an on click listener to the list or a longclicklistener
    */
    public void click_listener(Boolean set){
        if (set == true){
            listView.setOnItemClickListener(new clicklistener());
            listView.setOnItemLongClickListener(null);
        }
        else{
            listView.setOnItemClickListener(null);
            listView.setOnItemLongClickListener(new LongClickListener());
        }
    }


    /*
    gets the selected chapter from the database and presents it at the list
    if the database does not contain the information a download button will appear
    */

    public void read_chapter(String book, Integer chapter){
        // clear the listview
        ListText.clear();

        // afther column 3 the translations are present
        Integer verse_column = 3 + translation;
        Cursor theCursor = theDatabase.getchapter(book, chapter, translation);
        Integer rows = theCursor.getCount();

        // if there are multiple rows make the text readable else show a download button
        // the results depend on the selected translation
        if (rows >= 1){
            while (theCursor.moveToNext()){
                String number = theCursor.getString(2);
                String verse = theCursor.getString(verse_column);
                ListText.add(number + ":  "+ verse);
            }
        }
        else {
            download_btn.setVisibility(View.VISIBLE);
        }

        // will create an empty listview or full with verses
        fill_list();
    }

    /*
    popup which allows the user to select a different translation
    allows the user to read from a different translation
     */
    public void switch_translation(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choice) {
                switch (choice) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Toast.makeText(UserActivity.this, "WEB selected", Toast.LENGTH_SHORT).show();
                        translation = 0;
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(UserActivity.this, "KJV selected", Toast.LENGTH_SHORT).show();
                        translation = 1;
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        builder.setMessage("Which translation do you want to use?")
                .setNeutralButton("current translation", dialogClickListener)
                .setPositiveButton("WEB", dialogClickListener)
                .setNegativeButton("KJV", dialogClickListener).show();
    }

    /*
    creates a popup which asks the user if the user really wants to logout
    */
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
    /*
    will fill the list with what is currently in ListText
     */

    public void fill_list() {
        ArrayAdapter theAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ListText);
        listView.setAdapter(theAdapter);
        listView.setVisibility(View.VISIBLE);
    }
    /*
    will load all books and chapters in a ordered jsonarray
     */
    public void load_booksJSON() throws JSONException, IOException {
        // done with use of https://www.youtube.com/watch?v=h71Ia9iFWfI
        JSONObject jsonObject = null;
        InputStream is = getAssets().open("books.json");
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, "UTF-8");
        jsonObject = new JSONObject(json);
        BOOKSjson = jsonObject.getJSONObject("sections").getJSONArray("whole_bible");
    }
    /*
    will fill the listview with books from the old or new testament

    add factor and upper bound determine which books will be shown

    0-39 old testament books

    40-66 new testament books
     */
    public void show_books() {
        ListText.clear();
        for (int i = 0; i < upper_bound; i++) {
            String verse = null;
            try {
                verse = BOOKSjson.getJSONObject(i + add_factor).getString("key");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ListText.add(verse);
        }
        fill_list();
    }

    /*
    will show all chapters depending on the selected book

     */
    public void show_chapters(Integer chapters){
        ListText.clear();
        for (int i = 0; i < chapters; i ++){

            // +1 because the chapters in the bible do not start with 0
            ListText.add(String.valueOf(i + 1));
        }
        fill_list();
    }

    /*
    will handle the listview layout and which functions need to be run

    layer 0 = empty list
    layer 1 = choice between old and new testament
    layer 2 = choice between books of the old or new testament
    layer 3 = choice between the chapters of the selected book
    layer 4 = reading and adding to favorites of the selected chapter
    */
    public void select_layer()  {
        download_btn.setVisibility(View.INVISIBLE);
        switch (layer) {
            case 0:
                ListText.clear();
                fill_list();
                break;
            case 1:
                ListText.clear();
                ListText.add("Old");
                ListText.add("New");
                fill_list();
                break;
            case 2:
                show_books();
                break;
            case 3:
                show_chapters(chapters);
                getSupportActionBar().setTitle(selected_book);
                break;
            case 4:
                getSupportActionBar().setSubtitle(selected_chapter.toString());
                click_listener(false);
                read_chapter(selected_book, selected_chapter);
                break;
        }
    }
    /*
    will add the selected verses to firebase
     */

    public void add_text_to_firebase2(final String subject, final String book, final int chapter, final int begin_verse, final int end_verse, final ArrayList verses_list) {
        ValueEventListener postListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                VerseClass VerseText = new VerseClass(book, chapter,begin_verse, end_verse, verses_list);
                boolean found = false;
                // get the userclass from firebase (from the current user)
                FirebaseUser user = authTest.getCurrentUser();
                to_change = dataSnapshot.child("users").child(user.getUid()).getValue(UserClass.class);
                SubjectClass Subject_to_change;
                ArrayList<SubjectClass> SubjectList = to_change.subjects;
                ArrayList<VerseClass> verses_to_change = new ArrayList<VerseClass>();
                if(SubjectList == null)
                {
                    SubjectList = new ArrayList<SubjectClass>();
                }
                else {
                    for (int i = 0; i < SubjectList.size(); i++) {
                        if (Objects.equals(SubjectList.get(i).name, subject)) {
                            Toast.makeText(UserActivity.this, "found", Toast.LENGTH_SHORT).show();
                            verses_to_change = SubjectList.get(i).verses;
                            verses_to_change.add(VerseText);
                            SubjectList.get(i).verses = verses_to_change;
                            found = true;
                        }
                    }
                }
                if (!found){
                    verses_to_change.add(VerseText);
                    SubjectClass new_subject = new SubjectClass(subject,verses_to_change);
                    SubjectList.add(new_subject);
                }
                to_change.subjects = SubjectList;
                mDatabase.child("users").child(user.getUid()).setValue(to_change);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // if not possible toast it
                Toast.makeText(UserActivity.this, " error in adding",
                        Toast.LENGTH_SHORT).show();
            }
        };
        mDatabase.addListenerForSingleValueEvent(postListener);
    }

    /*
    will logout the user from firebase and go back to the MainActivity
    */
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        // starts the new activity
        startActivity(intent);
        finish();
    }
    /*
    will go to the favoritesActivity
    */
    private void GoToFavorites() {
        Intent intent = new Intent(this, FavoriteActivity.class);
        // starts the new activity
        startActivity(intent);
    }
    /*
       Goes to TranslationActivity with selected book and needed information to download it
    */
    private void go_to_translation(String book, Integer selected_book_int) {
        Intent intent = new Intent(this, TranslationActivity.class);
        intent.putExtra("book", book);
        intent.putExtra("book_int", selected_book_int);
        intent.putExtra("translation", translation);
        intent.putExtra("jsonArray", BOOKSjson.toString());


        System.out.println("before : " +book + selected_book_int);
        // starts the new activity
        startActivity(intent);
        finish();
    }
}






