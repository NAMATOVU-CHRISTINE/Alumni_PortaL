#!/bin/bash

echo "🧪 Testing Firestore connection in release mode..."
echo ""

# Build release APK
echo "📦 Building release APK..."
flutter build apk --release

# Install on device
echo "📲 Installing on device..."
adb install -r build/app/outputs/flutter-apk/app-release.apk

echo ""
echo "✅ APK installed. Now:"
echo "1. Open the app on your device"
echo "2. Go to MUST Community page"
echo "3. Watch the logs below:"
echo ""

# Show logs filtered for our app
adb logcat -c  # Clear logs
adb logcat | grep -E "flutter|Firestore|alumniportal"
