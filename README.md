# BibleApp
David van Grinsven

### Problem statement
When using a bible app, you can ofthen mark a few verses or favorite them.
However when two verses are talking about the same subject it is hard to link them.

Currently there are bible apps for example: Bijbel - Statenvertaling.
But they lack this.
Bible is another app, it has the functions but not orderly and usefull for study purposes.
However this app has multiple translations

__Solution:__

Instead of adding a verse to a universal favorites it can be added to a subject. This way verses will be ordered.
Allowing the user to switch translation and save multiple translations in the same subject.

__Hardest parts:__
* Making the Bible readable in the phone. (The API returns a verse but you want to read the whole chapter)
* Get which text is clicked to add to the correspondending subject.
* Getting Firebase and the APIs working.
* Getting a local database to enable reading the text.


### Prerequisites
* two different bible API sources
  * https://bible-api.com/
  * http://www.online-bijbel.nl/ontwikkelaars/
* the ability to login/register a user and keep track of their favorites (firebase)
  * https://firebase.google.com/
  

__MVP__
* Be able to read the bible
* Add a text to favorites (with a chosen subject) with use of Firebase
* Allow the user to make a new subject and delete one
* another bible translation

  ![draft](/doc/draft.jpg)
[![BCH compliance](https://bettercodehub.com/edge/badge/dedvg/BibleApp?branch=master)](https://bettercodehub.com/)
