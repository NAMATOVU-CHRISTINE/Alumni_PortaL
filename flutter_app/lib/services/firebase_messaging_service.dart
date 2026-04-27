import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:alumni_portal/services/navigation_service.dart';

// Store pending navigation for when app opens
String? _pendingChatId;

// Top-level function for background message handling
// This runs even when the app is terminated/closed
@pragma('vm:entry-point')
Future<void> firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  print('🔔 Background message received: ${message.messageId}');
  print('Title: ${message.notification?.title}');
  print('Body: ${message.notification?.body}');
  print('Data: ${message.data}');
  
  // Note: Local notifications are automatically shown by Firebase when app is in background/terminated
  // This handler is for additional processing like saving to database
  
  // You can add custom logic here like:
  // - Saving notification to local database
  // - Updating app badge count
  // - Processing data payloads
}

class FirebaseMessagingService {
  static final FirebaseMessaging _messaging = FirebaseMessaging.instance;
  static final FlutterLocalNotificationsPlugin _localNotifications =
      FlutterLocalNotificationsPlugin();
  static final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  static final FirebaseAuth _auth = FirebaseAuth.instance;

  static String? _fcmToken;
  static String? get fcmToken => _fcmToken;

  /// Initialize Firebase Messaging and local notifications
  static Future<void> initialize() async {
    // Request permission for iOS
    NotificationSettings settings = await _messaging.requestPermission(
      alert: true,
      badge: true,
      sound: true,
      provisional: false,
    );

    if (settings.authorizationStatus == AuthorizationStatus.authorized) {
      print('User granted notification permission');
    } else if (settings.authorizationStatus ==
        AuthorizationStatus.provisional) {
      print('User granted provisional notification permission');
    } else {
      print('User declined or has not accepted notification permission');
    }

    // Initialize local notifications
    await _initializeLocalNotifications();

    // Get FCM token
    _fcmToken = await _messaging.getToken();
    print('FCM Token: $_fcmToken');

    // Save token to Firestore
    if (_fcmToken != null) {
      await _saveFcmToken(_fcmToken!);
    }

    // Listen for token refresh
    _messaging.onTokenRefresh.listen((newToken) {
      _fcmToken = newToken;
      _saveFcmToken(newToken);
    });

    // Handle foreground messages
    FirebaseMessaging.onMessage.listen(_handleForegroundMessage);

    // Handle notification taps when app is in background
    FirebaseMessaging.onMessageOpenedApp.listen(_handleMessageOpenedApp);

    // Check if app was opened from a terminated state
    RemoteMessage? initialMessage = await _messaging.getInitialMessage();
    if (initialMessage != null) {
      _handleMessageOpenedApp(initialMessage);
    }

    // Note: Background message handler is registered in main.dart
    // FirebaseMessaging.onBackgroundMessage() should only be called once
  }

  /// Initialize local notifications plugin
  static Future<void> _initializeLocalNotifications() async {
    const androidSettings =
        AndroidInitializationSettings('@mipmap/launcher_icon');

    const iosSettings =
        DarwinInitializationSettings(
      requestAlertPermission: true,
      requestBadgePermission: true,
      requestSoundPermission: true,
    );

    const settings = InitializationSettings(
      android: androidSettings,
      iOS: iosSettings,
    );

    await _localNotifications.initialize(
      settings,
      onDidReceiveNotificationResponse: _onNotificationTapped,
    );

    // Create notification channels for Android
    const generalChannel = AndroidNotificationChannel(
      'high_importance_channel',
      'High Importance Notifications',
      description: 'This channel is used for important notifications.',
      importance: Importance.high,
    );

    const messageChannel = AndroidNotificationChannel(
      'messages_channel',
      'Messages',
      description: 'Notifications for new messages',
      importance: Importance.max,
      playSound: true,
      enableVibration: true,
      showBadge: true,
    );

    final androidPlugin = _localNotifications
        .resolvePlatformSpecificImplementation<
            AndroidFlutterLocalNotificationsPlugin>();

    await androidPlugin?.createNotificationChannel(generalChannel);
    await androidPlugin?.createNotificationChannel(messageChannel);
  }

