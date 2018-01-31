package com.example.david.bibleapp;

import java.util.ArrayList;

/*
When a selection of verses is added to the user Favorites this class is used.
It saves all the needed information to read it in FavoriteActivity.
*/

public class VerseClass {
    public ArrayList<String> text;
    public String book;
    public Integer begin_verse;
    public Integer end_verse;
    public Integer chapter;
    public String translation;
    
    // set default constructor needed for Firebase
    public VerseClass() {
    }

    public VerseClass (String Book, Integer Chapter) {
        this.text = null;
        this.chapter = Chapter;
        this.begin_verse = null;
        this.end_verse = null;
        this.book = Book;
        this.translation ="";
    }
}