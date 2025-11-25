#!/bin/bash

echo "=== Debug Keystore SHA-1 ==="
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android 2>/dev/null | grep -A 1 "Certificate fingerprints"

echo ""
echo "=== Release Keystore SHA-1 (if exists) ==="
if [ -f "keystore.jks" ]; then
    echo "Found keystore.jks - enter your keystore password when prompted:"
    keytool -list -v -keystore keystore.jks | grep -A 1 "Certificate fingerprints"
else
    echo "No keystore.jks found in project root"
fi
