# Location App
An app with CRUD functionality to create, view and modify markers on a map

This app allows users to create and modify markers on the map with MVVM architecture. The app is built upon the backend located at: https://labs.anontech.info/cse489/t3/api.php

## Features
- NavDrawer to browse through the app
- Displays Markers on a Google Map.
- Users are able to create a new entry
- Users can share their location or input a custom location latitude and longitude
- ReverseGeoCoding is used to get the address name of user location (if the app gets that through phone's gps)
- An image can be uploaded with each entry
- The image is resized to 800x600 before uploading to backend
- Clickable map markers show title and image at the bottom of the screen.
- Clicking images show an enlarged version of the image with Title
- An page consists of all the entries of map markers fethed from the backend
- Clicking an entry of the list lets users edit that marker

## Screenshots
<img height="500px" src="/screenshots/home_markerView.png"> &nbsp; <img height="500px" src="/screenshots/navDrawer.png"> &nbsp; <img height="500px" src="/screenshots/createEntity.png"> &nbsp; <img height="500px" src="/screenshots/userlocationfetching.png"> &nbsp; <img height="500px" src="/screenshots/imageuploader.png">
<img height="500px" src="/screenshots/successToast.png"> &nbsp; <img height="500px" src="/screenshots/markerSelectedView.png"> &nbsp; <img height="500px" src="/screenshots/enlargedView.png"> &nbsp; <img height="500px" src="/screenshots/entitiesList"> &nbsp; <img height="500px" src="/screenshots/clickToEdit.png">
<img height="500px" src="/screenshots/clickToEdited.png"> &nbsp; <img height="500px" src="/screenshots/afterEdit.png">


## Technologies Used
- Jetpack Compose for UI.
- Google Maps SDK for Android.
- Retrofit for API calls
- Coil for image loading.

## Setup and Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/MHThe1/Location-app.git
2. Import project to Android Studio
3. Sync Gradle
4. Get your own API key from Google Maps API
5. Set up API Key in local.properties file
   ```bash
   API_KEY=<your api key here>
6. Build and Run

## Future Enhancements
- Let users choose custiom location from MAP UI
- Store cache in the app to load faster when laoding map
- Implement user authentication for personalized experience



