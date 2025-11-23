# Alumni Portal

A comprehensive Android application designed to connect alumni, facilitate networking, and provide access to career opportunities and university news.

## Overview

Alumni Portal is a native Android application that serves as a central hub for former students to maintain connections, stay informed about university developments, discover job opportunities, and engage with the alumni community. The app combines modern Android development practices with Firebase backend services to deliver a seamless user experience.

## Key Features

Authentication and User Management
- Secure email and password authentication with Firebase
- Username-based login for convenience
- Password reset functionality
- Profile creation and management
- Profile picture uploads with cloud storage

Community and Networking
- Alumni directory with search and filtering
- Mentor search and mentorship connection system
- Alumni groups for community engagement
- Direct messaging between users
- Real-time chat functionality

Content and Information
- Live news feed from university RSS feeds
- Event listings and management
- Job board with career opportunities
- Career tips and knowledge hub
- Web content integration via WebView

Notifications
- Push notifications for messages and updates
- Customizable notification preferences
- Real-time notification delivery via Firebase Cloud Messaging

## Technology Stack

Core Development
- Language: Java
- Platform: Android (API 23+)
- Build System: Gradle with Kotlin DSL

Backend Services
- Firebase Authentication for user management
- Firebase Firestore for real-time database
- Firebase Storage for file uploads
- Firebase Cloud Messaging for push notifications
- Firebase Realtime Database for chat

UI and Design
- Material Design 3 components
- RecyclerView for efficient list rendering
- WebView for embedded web content
- Glide for image loading and caching
- CircleImageView for profile images

Data and Networking
- Retrofit for REST API communication
- Gson for JSON serialization
- JSoup for web scraping
- XmlPullParser for RSS feed parsing

Local Storage and Background Tasks
- Room database for offline storage
- WorkManager for background synchronization
- SharedPreferences for user preferences

## Project Structure

```
app/
├── src/main/java/com/namatovu/alumniportal/
│   ├── activities/          # UI screens and activities
│   ├── services/            # Background services and messaging
│   ├── models/              # Data models and entities
│   ├── utils/               # Helper utilities and managers
│   ├── adapters/            # RecyclerView adapters
│   └── receivers/           # Broadcast receivers
├── src/main/res/
│   ├── layout/              # XML layout files
│   ├── drawable/            # Vector drawables and images
│   ├── values/              # Strings, colors, dimensions
│   └── menu/                # Menu resources
└── build.gradle.kts         # Build configuration
```

## Getting Started

Prerequisites
- Android Studio (latest version)
- Android SDK 23 or higher
- Firebase account

Installation Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/NAMATOVU-CHRISTINE/Alumni_PortaL.git
   cd Alumni_PortaL
   ```

2. Open in Android Studio:
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. Configure Firebase:
   - Go to Firebase Console (https://console.firebase.google.com/)
   - Create a new project or use existing one
   - Add Android app with package name: com.namatovu.alumniportal
   - Download google-services.json
   - Place it in the app/ directory

4. Enable Firebase Services:
   - Authentication: Enable Email/Password sign-in
   - Firestore: Create database in production mode
   - Storage: Create storage bucket
   - Cloud Messaging: Enable for push notifications

5. Build and Run:
   - Sync Gradle files
   - Build the project
   - Run on device or emulator

## Build Configuration

The project is optimized for Play Store distribution with the following configurations:

Release Build Optimization
- Code minification enabled (ProGuard)
- Resource shrinking enabled
- Debug symbols removed
- Unused dependencies excluded
- Expected AAB size: 8-10 MB

To generate release AAB:
```bash
./gradlew bundleRelease
```

Output location: app/build/outputs/bundle/release/app-release.aab

## Firebase Setup Details

Firestore Database Structure
- users: User profiles and preferences
- chats: Chat conversations
- messages: Chat messages
- events: Event information
- jobs: Job postings
- news: News articles
- notifications: User notifications

Security Rules
- Users can only read/write their own data
- Public collections (events, jobs, news) are readable by all authenticated users
- Admin operations require custom claims

## Development Guidelines

Code Organization
- Activities handle UI and user interaction
- Services manage background tasks and messaging
- Models define data structures
- Utils provide reusable functionality
- Adapters handle list rendering

Best Practices
- Use LiveData for reactive UI updates
- Implement proper error handling
- Follow Material Design guidelines
- Optimize images and resources
- Handle permissions properly for Android 6+

## Testing

The application has been tested on:
- Android 6.0 (API 23) and above
- Various device sizes and orientations
- Real devices and emulators

## Deployment

Play Store Release Process
1. Generate signed release APK/AAB
2. Test thoroughly on real devices
3. Upload to Google Play Console
4. Configure store listing and screenshots
5. Submit for review

## Troubleshooting

Common Issues

Firebase Connection Issues
- Verify google-services.json is in app/ directory
- Check Firebase project settings match package name
- Ensure Firebase services are enabled in console

Notification Problems
- Verify FCM is enabled in Firebase
- Check notification permissions on device
- Ensure device has valid FCM token

Build Errors
- Run "Sync Now" in Android Studio
- Clear build cache: ./gradlew clean
- Invalidate caches and restart Android Studio

## Contributing

To contribute to this project:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is proprietary and intended for Makerere University alumni.

## Support

For issues or questions:
- Check existing GitHub issues
- Create a new issue with detailed description
- Contact the development team

## Version History

Version 1.1 (Current)
- Added push notifications
- Improved chat functionality
- Enhanced user profiles
- Optimized for Play Store

Version 1.0
- Initial release
- Core features implemented
- Firebase integration
