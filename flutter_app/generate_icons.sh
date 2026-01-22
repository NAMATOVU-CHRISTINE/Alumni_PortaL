#!/bin/bash

echo "🚀 Generating launcher icons for MUST Alumni Portal..."

# Get dependencies
echo "📦 Getting dependencies..."
flutter pub get

# Generate launcher icons
echo "🎨 Generating launcher icons..."
flutter pub run flutter_launcher_icons:main

echo "✅ Launcher icons generated successfully!"
echo "📱 Your app will now show the custom launcher icon instead of 'AP'"
echo ""
echo "Next steps:"
echo "1. Clean and rebuild your app: flutter clean && flutter build apk"
echo "2. Install the app on your device to see the new icon"
echo ""