# Alumni Portal Android Application

## Project Overview

This is a native Android application for the alumni of Mbarara University of Science and Technology (MUST). The app serves as a central hub for former students to connect, stay informed about university news, find job opportunities, and engage with the alumni community.

## Core Features

- **Complete User Authentication:** Secure login, signup, and password reset functionality using Firebase Authentication.
- **Username Login:** Users can sign up with an email but log in with a unique username for convenience.
- **Profile Management:** Users can view and edit their profiles, including their name, bio, career information, and personal skills.
- **Profile Picture Uploads:** Users can upload and update their profile pictures, which are stored securely in Firebase Storage.
- **Dynamic Home Dashboard:** A central screen that welcomes the user by name and provides easy navigation to all app features.
- **Live News Feed:** The "News & Events" page displays the latest news directly from the official MUST website by parsing its RSS feed, ensuring reliable and up-to-date content.
- **Embedded Web Content:** The home screen features an integrated `WebView` displaying the MUST homepage.
- **Feature-Rich Navigation:** Includes dedicated sections for Job Postings, Mentor Search, and a Knowledge Hub (functionality to be implemented).

## Technology Stack

*   **Language:** Java
*   **Platform:** Android
*   **Backend Services (Firebase):**
    *   **Firebase Authentication:** For managing user accounts (login, signup, password reset).
    *   **Firebase Firestore:** As the primary NoSQL database for storing all user data, posts, and other app content.
    *   **Firebase Storage:** For storing user-uploaded content like profile pictures.
*   **UI Components:**
    *   Modern Material Design Components (`MaterialCardView`, `TextInputLayout`, etc.).
    *   `RecyclerView` for efficiently displaying lists of data (news, profiles, etc.).
    *   `WebView` for displaying web content within the app.
    *   `CircleImageView` for profile images.
*   **Data Fetching & Parsing:**
    *   `XmlPullParser`: For robustly parsing the RSS feed from the MUST website.
    *   **Glide:** For efficient loading and caching of profile images.
*   **Networking (Demo):**
    *   `Retrofit` & `Gson`: Used in a separate demo screen to showcase industry-standard REST API communication.

## Setup and Installation

To run this project, you will need to configure your own Firebase backend.

1.  **Clone the Repository:**
    ```sh
    git clone <your-repository-url>
    ```
2.  **Open in Android Studio:** Open the cloned directory as a new project in Android Studio.
3.  **Connect to Firebase:**
    *   Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
    *   Add an Android app to your Firebase project with the package name `com.namatovu.alumniportal`.
    *   Follow the setup steps to download the `google-services.json` file.
    *   Place the downloaded `google-services.json` file into the `app/` directory of this project. Make sure the `package_name`/`applicationId` inside the file matches `com.namatovu.alumniportal`. If you keep a different `applicationId`, re-download the correct `google-services.json` from the Firebase console and replace the existing one.

    Note: This project currently uses the Java package `com.namatovu.alumniportal`. If you prefer a different applicationId (for example `com.example.alumni_portal`), update `app/build.gradle.kts`'s `applicationId` and `namespace` accordingly and download the matching `google-services.json` from Firebase.
4.  **Enable Firebase Services:** In the Firebase Console, ensure you have enabled the following services:
    *   **Authentication:** Enable the "Email/Password" sign-in method.
    *   **Firestore:** Create a new Firestore database.
    *   **Storage:** Create a new Firebase Storage bucket.
5.  **Build and Run:** Build and run the application on an Android device or emulator.
