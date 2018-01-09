# BibleApp
David van Grinsven

### Data sources
* Firebase
* APIs
  * https://bible-api.com/
  * http://www.online-bijbel.nl/ontwikkelaars/

### Advanced sketch
  ![draft](/doc/draft1-page-001.jpg)
__1.	MainActivity__

Allows the user to login. And will keep track if the divice is already logged in.

  a.	Authentication by Firebase
  
  b.	Makes a new Intent to register
  
__2.	RegisterActivity__

Allows the user to register. And log in with the given information.

  a.	Registration by Firebase
  
__3.	UserActivity__

Allows the user to read the Bible, add verses to his/her favorites and allows the user to add and delete subjects.
With use of a listview the Bible will be readable.
Navigating trough the bible and adding to favorites will be done with use of popups with a listview if needed.
Firebase will be updated if a verse is added.

  a.	Returns the user to MainActivity
  
  b.	Allows the user to select which book and chapter
  
  c.	Goes to FavoriteActivity
  
  d.	With use of the Bible API https://bible-api.com/ a listview will be filled.
  
__4.	UserActivity__

  a.	A Popup that allows the long tapped text to be added to a subject.
  
__5.	FavoriteActivity__

Navigate trough favorites and delete from it. This will be done by retrieving information belonging to the current user in firebase.

  a.	Go back to the Text you came from
  
  b.	Firebase list results (From the UserClass)
  
__6.	See 5__

__7.	FavoriteActivity__

  a.	Go back to the Subjects
  
  b.	Firebase list results (From the UserClass)
  
### Functions
__MainActivity__
* Check_Input: 
Will check if the username and password are valid. (If they look like an email and password), if valid Check_User will be executed.
* Check_User:
Will check if the user exists in Firebase, if the user exists go to the UserActivity
*Register:
Will go to a new Activity (RegisterActivity)

__RegisterActivity__
* Check_Input: 
Will check if the username and password are valid. (If they look like an email and password), if valid Register_User will be executed.
* Register_User:
Will register the user in firebase.

__UserActivity__
* Log_Out:
Logs out the user
* Get suggestions_names:
Will enable the user to select the wanted book.
* Get_chapters:
Will enable the user to select which chapter.
* Get Text:
Get text from the api, and fill the listview.
* Go_to_favorites:
Will go to the FavoriteActivity of the current user.
* Longtapped:
Will keep track of the verse number which is long tapped and will start Get Favorites
* Get Favorites:
Will get the user Favorite subjects to enable selecting one.
* Add to Favorites:
Adds the long tapped verse to the selected subject.

__FavoriteActivity__
* Log_Out:
Logs out the user
* Get_Favorites:
Will get the user Favorite subjects to enable selecting one.
* Get_texts:
Will get the verses belonging to the subject and fill a listview with them. 
* Back
will send the user back to the subjects or to the UserActivity
### Database Structure
The user will have a list of SubjectClasses


SubjectClass| 
------------ | 
name|
Verses[] | 

