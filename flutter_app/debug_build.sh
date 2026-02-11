#!/bin/bash

echo "🧹 Cleaning build..."
flutter clean

echo "📦 Getting dependencies..."
flutter pub get

echo "🔨 Building release APK (without minification)..."
flutter build apk --release

echo "✅ Build complete!"
echo "📍 APK location: build/app/outputs/flutter-apk/app-release.apk"
echo ""
echo "📊 APK size:"
ls -lh build/app/outputs/flutter-apk/app-release.apk

echo ""
echo "🚀 To install on device:"
echo "   adb install -r build/app/outputs/flutter-apk/app-release.apk"
