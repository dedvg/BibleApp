package com.example.david.bibleapp;

/**
 */

import java.util.ArrayList;
import java.util.List;

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