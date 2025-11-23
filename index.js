const express = require('express');
const admin = require('firebase-admin');
const cors = require('cors');
const bodyParser = require('body-parser');
require('dotenv').config();

const app = express();

// Middleware
app.use(cors());
app.use(bodyParser.json());

// Initialize Firebase Admin SDK
const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT || '{}');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: process.env.FIREBASE_DATABASE_URL
});

const db = admin.firestore();
const messaging = admin.messaging();

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Send message notification
app.post('/api/notifications/message', async (req, res) => {
  try {
    const { recipientUserId, senderName, messageText, chatId, senderId } = req.body;

    console.log('Notification request:', { recipientUserId, senderName, messageText, chatId });

    if (!recipientUserId || !senderName || !messageText || !chatId) {
      console.error('Missing required fields');
      return res.status(400).json({ error: 'Missing required fields' });
    }

    // Get user's FCM token
    const userDoc = await db.collection('users').doc(recipientUserId).get();
    if (!userDoc.exists) {
      console.error('User not found:', recipientUserId);
      return res.status(404).json({ error: 'User not found' });
    }

    const fcmToken = userDoc.data().fcmToken;
    console.log('FCM Token found:', fcmToken ? 'Yes' : 'No');
    
    if (!fcmToken) {
      console.error('User has no FCM token:', recipientUserId);
      return res.status(400).json({ error: 'User has no FCM token' });
    }

    // Send notification
    const message = {
      data: {
        type: 'message',
        senderId: senderId || 'unknown',
        senderName: senderName,
        messageText: messageText,
        chatId: chatId
      },
      token: fcmToken
    };

    console.log('Sending FCM message to token:', fcmToken.substring(0, 20) + '...');
    const response = await messaging.send(message);
    console.log('FCM message sent successfully:', response);
    res.json({ success: true, messageId: response });
  } catch (error) {
    console.error('Error sending message notification:', error);
    res.status(500).json({ error: error.message });
  }
});

// Send event notification
app.post('/api/notifications/event', async (req, res) => {
  try {
    const { eventId, eventTitle, action, sendToAll } = req.body;

    if (!eventId || !eventTitle || !action) {
      return res.status(400).json({ error: 'Missing required fields' });
    }

    const message = {
      data: {
        type: 'event',
        eventId: eventId,
        eventTitle: eventTitle,
        action: action
      },
      topic: 'events'
    };

    if (sendToAll) {
      // Send to all subscribers of 'events' topic
      const response = await messaging.send(message);
      res.json({ success: true, messageId: response });
    } else {
      // Send to specific user
      const { recipientUserId } = req.body;
      const userDoc = await db.collection('users').doc(recipientUserId).get();
      const fcmToken = userDoc.data().fcmToken;
      message.token = fcmToken;
      const response = await messaging.send(message);
      res.json({ success: true, messageId: response });
    }
  } catch (error) {
    console.error('Error sending event notification:', error);
    res.status(500).json({ error: error.message });
  }
});

// Send job notification
app.post('/api/notifications/job', async (req, res) => {
  try {
    const { jobId, jobTitle, company, sendToAll } = req.body;

    if (!jobId || !jobTitle || !company) {
      return res.status(400).json({ error: 'Missing required fields' });
    }

    const message = {
      data: {
        type: 'job',
        jobId: jobId,
        jobTitle: jobTitle,
        company: company
      },
      topic: 'jobs'
    };

    if (sendToAll) {
      const response = await messaging.send(message);
      res.json({ success: true, messageId: response });
    } else {
      const { recipientUserId } = req.body;
      const userDoc = await db.collection('users').doc(recipientUserId).get();
      const fcmToken = userDoc.data().fcmToken;
      message.token = fcmToken;
      const response = await messaging.send(message);
      res.json({ success: true, messageId: response });
    }
  } catch (error) {
    console.error('Error sending job notification:', error);
    res.status(500).json({ error: error.message });
  }
});

// Send mentorship notification
app.post('/api/notifications/mentorship', async (req, res) => {
  try {
    const { recipientUserId, fromUserName, action, requestId } = req.body;

    if (!recipientUserId || !fromUserName || !action || !requestId) {
      return res.status(400).json({ error: 'Missing required fields' });
    }

    const userDoc = await db.collection('users').doc(recipientUserId).get();
    if (!userDoc.exists) {
      return res.status(404).json({ error: 'User not found' });
    }

    const fcmToken = userDoc.data().fcmToken;
    if (!fcmToken) {
      return res.status(400).json({ error: 'User has no FCM token' });
    }

    const message = {
      data: {
        type: 'mentorship',
        requestId: requestId,
        fromUserName: fromUserName,
        action: action
      },
      token: fcmToken
    };

    const response = await messaging.send(message);
    res.json({ success: true, messageId: response });
  } catch (error) {
    console.error('Error sending mentorship notification:', error);
    res.status(500).json({ error: error.message });
  }
});

// Send news notification
app.post('/api/notifications/news', async (req, res) => {
  try {
    const { newsId, newsTitle, authorName, sendToAll } = req.body;

    if (!newsId || !newsTitle || !authorName) {
      return res.status(400).json({ error: 'Missing required fields' });
    }

    const message = {
      data: {
        type: 'news',
        newsId: newsId,
        newsTitle: newsTitle,
        authorName: authorName
      },
      topic: 'news'
    };

    if (sendToAll) {
      const response = await messaging.send(message);
      res.json({ success: true, messageId: response });
    } else {
      const { recipientUserId } = req.body;
      const userDoc = await db.collection('users').doc(recipientUserId).get();
      const fcmToken = userDoc.data().fcmToken;
      message.token = fcmToken;
      const response = await messaging.send(message);
      res.json({ success: true, messageId: response });
    }
  } catch (error) {
    console.error('Error sending news notification:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get notification preferences for a user
app.get('/api/notifications/preferences/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    const userDoc = await db.collection('users').doc(userId).get();

    if (!userDoc.exists) {
      return res.status(404).json({ error: 'User not found' });
    }

    const preferences = userDoc.data().notificationPreferences || {};
    res.json(preferences);
  } catch (error) {
    console.error('Error fetching preferences:', error);
    res.status(500).json({ error: error.message });
  }
});

// Update notification preferences
app.put('/api/notifications/preferences/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    const preferences = req.body;

    await db.collection('users').doc(userId).update({
      notificationPreferences: preferences
    });

    res.json({ success: true, preferences });
  } catch (error) {
    console.error('Error updating preferences:', error);
    res.status(500).json({ error: error.message });
  }
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({ error: 'Internal server error' });
});

// Start server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Alumni Portal Backend running on port ${PORT}`);
});
