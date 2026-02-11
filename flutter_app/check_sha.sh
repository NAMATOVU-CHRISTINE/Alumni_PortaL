#!/bin/bash

echo "🔍 Checking SHA-1 fingerprints..."
echo ""

echo "📱 Debug SHA-1:"
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android 2>/dev/null | grep SHA1

echo ""
echo "🔐 Release SHA-1 (from your keystore):"
if [ -f "../keystore.jks" ]; then
    keytool -list -v -keystore ../keystore.jks -alias upload -storepass android123 -keypass android123 2>/dev/null | grep SHA1
else
    echo "❌ keystore.jks not found at ../keystore.jks"
fi

echo ""
echo "⚠️  IMPORTANT:"
echo "Both SHA-1 fingerprints above must be added to your Firebase project:"
echo "1. Go to Firebase Console → Project Settings"
echo "2. Select your Android app"
echo "3. Add both SHA-1 fingerprints"
echo "4. Download the new google-services.json"
echo "5. Replace android/app/google-services.json with the new file"
