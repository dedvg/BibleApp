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
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + COL1 + " TEXT, " + COL2 + " INT, " + COL3 + " INT," + COL4 + " TEXT);" ;
        db.execSQL(createTable);

    }
    // make the on upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }

    public boolean check_chapter_existence_WEB(String book, int chapter){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;
        String Query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1 + " = '" + book+ "' AND " + COL2 + "= " + chapter +  ";";
        cursor= db.rawQuery(Query,null);

        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }



    // update the table
    public void addItem( String book, int chapter, int verse, String text) {
        SQLiteDatabase db = this.getWritableDatabase();
        System.out.println("hier komt hij ook");
        String query;


            query = "INSERT INTO " + TABLE_NAME + "(" + COL1 + ", " + COL2 + ", " + COL3 + ", " + COL4 + ") VALUES( '" + book + "', " + chapter + ", " + verse + ", '" + text + "');";

        System.out.println(query);
        db.execSQL(query);
    }


    // get the whole table
    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String all = "SELECT * FROM " + TABLE_NAME;
        Cursor entries = db.rawQuery(all, null);
        return entries;
    }

    // delete all from one item
    public void deleteItem(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + COL1 + " = '" +  id + "';";
        System.out.println(query);
        db.execSQL(query);
    }



    // delete the whole table
    public void clear()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String query ="DELETE FROM " + TABLE_NAME +";";
        db.execSQL(query);
    }
    // calculate the total price
    public String totalPrice() {
        int priceCol = 2;
        int amountCol = 3;
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        float total = 0;
        while (cursor.moveToNext()) {
            total = total + cursor.getFloat(priceCol) * cursor.getFloat(amountCol);
        }
        return "€ " + total;
    }



}