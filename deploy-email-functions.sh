#!/bin/bash

# Gmail API Email Notifications Deployment Script
# This script helps deploy the Firebase Cloud Functions for email notifications

set -e  # Exit on any error

echo "🚀 Alumni Portal - Gmail API Email Notifications Deployment"
echo "=========================================================="

# Check if Firebase CLI is installed
if ! command -v firebase &> /dev/null; then
    echo "❌ Firebase CLI not found. Please install it:"
    echo "   npm install -g firebase-tools"
    exit 1
fi

# Check if we're in the right directory
if [ ! -f "functions/package.json" ]; then
    echo "❌ Please run this script from the Alumni_PortaL root directory"
    exit 1
fi

echo "✅ Firebase CLI found"

# Check if user is logged in to Firebase
if ! firebase projects:list &> /dev/null; then
    echo "🔐 Please login to Firebase:"
    firebase login
fi

echo "✅ Firebase authentication confirmed"

# Set the project
echo "🎯 Setting Firebase project..."
firebase use subtle-torus-477010-u0

# Install function dependencies
echo "📦 Installing function dependencies..."
cd functions
npm install
cd ..

echo "✅ Dependencies installed"

# Check if secrets are configured
echo "🔐 Checking if secrets are configured..."

# Function to check secret
check_secret() {
    local secret_name=$1
    if firebase functions:secrets:access $secret_name &> /dev/null; then
        echo "✅ $secret_name is configured"
        return 0
    else
        echo "❌ $secret_name is not configured"
        return 1
    fi
}

# Check all required secrets
secrets_missing=false

if ! check_secret "GMAIL_CLIENT_ID"; then
    secrets_missing=true
fi

if ! check_secret "GMAIL_CLIENT_SECRET"; then
    secrets_missing=true
fi

if ! check_secret "GMAIL_REFRESH_TOKEN"; then
    secrets_missing=true
fi

if ! check_secret "FROM_EMAIL"; then
    secrets_missing=true
fi

if [ "$secrets_missing" = true ]; then
    echo ""
    echo "❌ Some secrets are missing. Please configure them:"
    echo ""
    echo "1. First, get your refresh token:"
    echo "   cd functions && node get-refresh-token.js"
    echo ""
    echo "2. Then configure secrets:"
    echo "   firebase functions:secrets:set GMAIL_CLIENT_ID"
    echo "   firebase functions:secrets:set GMAIL_CLIENT_SECRET"
    echo "   firebase functions:secrets:set GMAIL_REFRESH_TOKEN"
    echo "   firebase functions:secrets:set FROM_EMAIL"
    echo ""
    echo "Use these values:"
    echo "   GMAIL_CLIENT_ID: 26037417599-dadlj77bh87oma5kuhoh9c6j32k7at6a.apps.googleusercontent.com"
    echo "   GMAIL_CLIENT_SECRET: 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"
    echo "   GMAIL_REFRESH_TOKEN: (from get-refresh-token.js output)"
    echo "   FROM_EMAIL: your-gmail-address@gmail.com"
    echo ""
    exit 1
fi

echo "✅ All secrets are configured"

# Deploy functions
echo "🚀 Deploying Cloud Functions..."
firebase deploy --only functions

echo ""
echo "✅ Deployment completed successfully!"
echo ""
echo "🧪 Testing the deployment..."

# Get the project ID
PROJECT_ID=$(firebase projects:list | grep "subtle-torus-477010-u0" | awk '{print $1}' || echo "subtle-torus-477010-u0")

# Test health check endpoint
echo "📡 Testing health check endpoint..."
HEALTH_URL="https://us-central1-${PROJECT_ID}.cloudfunctions.net/healthCheck"

if curl -s "$HEALTH_URL" | grep -q "ok"; then
    echo "✅ Health check passed!"
else
    echo "⚠️  Health check failed or endpoint not ready yet"
    echo "   Try manually: curl $HEALTH_URL"
fi

echo ""
echo "🎉 Setup complete! Next steps:"
echo "================================"
echo ""
echo "1. Test mentorship email flow:"
echo "   - Create a mentorship request in the Android app"
echo "   - Check Firestore for the mentorship document"
echo "   - Verify email is sent"
echo ""
echo "2. Monitor function logs:"
echo "   firebase functions:log --only sendMentorshipEmail"
echo ""
echo "3. Check function metrics in Firebase Console:"
echo "   https://console.firebase.google.com/project/${PROJECT_ID}/functions"
echo ""
echo "📚 For troubleshooting, see GMAIL_SETUP.md"