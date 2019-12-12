# memARy

## Bulid and Compile

This repo is created and tested in Android Studio 3.5, and should be reproducible if required API_KEYs are obtained and set successfully.


## Use AR functions

AR activity would run under VIEW mode after intialization. Users should be able to see colorful drawings within certain geographical range, created by other users. Number of drawings and search range can be configured in App Preference. Refresh Button is provided for downloading and rendering    another group of previous AR Drawings.

By long press on the screen, the running state would be changed to DRAW, where the creation of new AR drawings is feasible. Users could have the activity back to VIEW mode using Cross Button on the top-left corner. There are two ways to draw AR lines,

   * By pressing and moving finger on the screen
   * By pressing and moving the phone in space

The width of AR lines can be choose by click the Overflow Button in the left side of the screen.  Users are also able to withdraw the previous line by clicking Undo Button and wipe out all the lines by clicking Delete Button on the top-left corner. If the current drawing is ready to share, on the    bottom there is an Upload Button in the middle. The created new AR Drawings is displayed in white, in order to be distinguishable from those drawings downloaded from database.

After uploading current AR drawing, the activity would be automatically set back to VIEW mode. Users could do long press again if they would like to create another drawing of their own.

**Note:** Due to the nature of AR and ARCore, the AR system may lose tracking due to sudden light change, large displacement or abrupt movement of the phone. The system would try to get back to tracking but there is no guarantee of success. We recommend our users to re-enter the activity if
 they found no AR objects are rendered or suddenly disappears. We would apology for any harm to our user experience due to technological limitation and we would always work on improving the system for better experience.