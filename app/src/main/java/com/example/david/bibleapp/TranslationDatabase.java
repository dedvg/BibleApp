package com.example.david.bibleapp;

/**
 * Created by dedvg on 15-1-2018.
 */


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TranslationDatabase extends SQLiteOpenHelper {

    private static TranslationDatabase instance = null;
    // makes the column names
    private static final String TAG ="TRANSLATIONS";
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

    public boolean check_chapter1_existence_WEB(String book){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;
        String Query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1 + " = '" + book+ "' AND " + COL2 + "=  1" +  " AND " + COL4 + " IS NOT NULL;";
        cursor= db.rawQuery(Query,null);

        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }
    public boolean check_chapter1_existence_KJV(String book){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;
        String Query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1 + " = '" + book+ "' AND " + COL2 + "=  1 AND " + COL5 + " IS NOT NULL;";
        cursor= db.rawQuery(Query,null);

        if(cursor.getCount() == 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }



    // update the table
    public void addItem( String book, int chapter, int verse, String text, int translation) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query;
        String variable_column = COL4;

        if (translation == 1){
            variable_column = COL5;
        }

        query = "INSERT INTO " + TABLE_NAME + "(" + COL1 + ", " + COL2 + ", " + COL3 + ", " + variable_column + ") VALUES( '" + book + "', " + chapter + ", " + verse + ", '" + text + "');";

        db.execSQL(query);
    }

    public Cursor get_max_verse(String book, int chapter, int verse, int translation){
        SQLiteDatabase db = this.getWritableDatabase();
        String query;


        query = "SELECT MAX (" + COL3 + ") FROM " + TABLE_NAME + " WHERE " + COL1 + " = '" + book+ "' AND " + COL2 + "= " + chapter + " ;";
        Cursor entries = db.rawQuery(query, null);
        return entries;
    }

    public Cursor get_verses (String book, int chapter, int begin_verse, int end_verse, int translation){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1 + " = '" + book+ "' AND " + COL3 + " BETWEEN " + begin_verse + " AND " + end_verse + " AND " + COL2 + " = " + chapter + ";";
        Cursor entries = db.rawQuery(query, null);
        return entries;
    }
    // get the whole table
    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor entries = db.rawQuery(query, null);
        return entries;
    }

    public Cursor getchapter(String book, Integer chapter, Integer translation) {

        String variable_column = COL4;

        if (translation == 1){
            variable_column = COL5;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1 + " = '" +  book + "' AND " + COL2 + " = " + chapter + " AND " + variable_column + " IS NOT NULL;";
        System.out.println(query);
        Cursor entries = db.rawQuery(query, null);
        return entries;
    }





    // delete the whole table
    public void clear()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String query ="DELETE FROM " + TABLE_NAME +";";
        db.execSQL(query);
    }




}