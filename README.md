# BibleApp
David van Grinsven

### Problem statement
When using a bible app, you can ofthen mark a few verses or favorite them.
However when two verses are talking about the same subject it is hard to link them.

Currently there are bible apps for example: Bijbel - Statenvertaling.
But they lack this.

__Optional problem:__
When reading the Bible, one translation can differ a lot from the other. However it can add quite some meaning to the text.
By reading different translations side by side this can be done quite easy to get a better understanding of the text. 
For example by comparing the dutch and english text.

These options will be usefull for people that read the bible on daily basis and want to get more depth.

__Solution:__

Instead of adding a verse to a universal favorites it can be added to a subject. This way verses will be ordered.
Also the user will be able to set notes.

And when clicking in a verse the side by side translation of the english (or dutch) verse will appear, alowing the user to spot the differences.

__Hardest parts:__
* Making the Bible readable in the phone.
* Get which text is clicked to add to the correspondending subject.
* Getting Firebase and the APIs working.

### Prerequisites
* two different bible API sources
  * https://bible-api.com/
  * http://www.online-bijbel.nl/ontwikkelaars/
* the ability to login/register a user and keep track of their favorites (firebase)
  * https://firebase.google.com/
  
  ![draft](/doc/draft.jpg)

__ MVP __
* Be able to read the bible
* Add a text to favorites (with a chosen subject)
* Lookup the favorites of the correspondending user

__ optional __ 
* another bible translation
