package com.example.david.bibleapp;

import java.util.ArrayList;

/**
 * Created by dedvg on 29-1-2018.
 * will enable saving a whole book
 */

public class ChapterClass {

    public ArrayList<String> verses;
    public Integer chapter;


    public ChapterClass() {
    }

    public ChapterClass(Integer Chapter, ArrayList<String> Verses) {
        this.verses = Verses;
        this.chapter = Chapter;

    }


}