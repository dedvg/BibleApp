# BibleApp
David van Grinsven

### Problem statement
When using a bible app, you can ofthen mark a few verses or favorite them.
However when two verses are talking about the same subject it is hard to link them.

Currently there are bible apps for example: Bijbel - Statenvertaling.
But they lack this.
Bible is another app, it has the functions but not orderly and usefull for study purposes.

__Optional problems:__

* When reading the Bible, one translation can differ a lot from the other. However it can add quite some meaning to the text.
By reading different translations side by side this can be done quite easy to get a better understanding of the text. 
  * For example by comparing the dutch and english text.
  * A user can select which translations he wishes to compare.
* Notes and boommarks are separated. They can be combined.
  * these notes and boommarks can be orderly sorted.
* Enable the user to share bible verses without adds.
* Let the text be continuous and still be able to select a verse.
* Lookup the favorites and notes from another user.

These options will be usefull for people that read the bible on daily basis and want to get more depth.

__Solution:__

Instead of adding a verse to a universal favorites it can be added to a subject. This way verses will be ordered.
Also the user will be able to set notes.

And when clicking in a verse the side by side translation of the english (or dutch) verse will appear, alowing the user to spot the differences.

__Hardest parts:__
* Making the Bible readable in the phone. (The API returns a verse but you want to read the whole chapter)
* Get which text is clicked to add to the correspondending subject.
* Getting Firebase and the APIs working.


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


__optional__ 
* alowing the user to see 2 translations side by side
* adding notes when adding a text to favorites
* Lookup the favorites of the correspondending user


  ![draft](/doc/draft.jpg)