  /// Handle foreground messages
  static Future<void> _handleForegroundMessage(RemoteMessage message) async {
    print('Foreground message received: ${message.messageId}');

    RemoteNotification? notification = message.notification;
    Map<String, dynamic> data = message.data;

    // Determine notification type
    String notificationType = data['type'] ?? 'general';

    // Show local notification when app is in foreground
    if (notification != null) {
      await _showLocalNotification(
        title: notification.title ?? 'New Notification',
        body: notification.body ?? '',
        payload: message.data.toString(),
        notificationType: notificationType,
      );
    } else if (data.isNotEmpty) {
      // Handle data-only messages
      await _showLocalNotification(
        title: data['title'] ?? 'New Notification',
        body: data['message'] ?? data['body'] ?? '',
        payload: message.data.toString(),
        notificationType: notificationType,
      );
    }

    // Save to Firestore
    await _saveNotificationToFirestore(message);
  }

  /// Handle notification tap
  static void _onNotificationTapped(NotificationResponse response) {
    print('Notification tapped: ${response.payload}');
    
    if (response.payload == null) return;
    
    try {
      // Store the chatId for navigation
      _pendingChatId = response.payload!;
      print('Stored pending chat ID: $_pendingChatId');
    } catch (e) {
      print('Error handling notification tap: $e');
    }
  }

  /// Get and clear pending chat navigation
  static String? getPendingChatId() {
    final chatId = _pendingChatId;
    _pendingChatId = null;
    return chatId;
  }

  /// Handle message opened from background
  static void _handleMessageOpenedApp(RemoteMessage message) {
    print('Message opened app: ${message.messageId}');
    // Handle navigation based on message data
  }

  /// Show local notification (public method)
  static Future<void> showNotification({
    required String title,
    required String body,
    String? payload,
    String notificationType = 'general',
  }) async {
    await _showLocalNotification(
      title: title,
      body: body,
      payload: payload,
      notificationType: notificationType,
    );
  }

  /// Show local notification (internal method)
  static Future<void> _showLocalNotification({
    required String title,
    required String body,
    String? payload,
    String notificationType = 'general',
  }) async {
    // Determine notification style based on type
    bool isMessage = notificationType == 'message' || notificationType == 'chat';
    bool isNews = notificationType == 'news';
    bool isEvent = notificationType == 'event';
    
    // Choose appropriate channel
    String channelId = isMessage ? 'messages_channel' : 'high_importance_channel';
    String channelName = isMessage ? 'Messages' : 'High Importance Notifications';
    
    final androidDetails = AndroidNotificationDetails(
      channelId,
      channelName,
      channelDescription: isMessage 
          ? 'Notifications for new messages'
          : 'This channel is used for important notifications.',
      importance: Importance.max,
      priority: Priority.high,
      showWhen: true,
      icon: '@mipmap/launcher_icon',
      playSound: true,
      enableVibration: true,
      enableLights: true,
      color: const Color(0xFF8BC34A), // Lime green color
      // WhatsApp-style notification with appropriate styling
      styleInformation: isMessage 
          ? MessagingStyleInformation(
              Person(name: 'You'),
              conversationTitle: title,
              messages: [
                Message(body, DateTime.now(), Person(name: title)),
              ],
              groupConversation: false,
            )
          : BigTextStyleInformation(
              body,
              contentTitle: title,
              summaryText: _getNotificationSummary(notificationType),
            ),
      category: isMessage 
          ? AndroidNotificationCategory.message 
          : (isEvent ? AndroidNotificationCategory.event : null),
      // Add action buttons for messages
      actions: isMessage ? [
        const AndroidNotificationAction(
          'reply',
          'Reply',
          showsUserInterface: true,
          inputs: [
            AndroidNotificationActionInput(
              label: 'Type a message',
            ),
          ],
        ),
        const AndroidNotificationAction(
          'mark_read',
          'Mark as Read',
        ),
      ] : null,
    );

    const iosDetails = DarwinNotificationDetails(
      presentAlert: true,
      presentBadge: true,
      presentSound: true,
      interruptionLevel: InterruptionLevel.timeSensitive,
      categoryIdentifier: 'message_category',
    );

    final details = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );

