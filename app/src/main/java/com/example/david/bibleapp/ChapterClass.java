/*
 with this Class all chapters of a book can be added as a list of ChapterClasses
*/
package com.example.david.bibleapp;

import java.util.ArrayList;

/*
each chapter has a number and verses
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