package com.example.david.bibleapp;

/*
This will handle the SQL database behind the app.
All text which is downloaded is stored in this database and read from this database.
 */


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TranslationDatabase extends SQLiteOpenHelper {

    private static TranslationDatabase instance = null;
    private static final String TABLE_NAME ="Bible_translations";
    private static final String COL1 ="book_name";
    private static final String COL2 ="chapter";
    private static final String COL3 ="verse";
    private static final String COL4 ="translation1";
    private static final String COL5 ="translation2";


    private TranslationDatabase(Context context) {
        super(context, TABLE_NAME, null, 1);

    }
    public static TranslationDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new TranslationDatabase(context);
        }
        return instance;
    }
    // create the table on create
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + COL1 + " TEXT, " + COL2 + " INT, " + COL3 + " INT," + COL4 + " TEXT," + COL5 + " TEXT);" ;
        db.execSQL(createTable);

    }
    // make the on upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }


    /*
    will check if chapter one is present of the translation
     */
    public boolean checkChapter1Existence(String book, Integer translation){
        SQLiteDatabase db = this.getWritableDatabase();
        String variable_column = COL4;
        if (translation == 1){
            variable_column = COL5;
        }

        Cursor cursor;
        String Query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1 + " = '" + book+ "' AND " + COL2 + "=  1 AND " + variable_column + " IS NOT NULL;";
        cursor= db.rawQuery(Query,null);

        if(cursor.getCount() == 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }



    /*
    add verses to the table depending whether the columns already exist or not
    if both col4 and col 5 are present the table needs to be updated instead of insert
     */
    public void addItem( String book, int chapter, int verse, String text, int translation) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query;

        String variable_column = COL4;

        if (translation == 1){
            variable_column = COL5;
        }

        if (checkChapter1Existence(book, 1) && checkChapter1Existence(book, 0))
        {
            query = "UPDATE " + TABLE_NAME + " SET " + variable_column + " = '" + text + "' WHERE " + COL1 + " = '" + book+ "' AND " + COL2 + " = " + chapter + " AND " + COL3 + " = " + verse +  ";";
        }
        else{
            query = "INSERT INTO " + TABLE_NAME + "(" + COL1 + ", " + COL2 + ", " + COL3 + ", " + variable_column + ") VALUES( '" + book + "', " + chapter + ", " + verse + ", '" + text + "');";
        }
        db.execSQL(query);
    }
    /*
    returns the max verse that is possible used to add verses to favorites
     */
    public Cursor get_max_verse(String book, int chapter){
        SQLiteDatabase db = this.getWritableDatabase();
        String query;

        query = "SELECT MAX (" + COL3 + ") FROM " + TABLE_NAME + " WHERE " + COL1 + " = '" + book+ "' AND " + COL2 + "= " + chapter + " ;";
        return db.rawQuery(query, null);
    }




    public Cursor getChapter(String book, Integer chapter, Integer translation) {

        String variable_column = COL4;

        if (translation == 1){
            variable_column = COL5;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1 + " = '" +  book + "' AND " + COL2 + " = " + chapter + " AND " + variable_column + " IS NOT NULL;";
        Cursor entries = db.rawQuery(query, null);
        return entries;
    }

    /*
    return a cursor to enable the user to select which translation to read from certain verses
    used to add verses to favorites
   */
    public Cursor getVerses(VerseClass verse_class) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1 + " = '" + verse_class.book + "' AND " + COL3 + " BETWEEN " + verse_class.begin_verse + " AND " + verse_class.end_verse + " AND " + COL2 + " = " + verse_class.chapter + ";";
        Log.d("query", query);

        return db.rawQuery(query, null);
    }
}