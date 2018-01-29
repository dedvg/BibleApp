package com.example.david.bibleapp;

import java.util.ArrayList;
import java.util.List;

/*
each subject has a name and verses in it

so a SubjectClass has its name and a list of verses
 */

public class SubjectClass {

    public ArrayList<VerseClass> verses;
    public String name;

    // empty constructor for firebase
    public SubjectClass() {
    }

    public SubjectClass(String Name, ArrayList<VerseClass> Verses) {
        this.verses = Verses;
        this.name = Name;

    }


}