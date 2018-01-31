### Report ###

made by David van Grinsven


___Short App description___

This app alows the user to read the Bible and add verses to his Favorites beneath a chosen name.
By logging in the user can acces this everywhere. The user is also able to switch betweek King James Version and World English Bible.
  ![screenshot](/doc/screenshot%20(6).png)
  
___technical design___

The app contains 5 Activities

_MainActivity_

This Activity allows the user to login with use of an email and password.
If the user already logged in previously, the user will be redirected to UserActivity.
If the user has no account the user can click on the text:
"click here if you have no account"
And if clicked the user will be redirected to RegisterActivity to register.

Related classes and notable features:

* login with use of firebase

_RegisterActivity_

This Activity will allow the user to register a new account or go back to MainActivity.

Related classes and notable features:

* register with use of firebase
* the user is instantly saved in the database with use of a UserClass which is mostly empty at start


_TranslationActivity_

This Activity allows the user to download a book they do not own yet.
The user is able to go back to UserActivity with use of the back button provided,
is able to switch translation and if needed logout.

Related classes and notable features:

* will volley the bible book chapter by chapter with use of to https://bible-api.com/ 
* a list of ChapterClasses is used to store the bible book in a local SQL database

_FavoriteActivity_

This Activity allows the user to read their subjects they have made.
By clicking on a subject the added verses will be shown and which translation belongs to them.
The user is able to logout and to go back to UserActivity to continue reading

Related classes and notable features:

 * use of a custom adapter to set the list layout
 * use of firebase 

_UserActivity_

This Activity is the Main Activity the user will use.
This activity allows the user to logout, switch translation, read the bible (if already downloaded),
and navigate to favorites.
The user will first need to select old or new testament, then a biblebook and then the chapter.
When reading a chapter the user can navigate to the next chapter and previous chapter if available
with use of the provided buttons beside the list in the middle of the Activity.
When long tapping a verse the user can add a selection of verses from that chapter to their
favorites with a name they can fill in.

Related classes and notable features:

* all book names and the amount of chapers each bible book has are stored in a local json
* use of a custom adapter to set the list layout
* use of a NavigationClass make reading a book easier.
* when adding something to favorites the UserClass is taken from Firebase, changed and set back. With use of multiple classes:
    * UserClass: a class with a username and a list of SubjectClasses used to store all the user favorites
    * SubjectClass : has an name and a list of VerseClasses, used to store the selected verses beneath one name.
    * VerseClass: Contains the book name from which it was added, the translation, chapter, the begin verse, end verse and a List of strings containing the verses from begin verse till end verse of the book and chapter.
* use of a local SQL database is used to
    * check if a book is already present in the selected translation
    * read the chapter
