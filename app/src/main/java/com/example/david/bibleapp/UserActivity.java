package com.example.david.bibleapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
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
    ListView listView, row_list;
    List<String> list_text = new ArrayList<String>();
    Integer layer = 0, translation = 0, clicked_pos;
    JSONArray BOOKSjson;
    TranslationDatabase theDatabase;
    AlertDialog dialog_verses;
    FirebaseAuth the_auth;
    UserClass to_change;
    String subject;
    DatabaseReference the_databse;
    NavigationClass navigatorClass = new NavigationClass();
    Button leftbtn, rightbtn;
    private Animation animation_click;
    EditText input;
    VerseClass verse_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Intent intent = getIntent();
        translation = intent.getIntExtra("translation", 0);
        // set animation
        animation_click = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        // create references
        toolbar = findViewById(R.id.toolbar);
        listView = findViewById(R.id.listView);
        theDatabase = TranslationDatabase.getInstance(this.getApplicationContext());
        the_auth = FirebaseAuth.getInstance();
        the_databse = FirebaseDatabase.getInstance().getReference();
        leftbtn = findViewById(R.id.leftBTN);
        rightbtn = findViewById(R.id.rightBTN);
        // set up the action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bible App");
        toolbar.setSubtitle("made by David");

        // add backbutton to the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /*
        Determines what happens when the back button is clicked.
        If the back button is pressed when the layer is 1 it will ask the user to logout.
         */
        toolbar.setNavigationOnClickListener(new navigationBackClicked());

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
                switchTranslationDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    will handle the leftbutton on click events
    sets the previous chapter as readable
     */
    public void previousChapter(View view) {
        navigatorClass.selected_chapter = navigatorClass.selected_chapter - 1;
        setLayerLayout();
        view.startAnimation(animation_click);
    }

    /*
    will handle the rightbutton on click events
    set the next chapter as readable
    */
    public void nextChapter(View view) {
        navigatorClass.selected_chapter = navigatorClass.selected_chapter + 1;
        setLayerLayout();
        view.startAnimation(animation_click);
    }

    /* factor and upper bound determine which books need to be sho
    will create a clicklistener on the listview
    it keeps track of the layer and changes variables to make navigation possible

    addwn

    old testament = 0 -39
    new testament = 40 - 66

     */

    private class ClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            clicked_pos = position;
            switch (layer) {
                case 0:
                    if (clicked_pos == 0) {
                        navigatorClass.setOld(true);
                    } else {
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
            Toast.makeText(UserActivity.this, "add " + navigatorClass.selected_book + " " + navigatorClass.selected_chapter.toString() + ":  " + verse.toString(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /*
    test dialog made with use of https://stackoverflow.com/questions/10903754/input-text-dialog-android
     */
    public void dialogAddFavorites1() {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choice) {
                switch (choice) {
                    case DialogInterface.BUTTON_POSITIVE:
                        ArrayList<String> verses_list = new ArrayList<String>();
                        subject = input.getText().toString();
                        Integer row = translation + 3;
                        Cursor theCursor = theDatabase.getVerses(verse_text);
                        while (theCursor.moveToNext()) {
                            verses_list.add(theCursor.getString(row));
                        }
                        verseInFirebase(verses_list);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Under which name do you want to add this to firebase?");

        // set a message accordingly to which verses are selected
        String builder_title = "Selected " + navigatorClass.selected_book + " " + navigatorClass.selected_chapter + " verse : " + verse_text.begin_verse;
        if (verse_text.begin_verse != verse_text.end_verse) {
            builder_title += " - " + verse_text.end_verse;
        }
        builder.setTitle(builder_title);

        // Set up the input and specify which type of input is expected
        input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons and show the dialog
        builder.setPositiveButton("add", dialogClickListener);
        builder.setNegativeButton("cancel", dialogClickListener);
        builder.show();
    }
    /*
    helper function for adding to favorites, done with:
    https://www.youtube.com/watch?v=0gTXUHDz6BM
     */

    public void dialogFavorites(final Integer verse) {
        Integer max_verse = 0;
        List<String> verses = new ArrayList<>();
        Cursor theCursor = theDatabase.get_max_verse(navigatorClass.selected_book, navigatorClass.selected_chapter);
        while (theCursor.moveToNext()) {
            max_verse = theCursor.getInt(0);
            System.out.println(max_verse.toString());
        }
        for (int i = verse; i <= max_verse; i++) {
            verses.add(String.valueOf(i));
        }
        row_list = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_custom_dialog, R.id.list_item_text, verses);
        row_list.setAdapter(adapter);
        row_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Integer option_clicked = verse + position;
                verse_text = new VerseClass(navigatorClass.selected_book, navigatorClass.selected_chapter, verse, option_clicked);
                dialogAddFavorites1();
                dialog_verses.cancel();
            }
        });
        dialogFavorites2();
    }

    /*
    actually showing the dialog
     */
    public void dialogFavorites2() {
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
    public void clickListener(Boolean set) {
        if (set == true) {
            listView.setOnItemClickListener(new ClickListener());
            listView.setOnItemLongClickListener(null);
        } else {
            listView.setOnItemClickListener(null);
            listView.setOnItemLongClickListener(new LongClickListener());
        }
    }

    /*
    implements an onclicklistener to check of the translation needs to be changed
     */
    DialogInterface.OnClickListener translationSwitchListener = new DialogInterface.OnClickListener() {
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
    popup which allows the user to select a different translation
    allows the user to read from a different translation
     */
    public void switchTranslationDialog() {

        String translation_txt;
        if (translation == 0) {
            translation_txt = "KJV";
        } else {
            translation_txt = "WEB";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        builder.setMessage("do you want to switch to " + translation_txt)
                .setNeutralButton("no", translationSwitchListener)
                .setPositiveButton("yes", translationSwitchListener).show();
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
        Toast.makeText(UserActivity.this, translation_txt + " selected", Toast.LENGTH_SHORT).show();
        toolbar.setSubtitle(translation_txt);
        if (!checkBookExistence()) {
            layer = 1;
        }
        setLayerLayout();
    }

    /*
    creates a popup which asks the user if the user really wants to logout
    */
    public void logoutMsg() {
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

            TextView description_txt = convertView.findViewById(R.id.list_item);
            description_txt.setText(list_text.get(position));
            return convertView;
        }
    }

    /*
    will fill the list with what is currently in list_text
     */

    public void fillList() {
//        ArrayAdapter theAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list_text);
//        listView.setAdapter(theAdapter);


        // Initialize a new ArrayAdapter object from list
        CustomAdapter theAdapter = new CustomAdapter();

        // Populate the second ListView with second ArrayAdapter
        listView.setAdapter(theAdapter);

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
    public void setLayerLayout() {
        listView.setDividerHeight(1);
        buttonLayout();
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
    will create a listview with old and new
     */
    public void layer0Layout() {
        list_text.clear();
        list_text.add("Old");
        list_text.add("New");
        fillList();
    }

    /*
   will fill the listview with books from the old or new testament

   add factor and upper bound determine which books will be shown

   0-39 old testament books

   40-66 new testament books
    */
    public void layer1Layout() {
        list_text.clear();
        for (int i = 0; i < navigatorClass.upper_bound; i++) {
            String book_name = null;
            try {
                book_name = BOOKSjson.getJSONObject(i + navigatorClass.add_factor).getString("key");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            list_text.add(book_name);
        }
        fillList();
    }

    /*
    will show all chapters depending on the selected book
     */
    public void layer2Layout() {
        if (checkBookExistence()) {
            list_text.clear();
            getSupportActionBar().setTitle(navigatorClass.selected_book);
            for (int i = 0; i < navigatorClass.chapters; i++) {

                // +1 because the chapters in the bible do not start with 0
                list_text.add(String.valueOf(i + 1));
            }
            fillList();
        } else {
            go_to_translation(navigatorClass.selected_book, navigatorClass.selected_book_int);
            layer = 1;
        }
    }

    /*
    gets the selected chapter from the database and presents it at the list
    if the database does not contain the information a download button will appear
    */
    public void layer3Layout() {
        getSupportActionBar().setTitle(navigatorClass.selected_book + " " + navigatorClass.selected_chapter.toString());
        clickListener(false);
        toolbar.setSubtitle("long tap to add to favorites");
        // clear the listview
        list_text.clear();

        // afther column 3 the translations are present
        Integer verse_column = 3 + translation;
        Cursor theCursor = theDatabase.getChapter(navigatorClass.selected_book, navigatorClass.selected_chapter, translation);

        // add al verses and correspondending verse numbers to the list_text
        while (theCursor.moveToNext()) {
            String number = theCursor.getString(2);
            String verse = theCursor.getString(verse_column);

            list_text.add(number + ":  " + verse);
        }

        // will create an empty listview or full with verses
        fillList();
    }

    public boolean checkBookExistence() {
        Cursor theCursor = theDatabase.getChapter(navigatorClass.selected_book, 1, translation);
        Integer rows = theCursor.getCount();
        if (rows >= 1) {
            return true;
        }
        return false;
    }

    ValueEventListener updateUser = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            FirebaseUser user = the_auth.getCurrentUser();
            to_change = dataSnapshot.child("users").child(user.getUid()).getValue(UserClass.class);
            boolean found = false;
            // get the userclass from firebase (from the current user)
            ArrayList<SubjectClass> subject_list = new ArrayList<SubjectClass>();
            ArrayList<VerseClass> verses_to_change = new ArrayList<VerseClass>();
            // prevent it from being null
            if (to_change.subjects == null) {
                subject_list = new ArrayList<SubjectClass>();
            } else {
                subject_list = to_change.subjects;
            }
            for (int i = 0; i < subject_list.size(); i++) {
                if (Objects.equals(subject_list.get(i).name, subject)) {
                    verses_to_change = subject_list.get(i).verses;
                    subject_list.get(i).verses = verses_to_change;
                    found = true;
                }
            }
            verses_to_change.add(verse_text);
            if (!found) {
                SubjectClass new_subject = new SubjectClass(subject, verses_to_change);
                subject_list.add(new_subject);
            }
            to_change.subjects = subject_list;
            the_databse.child("users").child(user.getUid()).setValue(to_change);
        }
        @Override
        public void onCancelled(DatabaseError error) {
            // if not possible toast it
            Toast.makeText(UserActivity.this, "please check your connection",
                    Toast.LENGTH_SHORT).show();
        }
    };



    public void verseInFirebase(final ArrayList verses_list){
        verse_text.text = verses_list;
        if (translation == 0) {
            verse_text.translation = "(WEB)";
        } else {
            verse_text.translation = "(KJV)";
        }
        the_databse.addListenerForSingleValueEvent(updateUser);
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


        System.out.println("before : " + book + selected_book_int);
        // starts the new activity
        startActivity(intent);
        finish();
    }

    private class navigationBackClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (layer > 0) {
                layer -= 1;
                setLayerLayout();
                clickListener(true);
            }
        }
    }
}






