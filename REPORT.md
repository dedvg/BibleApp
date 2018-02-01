### Report ###

made by David van Grinsven


___Short App description___

This app alows the user to read the Bible and add verses to his Favorites beneath a chosen name.
By logging in the user can acces this everywhere. The user is also able to switch between King James Version and World English Bible.
  ![screenshot](/doc/screenshot%20(6).png)
  
___technical design___

The app contains 5 Activities

_MainActivity_

This Activity allows the user to login with use of an email and password.
If the user already logged in previously, the user will be redirected to UserActivity.
If the user has no account the user can click on the text:
"click here if you have no account"
And if clicked the user will be redirected to RegisterActivity to register.

* login with use of firebase

_RegisterActivity_

This Activity will allow the user to register a new account or go back to MainActivity.

* register with use of firebase
* the user is instantly saved in the database(Firebase) with use of a UserClass which is mostly empty at start


_TranslationActivity_

This Activity allows the user to download a book they do not own yet.
The user is able to go back to UserActivity with use of the back button provided,
is able to switch translation and if needed logout.

* will volley the bible book chapter by chapter with use of to https://bible-api.com/ 
* a list of ChapterClasses is used to store the bible book in a local SQL database

_FavoriteActivity_

This Activity allows the user to read their subjects they have added.
By clicking on a subject the belonging verses will be shown and from where the verses are (book, chapter, begin verse, end verse).
The user is able go back to UserActivity to continue reading.

 * use of a custom adapter to set the list layout
 * use of a database from Firebase
 * use of classes to navigate trough subjects and delete parts of a subject or a subject from the database(Firebase)

_UserActivity_

This Activity is the Main Activity the user will use.
This activity allows the user to logout, switch translation, read the bible (if already downloaded),
and navigate to favorites.
The user will first need to select old or new testament, then a biblebook and then the chapter.
When reading a chapter the user can navigate to the next chapter and previous chapter if available,
with use of the provided buttons beside the list in the middle of the Activity.
When long tapping a verse the user can add a selection of verses from that chapter to their
favorites with a name they can fill in.

* all book names and the amount of chapers each bible book has are stored in a local json
* use of a custom adapter to set the list layout
* use of a NavigationClass make navigating from book to book easier
* when adding something to favorites the UserClass is taken from Firebase, changed and set back. With use of multiple classes:
    * UserClass: a class with a username and a list of SubjectClasses used to store all the user favorites
    * SubjectClass : has an name and a list of VerseClasses, used to store the selected verses beneath one name.
    * VerseClass: Contains the book name from which it was added, the translation, chapter, the begin verse, end verse and a List of strings containing the verses from begin verse till end verse of the book and chapter.
* use of a local SQL database is used to
    * check if a book is already present in the selected translation
    * read the chapter
    * get the highest verse a chapter has


___changes and challenges___

_changes_

* the layout had minor changes due to extra icons not being needed anymore
* http://www.online-bijbel.nl/ontwikkelaars/ has not been used due to a time shortage and https://bible-api.com/ already containing multiple english tranlations of the bible.
* dialogs are used for multiple functions (like: logging out, adding to favorites ect.)
* another activity is made to download a book (TranslationActivity)
* animations are used for clicking the arrow buttons while reading a chapter
* Instead of only using an api to read a chapter use of a SQL database is made so the user can read a book offline.

Due to the API only sending a chapter at most it was not possible to volley the complete bible. Also the api had no list with bible books that are present. So to know which books are able to be read a solution was needed. When saving multiple verses just a SubjectClass was not enough to store them so another Class was needed. The standard listview layout was not a nice way of reading the bible. This gave the following changes:

* instead of just downloading a chapter a whole book is dowloaded, because downloading each chapter seperatly will take a long time for the user. 
* use of a local json is used to get all book names and amount of chapters they have
* extra classes are made to safe Favorites in Firebase and to navigate better
* Custom Adapters are made to change the layout for the listviews that are used

_trade-offs of these choices_

* the user can not download the whole bible at once
* the user is not able to select more than 2 translations
* the user can not read the bible in dutch

Given more time was available the volley would be devided in multiple parts enabling the user to download the hole bible at once or another API could have been used. Adding more translations would not be hard with more time. With the current API the database would need one colum and each statement with translation in it would have to be changed a bit. Given more time multiple API sources could have been added.

_Challenges_

* get the layout right for reading the bible, each part (selecting old or new testament, selecting a book, selecting a chapter) needed another layout and separate functions
* use of a local SQL database and firebase together created some issues
* switching translation
* downloading the bible and saving it in SQL
* use of a local json
