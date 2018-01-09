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

  a.	Returns the user to MainActivity
  
  b.	Allows the user to select which book and chapter
  
  c.	Goes to FavoriteActivity
  
  d.	With use of the Bible API https://bible-api.com/ a listview will be filled.
  
__4.	UserActivity__

  a.	A Popup that allows the long tapped text to be added to a subject.
  
__5.	FavoriteActivity__

Navigate trough favorites and delete from it.

  a.	Go back to the Text you came from
  
  b.	Firebase list results (From the UserClass)
  
__6.	See 5__

__7.	FavoriteActivity__

  a.	Go back to the Subjects
  
  b.	Firebase list results (From the UserClass)
  


