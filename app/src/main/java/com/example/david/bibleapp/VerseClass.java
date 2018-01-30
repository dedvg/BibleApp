package com.example.david.bibleapp;

import java.util.ArrayList;

/**
 * Created by dedvg on 23-1-2018.
 */



public class VerseClass {
    public ArrayList<String> text;
    public String book;
    public Integer begin_verse;
    public Integer end_verse;
    public Integer chapter;
    public String translation;


    // set default constructor needed for firebase
    public VerseClass() {
    }

    public VerseClass (String Book, Integer Chapter, Integer Begin_Verse, Integer End_Verse ) {
        this.text = null;
        this.chapter = Chapter;
        this.begin_verse = Begin_Verse;
        this.end_verse = End_Verse;
        this.book = Book;
        this.translation ="";
    }
}