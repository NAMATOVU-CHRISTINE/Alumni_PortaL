# MUST Alumni Portal

A comprehensive multi-platform application ecosystem designed to connect Mbarara University of Science and Technology (MUST) alumni, facilitate networking, and provide access to career opportunities and university news.

## Overview

The MUST Alumni Portal is a multi-platform application ecosystem that serves as a central hub for former students to maintain connections, stay informed about university developments, discover job opportunities, and engage with the alumni community. The project includes both Android (Java) and Flutter applications, with Firebase backend services delivering a seamless cross-platform experience.

### Project Components

- **Android App** (Java): Native Android application with full feature set
- **Flutter App**: Cross-platform mobile application for iOS and Android
- **Web Interface**: Account deletion and compliance pages
- **Backend Services**: Firebase-powered cloud infrastructure
- **Documentation**: Comprehensive project documentation and budget templates

## Key Features

### Authentication and User Management
- Secure email and password authentication with Firebase
- Google Sign-In integration for seamless access
- Username-based login for convenience (demo_alumni, demo_student, demo_staff)
- Password reset functionality
- Profile creation and management with role-based access (Alumni, Student, Staff)
- Profile picture uploads with cloud storage

### Community and Networking
- Alumni directory with advanced search and filtering
- **Notable Alumni Carousel**: Featured alumni showcase with auto-scrolling
- Mentor search and mentorship connection system
- Alumni groups for community engagement
- Direct messaging between users with real-time chat
- Stories feature for sharing updates

### Content and Information
- Live news feed from university RSS feeds
- Event listings and management with RSVP functionality
- Job board with career opportunities and application tracking
- Career tips and knowledge hub
- Enhanced career center with professional development resources
- Web content integration via WebView

### Notifications and Communication
- Push notifications for messages, events, and updates
- Customizable notification preferences
- Real-time notification delivery via Firebase Cloud Messaging
- Email integration for important communications

### Mobile Experience
- **Flutter App**: Cross-platform mobile application
- Material Design 3 components with consistent theming
- Responsive design for tablets and phones
- Offline functionality with local data caching
- Auto-scrolling carousels and interactive UI elements

## Technology Stack

### Android Application (Java)
- **Language**: Java
- **Platform**: Android (API 21+ / Android 5.0+)
- **Build System**: Gradle with Kotlin DSL
- **Architecture**: MVVM with Repository pattern

### Flutter Application
- **Language**: Dart
- **Framework**: Flutter 3.x
- **Platform**: iOS and Android
- **State Management**: Provider pattern
- **Navigation**: GoRouter for declarative routing

### Backend Services
- **Firebase Authentication**: User management and security
- **Firebase Firestore**: Real-time NoSQL database
- **Firebase Storage**: File uploads and media storage
- **Firebase Cloud Messaging**: Push notifications
- **Firebase Hosting**: Web hosting for compliance pages

### UI and Design
- **Material Design 3**: Consistent design system
- **Responsive Design**: Tablet and phone optimization
- **Custom Animations**: Smooth transitions and interactions
- **Image Handling**: Cached network images with Glide/CachedNetworkImage

### Development Tools
- **Version Control**: Git with GitHub
- **CI/CD**: GitHub Actions for automated builds
- **Code Quality**: Linting and formatting rules
- **Documentation**: Comprehensive README and inline comments

## Project Structure

```
├── app/                           # Android application (Java)
│   ├── src/main/java/com/namatovu/alumniportal/
│   │   ├── activities/            # UI screens and activities
│   │   ├── services/              # Background services and messaging
│   │   ├── models/                # Data models and entities
│   │   ├── utils/                 # Helper utilities and managers
│   │   ├── adapters/              # RecyclerView adapters
│   │   └── receivers/             # Broadcast receivers
│   ├── src/main/res/              # Android resources
│   └── build.gradle.kts           # Android build configuration
├── flutter_app/                   # Flutter application
│   ├── lib/
│   │   ├── screens/               # UI screens and pages
│   │   │   └── home/              # Home screen components
│   │   │       ├── home_screen.dart
│   │   │       └── notable_alumni_carousel.dart
│   │   ├── providers/             # State management
│   │   ├── services/              # API and Firebase services
│   │   ├── models/                # Data models
│   │   └── config/                # App configuration
│   ├── android/                   # Android-specific Flutter config
│   ├── ios/                       # iOS-specific Flutter config
│   └── pubspec.yaml               # Flutter dependencies
├── docs/                          # Project documentation
│   └── SYSTEM_BUDGET.md           # Budget and financial planning
├── functions/                     # Firebase Cloud Functions
├── public/                        # Web assets and hosting
├── account-deletion.html          # Google Play Console compliance
├── Phase-1-Budget-Template-ST-OP.csv  # Budget template (CSV)
├── Phase-1-Budget-Template-ST-OP.xls  # Budget template (Excel)
└── README.md                      # This file
```

## Getting Started

### Prerequisites
- **Android Studio**: Latest version (for Android development)
- **Flutter SDK**: 3.x or higher (for Flutter development)
- **Android SDK**: API 21 or higher
- **Firebase Account**: For backend services
- **Git**: For version control

