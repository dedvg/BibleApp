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
    ListView listView ,row_list ;
    List<String> ListText = new ArrayList<String>();
    Integer layer = 0, translation = 0, clicked_pos;
    JSONArray BOOKSjson;
    TranslationDatabase theDatabase;
    AlertDialog dialog_verses;
    FirebaseAuth authTest;
    UserClass to_change;
    DatabaseReference mDatabase;
    NavigationClass navigatorClass = new NavigationClass();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Intent intent = getIntent();
        translation = intent.getIntExtra("translation", 0);


        // create references
        toolbar = findViewById(R.id.toolbar);
        listView = findViewById(R.id.listView);
        theDatabase = TranslationDatabase.getInstance(this.getApplicationContext());
        authTest = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // set up the action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bible App");
        toolbar.setSubtitle("made by David");

        // initialize some varioables

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
                    setLayerLayout();
                    clickListener(true);
                }
            }
        });
        clickListener(true);

        setLayerLayout();
        // will make all info available from the local JSON
        try {
            getBookJson();
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
                logoutMsg();
                break;
            case R.id.favorites:
                GoToFavorites();
                break;
            case R.id.switch_translation:
                switchTranslation();
              break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    will create a clicklistener on the listview
    it keeps track of the layer and changes variables to make navigation possible

    add factor and upper bound determine which books need to be shown

    old testament = 0 -39
    new testament = 40 - 66

     */

    private class ClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            clicked_pos = position;
            switch (layer){
                case 0:
                    if (clicked_pos == 0)
                    {
                        navigatorClass.setOld(true);
                    }
                    else{
                        navigatorClass.setOld(false);
                    }
                    break;
                case 1:
                    navigatorClass.setSelected_book_int(clicked_pos);
                    try {
                        navigatorClass.selected_book = BOOKSjson.getJSONObject(navigatorClass.selected_book_int).getString("key");
                        navigatorClass.chapters = BOOKSjson.getJSONObject(navigatorClass.selected_book_int).getInt("val");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    navigatorClass.setSelected_chapter(clicked_pos);
                    break;
            }
            // something is clicked so the layer will become 1 higher and select layer will handle the layout
            layer += 1;
            setLayerLayout();
        }
    }
    /*
    this function will enable adding the clicked text to the favorites of the user
    */
    private class LongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Integer verse = position + 1;
            dialogFavorites(verse);
            Toast.makeText(UserActivity.this, "add " + navigatorClass.selected_book + " " + navigatorClass.selected_chapter.toString() + ":  " + verse.toString() , Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    /*
    test dialog made with use of https://stackoverflow.com/questions/10903754/input-text-dialog-android
     */
    public void dialogAddFavorites1(final Integer begin_verse, final Integer end_verse){
        final String[] m_Text = {""};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Under which name do you want to add this to firebase?");
        if (begin_verse == end_verse){
            builder.setTitle("Selected " + navigatorClass.selected_book + " " + navigatorClass.selected_chapter + " verse : " + begin_verse );

        }
        else{
            builder.setTitle("Selected " + navigatorClass.selected_book + " " + navigatorClass.selected_chapter + " verse : " + begin_verse + " - " + end_verse);
        }

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
                Integer row = translation + 3;
                Cursor theCursor = theDatabase.get_verses(navigatorClass.selected_book, navigatorClass.selected_chapter, begin_verse, end_verse, translation);
                while (theCursor.moveToNext()){
                    String verse = theCursor.getString(row);
                    VersesList.add(verse);
                }
                verseInFirebase(m_Text[0],begin_verse, end_verse,VersesList);
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

    public void dialogFavorites(final Integer verse ){
        Integer max_verse = 0;
        List<String> verses = new ArrayList<>();
        Cursor theCursor = theDatabase.get_max_verse(navigatorClass.selected_book, navigatorClass.selected_chapter, verse, translation);
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
                Integer option_clicked = verse + position;
                dialogAddFavorites1(verse, option_clicked);
                dialog_verses.cancel();
            }
        });
        dialogFavorites2();
    }
    /*
    actually showing the dialog
     */
    public void dialogFavorites2(){
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
    public void clickListener(Boolean set){
        if (set == true){
            listView.setOnItemClickListener(new ClickListener());
            listView.setOnItemLongClickListener(null);
        }
        else{
            listView.setOnItemClickListener(null);
            listView.setOnItemLongClickListener(new LongClickListener());
        }
    }




    /*
    popup which allows the user to select a different translation
    allows the user to read from a different translation
     */
    public void switchTranslation(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choice) {
                switch (choice) {
                    case DialogInterface.BUTTON_POSITIVE:
                        String translation_txt;
                        if( translation == 0){
                            translation = 1;
                            translation_txt = "KJV";
                        }
                        else {
                            translation = 0;
                            translation_txt = "WEB";
                        }
                        Toast.makeText(UserActivity.this, translation_txt + " selected", Toast.LENGTH_SHORT).show();
                        toolbar.setSubtitle(translation_txt);
                        if (!checkBookExistence()) {
                            layer = 1;
                        }
                        setLayerLayout();
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        break;
                }
            }
        };
        String translation_txt;
        if (translation == 0){
            translation_txt ="KJV";
        }
        else {
            translation_txt = "WEB";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        builder.setMessage("do you want to switch to " + translation_txt)
                .setNeutralButton("no", dialogClickListener)
                .setPositiveButton("yes", dialogClickListener).show();
    }

    /*
    creates a popup which asks the user if the user really wants to logout
    */
    public void logoutMsg(){
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

    public void fillList() {
        ArrayAdapter theAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ListText);
        listView.setAdapter(theAdapter);
        listView.setVisibility(View.VISIBLE);
    }
    /*
    will load all books and chapters in a ordered jsonarray
     */
    public void getBookJson() throws JSONException, IOException {
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
    will handle the listview layout and which functions need to be run

    layer 0 = choice between old and new testament
    layer 1 = choice between books of the old or new testament
    layer 2 = choice between the chapters of the selected book
    layer 3 = reading and adding to favorites of the selected chapter
    */
    public void setLayerLayout()  {
        clickListener(true);
        switch (layer) {
            case 0:
                layer0Layout();
                break;
            case 1:
                layer1Layout();
                break;
            case 2:
                layer2Layout();
                break;
            case 3:
                layer3Layout();
                break;
        }
    }
    /*
    will create a listview with old and new
     */
    public void layer0Layout (){
        ListText.clear();
        ListText.add("Old");
        ListText.add("New");
        fillList();
    }
    /*
   will fill the listview with books from the old or new testament

   add factor and upper bound determine which books will be shown

   0-39 old testament books

   40-66 new testament books
    */
    public void layer1Layout() {
        ListText.clear();
        for (int i = 0; i < navigatorClass.upper_bound; i++) {
            String book_name = null;
            try {
                book_name = BOOKSjson.getJSONObject(i + navigatorClass.add_factor).getString("key");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ListText.add(book_name);
        }
        fillList();
    }

    /*
    will show all chapters depending on the selected book
     */
    public void layer2Layout(){
        if (checkBookExistence()){
            ListText.clear();
            getSupportActionBar().setTitle(navigatorClass.selected_book);
            for (int i = 0; i < navigatorClass.chapters; i ++){

                // +1 because the chapters in the bible do not start with 0
                ListText.add(String.valueOf(i + 1));
            }
            fillList();
        }
        else {
            go_to_translation(navigatorClass.selected_book, navigatorClass.selected_book_int);
            layer = 1;
        }
    }
    /*
    gets the selected chapter from the database and presents it at the list
    if the database does not contain the information a download button will appear
    */
    public void layer3Layout(){
        getSupportActionBar().setTitle(navigatorClass.selected_book + " " + navigatorClass.selected_chapter.toString());
        clickListener(false);
        toolbar.setSubtitle("long tap to add to favorites");
        // clear the listview
        ListText.clear();

        // afther column 3 the translations are present
        Integer verse_column = 3 + translation;
        Cursor theCursor = theDatabase.getchapter(navigatorClass.selected_book, navigatorClass.selected_chapter, translation);
        Integer rows = theCursor.getCount();

        // if there are multiple rows make the text readable else show a download button
        // the results depend on the selected translation
        while (theCursor.moveToNext()){
            String number = theCursor.getString(2);
            String verse = theCursor.getString(verse_column);
            ListText.add(number + ":  "+ verse);
        }

        // will create an empty listview or full with verses
        fillList();
    }
    public boolean checkBookExistence (){
        Cursor theCursor = theDatabase.getchapter(navigatorClass.selected_book, 1, translation);
        Integer rows = theCursor.getCount();
        if (rows >= 1) {
            return true;
        }
        return false;
    }
    /*
    will add the selected verses to firebase
     */

    public void verseInFirebase(final String subject, final int begin_verse, final int end_verse, final ArrayList verses_list) {
        ValueEventListener postListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                VerseClass VerseText = new VerseClass(navigatorClass.selected_book, navigatorClass.selected_chapter ,begin_verse, end_verse, verses_list);
                if (translation == 0){VerseText.translation = "(WEB)";}
                else {VerseText.translation = "(KJV)";}

                boolean found = false;
                // get the userclass from firebase (from the current user)
                FirebaseUser user = authTest.getCurrentUser();
                to_change = dataSnapshot.child("users").child(user.getUid()).getValue(UserClass.class);
                ArrayList<SubjectClass> SubjectList;
                ArrayList<VerseClass> verses_to_change = new ArrayList<VerseClass>();
                // prevent it from being null
                if(to_change.subjects == null) {SubjectList = new ArrayList<SubjectClass>();}
                else{SubjectList = to_change.subjects;}
                for (int i = 0; i < SubjectList.size(); i++) {
                    if (Objects.equals(SubjectList.get(i).name, subject)) {
                        verses_to_change = SubjectList.get(i).verses;
                        SubjectList.get(i).verses = verses_to_change;
                        found = true;
                    }
                }
                verses_to_change.add(VerseText);
                if (!found){
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






