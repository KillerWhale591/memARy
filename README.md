# memARy
An Augmented Reality(AR) based real-time sharing app on Android platform. Sharing you memory at any spot by 
AR doodling.

## Features
* **AR doodling**   
Doodle anywhere you like in an AR world, just by pressing on screen, and moving your phone or your finger.
* **Location-based sharing**   
Share your memory at a spot by forms of texts, images or AR doodles. See what other people shared nearby.
* **Data visualization on map**   
Find hot spots near you. See where do people post most often.
* **Lower Android version compatible**   
On Android phones where ARCore is not supported, sharing your memory by traditional posts.
* **Customize your preference**   
Filter searching range of posts or AR rendering limit, for better user experience.

## Contributing
If you want to contribute to this project, see [CONTRIBUTING.md](https://github.com/KillerWhale591/memARy/blob/master/CONTRIBUTING.md).

## Development environment
This repo is created and tested in Android Studio 3.5, and should be reproducible if required API_KEYs are obtained and set successfully. To build and compile, please provide all the tokens in [key.xml](https://github.com/KillerWhale591/memARy/blob/master/app/src/main/res/values/key.xml), including an API key from mapbox and two from Google API.

## Support
If you have any bug reports, feature suggestions, questions, or just want to talk about this project, open an issue or contact [zeyufu@bu.edu](zeyufu@bu.edu).

## Guidance of using AR
By long press on the screen, the running state would be changed to DRAW, where the creation of new AR drawings is feasible. Users could have the activity back to VIEW mode using Cross Button on the top-left corner. There are two ways to draw AR lines:

   * By pressing and moving finger on the screen
   * By pressing on the screen and moving the phone

The width of AR lines can be set by clicking the Overflow Button on the left side of the screen.  Users are also able to withdraw the previous line by clicking Undo Button and wipe out all the lines by clicking Delete Button on the top-left corner. If the current drawing is ready to share, on the bottom there is an Upload Button in the middle. The created new AR drawings is displayed in white, in order to be distinguishable from those drawings downloaded from database.

After uploading current AR drawing, the activity would be automatically set back to VIEW mode. Users could do long press again if they would like to create another drawing of their own.

**Note:** Due to the nature of AR and ARCore, the AR system may lose tracking due to sudden light change, large displacement or abrupt movement of the phone. The system would try to get back to tracking but there is no guarantee of success. We recommend our users to re-enter the activity if
 they found no AR object is rendered or suddenly disappears. We would apologize for any harm to our user experience due to technological limitation and we would always work on improving the system for better experience.

## License
This software is licensed under [Apache License 2.0](https://github.com/KillerWhale591/memARy/blob/master/LICENSE).