    await _localNotifications.show(
      DateTime.now().millisecond,
      title,
      body,
      details,
      payload: payload,
    );
  }

  /// Get notification summary text based on type
  static String _getNotificationSummary(String type) {
    switch (type) {
      case 'news':
        return 'The Convocation News';
      case 'event':
        return 'The Convocation Events';
      case 'chat':
      case 'message':
        return 'The Convocation Messages';
      case 'job':
        return 'The Convocation Jobs';
      case 'mentorship':
        return 'The Convocation Mentorship';
      default:
        return 'The Convocation';
    }
  }

  /// Save FCM token to Firestore
  static Future<void> _saveFcmToken(String token) async {
    final userId = _auth.currentUser?.uid;
    if (userId == null) return;

    try {
      await _firestore.collection('users').doc(userId).update({
        'fcmToken': token,
        'lastTokenUpdate': FieldValue.serverTimestamp(),
      });
      print('FCM token saved to Firestore');
    } catch (e) {
      print('Error saving FCM token: $e');
    }
  }

  /// Save notification to Firestore
  static Future<void> _saveNotificationToFirestore(
      RemoteMessage message) async {
    final userId = _auth.currentUser?.uid;
    if (userId == null) return;

    try {
      await _firestore.collection('notifications').add({
        'userId': userId,
        'title': message.notification?.title ?? 'Notification',
        'message': message.notification?.body ?? '',
        'type': message.data['type'] ?? 'general',
        'referenceId': message.data['referenceId'],
        'read': false,
        'timestamp': FieldValue.serverTimestamp(),
        'data': message.data,
      });
    } catch (e) {
      print('Error saving notification to Firestore: $e');
    }
  }

  /// Send notification to specific user
  static Future<void> sendNotificationToUser({
    required String userId,
    required String title,
    required String body,
    Map<String, dynamic>? data,
  }) async {
    try {
      // Get user's FCM token
      final userDoc = await _firestore.collection('users').doc(userId).get();
      final fcmToken = userDoc.data()?['fcmToken'] as String?;

      if (fcmToken == null) {
        print('User does not have FCM token');
        return;
      }

      // Save notification to Firestore (will trigger Cloud Function to send FCM)
      await _firestore.collection('notifications').add({
        'userId': userId,
        'title': title,
        'message': body,
        'type': data?['type'] ?? 'general',
        'referenceId': data?['referenceId'],
        'read': false,
        'timestamp': FieldValue.serverTimestamp(),
        'data': data ?? {},
      });

      print('Notification sent to user: $userId');
    } catch (e) {
      print('Error sending notification: $e');
    }
  }

  /// Subscribe to topic
  static Future<void> subscribeToTopic(String topic) async {
    try {
      await _messaging.subscribeToTopic(topic);
      print('Subscribed to topic: $topic');
    } catch (e) {
      print('Error subscribing to topic: $e');
    }
  }

  /// Unsubscribe from topic
  static Future<void> unsubscribeFromTopic(String topic) async {
    try {
      await _messaging.unsubscribeFromTopic(topic);
      print('Unsubscribed from topic: $topic');
    } catch (e) {
      print('Error unsubscribing from topic: $e');
    }
  }

  /// Delete FCM token
  static Future<void> deleteToken() async {
    try {
      await _messaging.deleteToken();
      _fcmToken = null;
      print('FCM token deleted');
    } catch (e) {
      print('Error deleting FCM token: $e');
    }
  }
}
