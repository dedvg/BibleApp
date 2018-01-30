package com.example.david.bibleapp;

/*
each subject has a name and the belonging list of verses in it
this is used for the favorites of the user in firebase
 */
import java.util.ArrayList;



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