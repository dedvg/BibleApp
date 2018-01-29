package com.example.david.bibleapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dedvg on 23-1-2018.
 */

public class SubjectClass {

    public ArrayList<VerseClass> verses;
    public String name;

    public SubjectClass() {
    }

    public SubjectClass(String Name, ArrayList<VerseClass> Verses) {
        this.verses = Verses;
        this.name =Name;

    }


}