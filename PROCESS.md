# BibleApp
David van Grinsven


# day 1 (15-1-18)

__decicions__
I decided to enable the user to safe a bible translation in SQL. 
Tried to download the whole chapter to SQL to prevent internet a next time reading that chapter.
However with multiple translations this will get messy soon. 
I decided to enable the user to select a translation (download it to SQL). The user will only be able to read a translation if the translation is already downloaded.
This way no volley is needed each time the user wants to read the bible.
To do this another activity is made, this will give cleaner code and is easy.

__progress:__
* One chapter is downloadable 
* the SQL basis is made to enable safing the translation (one for now)



# day 2(16-1-18)

__decicions__

__progress__
* new activity is made
* One biblebook is downloadable with a volley


# day 3 (17-1-18)
__decicions__
* Decided to enable the user to download book by book instead of by bible translation due to insufficient memmory errors when trying.

__progress__
* Made check functions to check whether a translation is present in the database.
* Made a button to download the book when the book is not yet downloaded.
* Started commenting the functions

# day 4 (18-1-18)
__decicions__
* When a book or chapter is not present in the database the user will see a download button and brings the user to another activity to download it.
__progress__
* Tried to make a listview with buttons but did not work
* Enabled passing on the clicked biblebook and chapter to the new activity.


# day 5 (19-1-2018)
__decicions__

__progress__
* Made check functions to check whether a translation is present in the database.
* Made a button to download the book when the book is not yet downloaded.
* Started commenting the functions

# day 6 (22-1-2018)
__decicions__
Thinking about changing translationActivity to an activity where you can only download the seleced book from UserActivity.
This is due to selecting a book in UserActivity which is not present in the database will bring the user to the corect book to download in TranslateActivity. But not yet sure because if the user wants to download multiple translations of the same book this function can still be usefull (which translationActivity currently has).

__progress__
* commented UserActivity
* made navigation with back button easier
* deletion of testing code
* enabling of selecting multiple verses

# day 7 (23-1-2018)
__decicions__
* __BUGG__ when adding a translation new rows are made, it is supposed to be in the same row this needs to be fixed
* will remove functions from translation activity, navigating which book to download will only be available in UserActivity
    * to do so UserActivity needs to pass on which translation is used and TranslationActivity will change accordingly
    * only one button and textfield are neccesary for downloading independent of the translation
    
__progress__
* will update the table if another translation is present instead of always inserting in sql.
* switching from translation is now possible in TranslationActivity
      * __BUGG__ when switching in the list, it will crash. The list part will probally be removed though.

# day 8 (24-1-2018)
__decicions__
* removing of the navigation to TranslateActivity, when a book is not present in the selected translation the user is immeadiatelly brought to TranslateActivity to download the book instead of first selecting chapters.
* when deleting the last verses of a subject, the whole subject will be deleted from firebase and from the users favorites
* need to prevent users from filling an empty subject name
* for Favorites a Custom adapter is made so users can see
   * which book they added
   * which translation the verses are from
   * begin verse and end verse that where added
    
__progress__
* Multiple classes are made to enable saving verses to a subject in Firebase
* Check statements are present to check whether the subject already exist and if so insert it there
* Favorites is now working and readable


# day 9 (25-1-2018)
__decicions__
* will try to change messages so the current translation is not an option
* will try to make a class from the navigation part in useractivity
* the translation should be passed on from activity to activity

__progress__
* extra functions in TranslateActivity are deleted
* If the book is not present the user does not have to select a chapter and click download first to go to TranslateActivity. This will go automatically
* navigation class is made
* translation is passed on in each activity

# day 10 (26 -1-2018)
__decicions__
* __BUGGS__
      * downloading a book can happen half
      * favorites should not be possible when no connection
      

__progress__
* left and right buttons are made for navigating chapters
* layout of the list in UserActivity is changed

# day 11 (29 -1-2018)
problems with custom adapters


__decicions__
* made a new class to enable downloading a book
* will make  a progressbar to see how far the download is (especially needed for psalms)
* __BUGGS__
      * loading can take forever
      * if you click fast on the button for the next chapter you can get an empty chapter
      

__progress__
* the data will be added at once enabling preventing the data from being added if the book is downloaded half (second part not yet implemented)
* more comments
* better loading bar (but improvements are needed)
* removed some not needed functions

# day 12 (30 -1-2018)
__decicions__

      

__progress__
* prevented books with a number in front of it lik 1 corintians from crashing
* commented some more code
* chapters where not in the right order, this is fixed now
* worked on getting better code hub to 6 instead of 5