### Demo Accounts
For testing purposes, use these demo credentials:
- **Alumni**: `demo_alumni` / `DemoPass123!`
- **Student**: `demo_student` / `DemoPass123!`
- **Staff**: `demo_staff` / `DemoPass123!`

### Installation Steps

#### 1. Clone the Repository
```bash
git clone https://github.com/NAMATOVU-CHRISTINE/Alumni_PortaL.git
cd Alumni_PortaL
```

#### 2. Android App Setup
```bash
# Open Android Studio
# Select "Open an Existing Project"
# Navigate to the cloned directory
```

#### 3. Flutter App Setup
```bash
cd flutter_app
flutter pub get
flutter run
```

#### 4. Firebase Configuration
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing one
3. Add Android app with package name: `com.namatovu.alumniportal`
4. Add Flutter app with the same package name
5. Download `google-services.json` for Android
6. Download `GoogleService-Info.plist` for iOS
7. Place files in respective directories

#### 5. Enable Firebase Services
- **Authentication**: Enable Email/Password and Google Sign-In
- **Firestore**: Create database in production mode
- **Storage**: Create storage bucket
- **Cloud Messaging**: Enable for push notifications
- **Hosting**: Enable for web compliance pages

## Build Configuration

### Android Release Build
The Android project is optimized for Google Play Store distribution:

```bash
# Generate release AAB (recommended for Play Store)
./gradlew bundleRelease

# Generate release APK (for direct distribution)
./gradlew assembleRelease
```

**Build Optimization:**
- Code minification enabled (R8/ProGuard)
- Resource shrinking enabled
- Debug symbols removed for release builds
- Unused dependencies excluded
- Expected AAB size: 50-70 MB
- Expected APK size: 65-85 MB

**Output Locations:**
- AAB: `app/build/outputs/bundle/release/app-release.aab`
- APK: `app/build/outputs/apk/release/app-release.apk`

### Flutter Release Build
```bash
cd flutter_app

# Build for Android
flutter build apk --release
flutter build appbundle --release

# Build for iOS
flutter build ios --release
```

### Version Management
- **Current Version**: 1.3.0 (Version Code: 3)
- **Minimum SDK**: Android 5.0 (API 21)
- **Target SDK**: Android 14 (API 34)
- **Compile SDK**: Android 14 (API 34)

## Google Play Console Compliance

### Account Deletion
The app provides a compliant account deletion mechanism as required by Google Play policies:

**Account Deletion URL**: https://namatovu-christine.github.io/Alumni_PortaL/account-deletion.html

This page allows users to:
- Request permanent account deletion
- Understand the implications of deletion
- Contact support for assistance
- Submit deletion requests via email

### Data Safety Declaration
The app collects the following user data types:
- **Personal Info**: Names, email addresses, profile information
- **Photos**: Profile pictures and uploaded images
- **Messages**: Chat messages and communications
- **App Activity**: Posts, interactions, and usage data

All data is:
- Encrypted in transit and at rest
- Used only for app functionality and user experience
- Not shared with third parties without consent
- Deletable upon user request

### Demo Test Accounts
For Google Play Console review team:
- **Alumni Account**: `demo_alumni` / `DemoPass123!`
- **Student Account**: `demo_student` / `DemoPass123!`
- **Staff Account**: `demo_staff` / `DemoPass123!`

## Project Budget and Planning

### Budget Overview
- **Total Project Cost**: UGX 2,000,000
- **Annual Maintenance**: UGX 300,000

### Budget Allocation
- **Development & Design**: UGX 1,150,000 (57.5%)
- **Cloud Services**: UGX 450,000 (22.5%)
- **Third-Party Services**: UGX 270,000 (13.5%)
- **Deployment & Setup**: UGX 130,000 (6.5%)

### Payment Schedule
- **Phase 1**: UGX 500,000 (25%) - Initial development
- **Phase 2**: UGX 600,000 (30%) - Core features
- **Phase 3**: UGX 500,000 (25%) - Testing and refinement
- **Phase 4**: UGX 400,000 (20%) - Deployment and launch

**Budget Files:**
- `Phase-1-Budget-Template-ST-OP.csv` - Detailed budget breakdown
- `docs/SYSTEM_BUDGET.md` - Comprehensive budget documentation

## Firebase Setup Details

### Firestore Database Structure
```
├── users/                    # User profiles and authentication data
├── chats/                    # Chat conversations and metadata
├── messages/                 # Individual chat messages
├── events/                   # University events and activities
├── jobs/                     # Job postings and opportunities
├── news/                     # News articles and updates
├── notifications/            # User notifications and alerts
├── notable_alumni/           # Featured alumni for carousel
├── mentors/                  # Mentor profiles and availability
└── groups/                   # Alumni groups and communities
```

### Security Rules
- Users can only read/write their own profile data
- Public collections (events, jobs, news) are readable by authenticated users
- Chat messages are restricted to conversation participants
- Admin operations require custom security claims
- All writes are validated against schema rules

### Firebase Configuration Files
- **Android**: `app/google-services.json`
- **Flutter**: `flutter_app/android/app/google-services.json`
- **iOS**: `flutter_app/ios/Runner/GoogleService-Info.plist`
- **Web**: Firebase config in `flutter_app/lib/firebase_options.dart`

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
