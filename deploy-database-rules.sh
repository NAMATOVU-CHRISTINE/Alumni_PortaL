#!/bin/bash

# Deploy Firebase Realtime Database rules
echo "Deploying Firebase Realtime Database rules..."
firebase deploy --only database

echo "Database rules deployed successfully!"
