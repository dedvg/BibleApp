package com.example.david.bibleapp;

/*
By David van Grinsven, Minor Programmeren, 2018, UvA

This class had a username
and a ArrayList of subjects to store the favorites of the user in firebase.
 */

import java.util.ArrayList;

public class UserClass {

    public ArrayList<SubjectClass> subjects;
    public String username;

    // set default constructor needed for firebase
    public UserClass() {
    }

    public UserClass(String username, ArrayList<SubjectClass> Subjects) {
        this.subjects = Subjects;
        this.username = username;
    }
}