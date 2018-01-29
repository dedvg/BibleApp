package com.example.david.bibleapp;

/**
 *
 */



public class NavigationClass {
    // book name
    String selected_book;
    Integer layer;

    // book number from the 66 books
    Integer selected_book_int;

    // amount of chapters the book has
    Integer chapters;

    // chapter number
    Integer selected_chapter;

    // used for showing books from old or new testament
    Integer add_factor;
    Integer upper_bound;

    // checks if old or new testament is clicked
    Boolean old;


    // set default constructor needed for firebase
    public NavigationClass() {
    }

    /*
    add factor and upper bound determine which books need to be shown

    old testament = 0 -39 (39 books so upper bound is 39 and start 0)
    new testament = 40 - 66 (27 books so upper bound is 27 and start 39)
     */
    public void setOld(Boolean old) {
        if (old){
            add_factor = 0;
            upper_bound = 39;
        }
        else{
            add_factor = 39;
            upper_bound = 27;
        }
        this.old = old;
    }

    // determine the position of the book with a correction factor if needed
    public void setSelected_book_int(Integer clicked_pos) {
        this.selected_book_int = clicked_pos + this.add_factor;
    }

    // set the selected chapter
    public void setSelected_chapter(Integer clicked_pos) {
        Integer temp = clicked_pos + 1;
        this.selected_chapter = temp;
    }


}