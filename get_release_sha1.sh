#!/bin/bash
# Script to get SHA-1 fingerprints for Firebase

echo "=== Debug Keystore SHA-1 ==="
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android 2>/dev/null | grep -i "SHA1\|SHA256"

echo ""
echo "=== Release Keystore SHA-1 ==="
echo "Enter your release keystore password when prompted:"
keytool -list -v -keystore ./keystore.jks -alias upload 2>/dev/null | grep -i "SHA1\|SHA256"

echo ""
echo "If the alias 'upload' doesn't work, try these common aliases:"
echo "  - key0"
echo "  - androiddebugkey"
echo "  - release"
echo ""
echo "To list all aliases in your keystore:"
echo "  keytool -list -keystore ./keystore.jks"
