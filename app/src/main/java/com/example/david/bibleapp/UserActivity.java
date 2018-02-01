package com.example.david.bibleapp;

/*
By David van Grinsven, Minor Programmeren, 2018, UvA

This Activity is the Main Activity the user will use.
This activity allows the user to logout, switch translation, read the bible (if already downloaded),
and navigate to favorites.
The user will first need to select old or new testament, then a biblebook and then the chapter.
When reading a chapter the user can navigate to the next chapter and previous chapter if available
with use of the provided buttons beside the listview.
When long tapping a verse the user can add a selection of verses from that chapter to their
favorites with a name they can fill in.
 */

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserActivity extends AppCompatActivity {

    Toolbar toolbar;
    ListView listview, row_list;
    List<String> list_text = new ArrayList<String>();
    Integer layer = 0, translation = 0, clicked_pos;
    JSONArray books_json;
    TranslationDatabase sql_databse;
    AlertDialog dialog_verses;
    FirebaseAuth the_auth;
    String subject;
    DatabaseReference the_database;
    Button leftbtn, rightbtn;
    private Animation animation_click;
    EditText input;
    NavigationClass navigatorClass = new NavigationClass();
    VerseClass verse_text;
    UserClass to_change;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Intent intent = getIntent();

        // get the translation from the intent else set it to 0 (WEB)
        translation = intent.getIntExtra("translation", 0);

        // set animation for the buttons
        animation_click = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        // initializing references
        toolbar = findViewById(R.id.toolbar);
        listview = findViewById(R.id.listView);
        sql_databse = TranslationDatabase.getInstance(this.getApplicationContext());
        the_auth = FirebaseAuth.getInstance();
        the_database = FirebaseDatabase.getInstance().getReference();
        leftbtn = findViewById(R.id.leftBTN);
        rightbtn = findViewById(R.id.rightBTN);

        // add backbutton to the toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // add some layout for the custom toolbar
        getSupportActionBar().setTitle("Bible App");
        toolbar.setSubtitle("made by David");

        // sets the layout ready for use
        setLayerLayout();

        // gets the information needed to navigate from a local JSON
        try {
            getBookJson();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // set on click listener for the navigationBackButton
        toolbar.setNavigationOnClickListener(new navigationBackClicked());
    }

    /*
    handeles the on click events from the custom toolbar
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logoutDialog();
                break;
            case R.id.favorites:
                goToFavorites();
                break;
            case R.id.switch_translation:
                switchTranslationDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
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
    will handle the leftbutton on click events
    sets the previous chapter as readable
     */
    public void previousChapter(View view) {
        // prevent the user from reading a chapter that does not exist
        if (0 < navigatorClass.selected_chapter){

            // set the new chapter and adapt the layout to it
            navigatorClass.selected_chapter = navigatorClass.selected_chapter - 1;
            setLayerLayout();

            // show an animation
            view.startAnimation(animation_click);
        }
    }

    /*
    will handle the rightbutton on click events
    set the next chapter as readable
    */
    public void nextChapter(View view) {

        // prevent the user from reading a chapter that does not exist
        if (navigatorClass.chapters > navigatorClass.selected_chapter){

            // set the new chapter and adapt the layout to it
            navigatorClass.selected_chapter = navigatorClass.selected_chapter + 1;
            setLayerLayout();

            // show an animation
            view.startAnimation(animation_click);
        }

    }

    /*
    helper function for adding to favorites, done with:
     */
    public void preperationAddFavorites() {

        // will get the max verse
        Integer max_verse = 0;
        List<String> verses = new ArrayList<>();
        Cursor theCursor = sql_databse.getMaxVerse(navigatorClass.selected_book,
                                                   navigatorClass.selected_chapter);
        while (theCursor.moveToNext()) {
            max_verse = theCursor.getInt(0);
        }

        // will set a list full with numbers from the selected verse till the max verse
        for (int i = verse_text.begin_verse; i <= max_verse; i++) {
            verses.add(String.valueOf(i));
        }

        // make a list with a clicklistener and custom adapter
        row_list = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                                                                R.layout.row_custom_dialog,
                                                                R.id.list_item_text, verses);
        row_list.setAdapter(adapter);
        row_list.setOnItemClickListener(verseSelected);

        // maka a dialog with this list used in it
        addFavoritesDialog1();
    }

    /*
    will show a dialog which enables the user to select an end verse with use of
    the list row_list made in preperationAddFavorites
     */
    public void addFavoritesDialog1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        // set dialog messages
        builder.setTitle("Add to Favorties");
        builder.setMessage("till which verse do you want to add? ");

        // Specify the type of input expected
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_NUMBER);

        // set row list in the dialog
        builder.setView(row_list);
        dialog_verses = builder.create();
        dialog_verses.show();
    }

    /*
    dialog made with use of:
    https://stackoverflow.com/questions/10903754/input-text-dialog-android

    will add the text with given name in FireBase
     */
    public void addFavoritesDialog2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // set a message accordingly to which verses are selected
        builder.setMessage("Under which name do you want to add this to your Favorites?");
        String builder_title = "Selected " + navigatorClass.selected_book + " " +
                navigatorClass.selected_chapter + " verse : " +
                verse_text.begin_verse;
        if (verse_text.begin_verse != verse_text.end_verse) {
            builder_title += " - " + verse_text.end_verse;
        }
        builder.setTitle(builder_title);

        // Set up the input and specify which type of input is expected
        input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons and show the dialog
        builder.setPositiveButton("add", setSubjectListener);
        builder.setNegativeButton("cancel", setSubjectListener);
        builder.show();
    }

    /*
    function to set an on click listener to the list or a longclicklistener
    */
    public void clickListener(Boolean set) {
        if (set) {
            listview.setOnItemClickListener(new listClickListener());
            listview.setOnItemLongClickListener(null);
        } else {
            listview.setOnItemClickListener(null);
            listview.setOnItemLongClickListener(new LongClickListener());
        }
    }

    /*
    popup which allows the user to select a different translation
    allows the user to read from a different translation
     */
    public void switchTranslationDialog() {
        // set the text for the dialog and onclicklistener
        String translation_txt;
        if (translation == 0) {
            translation_txt = "KJV";
        } else {
            translation_txt = "WEB";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        builder.setMessage("do you want to switch to " + translation_txt)
                .setNeutralButton("no", switchTranslationListener)
                .setPositiveButton("yes", switchTranslationListener).show();
    }

    /*
    switches translation and changes the layout accordingly
     */
    private void switchTranslation() {
        String translation_txt;
        if (translation == 0) {
            translation = 1;
            translation_txt = "KJV";
        } else {
            translation = 0;
            translation_txt = "WEB";
        }
        Toast.makeText(UserActivity.this, "switched translation to " + translation_txt,
                       Toast.LENGTH_SHORT).show();
        if (!sql_databse.checkChapter1Existence(navigatorClass.selected_book, translation)) {
            layer = 1;
        }
        setLayerLayout();
    }

    /*
    creates a popup which asks the user if the user really wants to logout
    */
    public void logoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        builder.setMessage("Do you want to log out?")
                .setPositiveButton("Yes", logoutListener)
                .setNegativeButton("No", logoutListener).show();
    }


    /*
    will fill the list with what is currently in list_text
     */
    public void fillList() {
        // Initialize a new ArrayAdapter object from list
        CustomAdapter theAdapter = new CustomAdapter();

        // Populate the second ListView with second ArrayAdapter
        listview.setAdapter(theAdapter);
    }

    /*
    will load all books and chapters in a ordered jsonarray books_json
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
        books_json = jsonObject.getJSONObject("sections").getJSONArray("whole_bible");
    }

    /*
    will handle the listview layout and which functions need to be run
    layer 0 = choice between old and new testament
    layer 1 = choice between books of the old or new testament
    layer 2 = choice between the chapters of the selected book
    layer 3 = reading and adding to favorites of the selected chapter
    */
    public void setLayerLayout() {
        buttonLayout();
        toolbar.setSubtitle("made by David");
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
    will handle the visibility of the left and right button
    only for layer 3 the buttons need to be visible
     */
    public void buttonLayout() {
        if (layer == 3) {
            if (navigatorClass.selected_chapter >= navigatorClass.chapters) {
                rightbtn.setVisibility(View.INVISIBLE);
                leftbtn.setVisibility(View.VISIBLE);
            } else if (navigatorClass.selected_chapter <= 1) {
                leftbtn.setVisibility(View.INVISIBLE);
                rightbtn.setVisibility(View.VISIBLE);
            } else {
                leftbtn.setVisibility(View.VISIBLE);
                rightbtn.setVisibility(View.VISIBLE);
            }
        } else {
            leftbtn.setVisibility(View.INVISIBLE);
            rightbtn.setVisibility(View.INVISIBLE);
        }
    }

    /*
    will create a listview with old and new testament in it ready to select
     */
    public void layer0Layout() {
        list_text.clear();
        list_text.add("Old Testament");
        list_text.add("New Testament");
        fillList();
    }

    /*
   will fill the listview with books from the old or new testament
   from navigatorClass add factor and upper bound determine which books will be shown
   0-39 old testament books
   40-66 new testament books
    */
    public void layer1Layout() {
        list_text.clear();
        for (int i = 0; i < navigatorClass.upper_bound; i++) {
            String book_name = null;
            try {
                book_name = books_json.getJSONObject(i + navigatorClass.add_factor).getString("key");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            list_text.add(book_name);
        }
        fillList();
    }

    /*
    will show all chapters depending on the selected book
    but first it will check if the book is already in the database
    if not go to TranslateActivity to download the book
     */
    public void layer2Layout() {
        if (sql_databse.checkChapter1Existence(navigatorClass.selected_book, translation)) {

            // if it exist show all available chapters in the list and set the title to the
            // selected book
            list_text.clear();
            getSupportActionBar().setTitle(navigatorClass.selected_book);
            for (int i = 0; i < navigatorClass.chapters; i++) {

                // +1 because the chapters in the bible do not start with 0
                list_text.add(String.valueOf(i + 1));
            }
            fillList();
        } else {

            // the layer will be set to 1 to make sure the user can select the book again afther
            // downloading in translationActivity
            goToTranslation(navigatorClass.selected_book, navigatorClass.selected_book_int);
            layer = 1;
        }
    }

    /*
    gets the selected chapter from the database and presents it at the list
    if the database does not contain the information a download button will appear
    */
    public void layer3Layout() {
        list_text.clear();

        // set the title to the selcted book and chapter
        getSupportActionBar().setTitle(navigatorClass.selected_book + " " +
                                       navigatorClass.selected_chapter.toString());

        // remove the click listener and set a longclicklistener
        clickListener(false);

        // set hint to add to favorites
        toolbar.setSubtitle("long tap to add to favorites");

        // afther column 3 the translations are present so the row is calculaed this way
        Integer verse_column = 3 + translation;
        Cursor theCursor = sql_databse.getChapter(navigatorClass.selected_book,
                                                  navigatorClass.selected_chapter, translation);

        // add al verses and correspondending verse numbers to the list_text
        while (theCursor.moveToNext()) {
            String number = theCursor.getString(2);
            String verse = theCursor.getString(verse_column);
            list_text.add(number + ":  " + verse);
        }

        // will create an empty listview or full with verses
        fillList();
    }

    /*
    will set the selected verses in Firebase by setting a singleEventListener
    and set the translation property in the class
     */
    public void verseInFirebase(final ArrayList verses_list){
        verse_text.text = verses_list;
        if (translation == 0) {
            verse_text.translation = "(WEB)";
        } else {
            verse_text.translation = "(KJV)";
        }
        the_database.addListenerForSingleValueEvent(updateUser);
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
    private void goToFavorites() {
        Intent intent = new Intent(this, FavoriteActivity.class);

        // starts the new activity
        startActivity(intent);
    }

    /*
    goes to TranslationActivity with selected book and needed information to download it
    */
    private void goToTranslation(String book, Integer selected_book_int) {
        Intent intent = new Intent(this, TranslationActivity.class);
        intent.putExtra("book", book);
        intent.putExtra("book_int", selected_book_int);
        intent.putExtra("translation", translation);
        intent.putExtra("json_array", books_json.toString());

        // starts the new activity
        startActivity(intent);
        finish();
    }

    /*
    Custom adapter for seeing Listview items
    */
    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list_text.size();
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

            // set the text to what is in list_text
            TextView description_txt = convertView.findViewById(R.id.list_item);
            description_txt.setText(list_text.get(position));
            return convertView;
        }
    }

    /*
    updates the user with the new favorites
    */
    ValueEventListener updateUser = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            // get user from firebase
            FirebaseUser user = the_auth.getCurrentUser();
            to_change = dataSnapshot.child("users").child(user.getUid()).getValue(UserClass.class);

            // this boolean is used to check if the added subject already exist
            boolean found = false;

            // initialize a subjectclass and the verseslist belonging to it
            ArrayList<SubjectClass> subject_list = new ArrayList<>();
            ArrayList<VerseClass> verses_to_change = new ArrayList<>();

            // if there are subjects the subjectlist will become the users subjects
            if (to_change.subjects != null) {
                subject_list = to_change.subjects;
            }

            // check if the subject is already present if so add the verses to that subject
            for (int i = 0; i < subject_list.size(); i++) {
                if (Objects.equals(subject_list.get(i).name, subject)) {
                    verses_to_change = subject_list.get(i).verses;

                    // get which verseslist need to be changed from the subject and set found true
                    subject_list.get(i).verses = verses_to_change;
                    found = true;
                }
            }

            // add the verses to the verseslist
            verses_to_change.add(verse_text);
            if (!found) {

                // if not found make a new subject
                SubjectClass new_subject = new SubjectClass(subject, verses_to_change);
                subject_list.add(new_subject);
            }

            // set the new variables in firebase
            to_change.subjects = subject_list;
            the_database.child("users").child(user.getUid()).setValue(to_change);
        }
        @Override
        public void onCancelled(DatabaseError error) {
            // if not possible toast it
            Toast.makeText(UserActivity.this, "please check your connection",
                    Toast.LENGTH_SHORT).show();
        }
    };

    /*
    will handle the back button presses by lowering the layout each time
    */
    private class navigationBackClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            // will only lower the layer to 0
            if (layer > 0) {
                layer -= 1;
                setLayerLayout();
            }
        }
    }

    /*
     click listener for the list,
     depending on which layer is the current layer some variables are set
    */
    private class listClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            clicked_pos = position;
            switch (layer) {
                case 0:

                    // set the variables for old or new depending on which is clicked
                    if (clicked_pos == 0) {
                        navigatorClass.setOld(true);
                    } else {
                        navigatorClass.setOld(false);
                    }
                    break;
                case 1:

                    // set the selected book and the variables belonging to that book
                    navigatorClass.setSelected_book_int(clicked_pos);
                    try {
                        int selectedInt = navigatorClass.selected_book_int;
                        JSONObject selected_book_json = books_json.getJSONObject(selectedInt);
                        navigatorClass.selected_book = selected_book_json.getString("key");
                        navigatorClass.chapters = selected_book_json.getInt("val");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:

                    // set the selected chapter
                    navigatorClass.setSelected_chapter(clicked_pos);
                    break;
            }

            // set the layer one higher and set the correct layout for it
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

            // initialize the new verseclass ready for adding
            verse_text = new VerseClass(navigatorClass.selected_book,
                    navigatorClass.selected_chapter);

            // set the begin verse to the clicked verse
            verse_text.begin_verse = position + 1;

            // dialog till which verse you want to add to favorites
            preperationAddFavorites();
            return false;
        }
    }

    /*
    dialogclicklistener which will set the subject and add it to firebase
    (will add the subject with the selected verses)
     */
    DialogInterface.OnClickListener setSubjectListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int choice) {
            switch (choice) {
                case DialogInterface.BUTTON_POSITIVE:
                    ArrayList<String> verses_list = new ArrayList<String>();

                    // get the subject the user typed
                    subject = input.getText().toString();
                    Integer row = translation + 3;

                    // get the verses the cursor selected with the right row
                    Cursor theCursor = sql_databse.getVerses(verse_text);
                    while (theCursor.moveToNext()) {
                        verses_list.add(theCursor.getString(row));
                    }

                    // add the verse(s) to firebase
                    verseInFirebase(verses_list);
                    Toast.makeText(UserActivity.this, "ADDED",
                            Toast.LENGTH_SHORT).show();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    /*
    dialogclicklistener which will set end verse of the subject wich can be added to firebase
    */
    AdapterView.OnItemClickListener verseSelected =  new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Integer option_clicked = verse_text.begin_verse + position;
            verse_text.end_verse = option_clicked;

            // second dialog will enable the user to select under which subject it is added
            // to firebase
            addFavoritesDialog2();
            dialog_verses.cancel();
        }
    };

    /*
    implements an onclicklistener to check of the translation needs to be changed
    if the positive button is clicked, switch translation
    */
    DialogInterface.OnClickListener switchTranslationListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int choice) {
            switch (choice) {
                case DialogInterface.BUTTON_POSITIVE:
                    switchTranslation();
                    break;
                case DialogInterface.BUTTON_NEUTRAL:
                    break;
            }
        }
    };

    /*
    a clicklisetener which will logout the user if clicked yes
    used for the function logoutDialog
    */
    DialogInterface.OnClickListener logoutListener = new DialogInterface.OnClickListener() {
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
}






