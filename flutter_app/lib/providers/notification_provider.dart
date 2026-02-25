import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:alumni_portal/models/notification_model.dart';
import 'package:alumni_portal/services/firebase_messaging_service.dart';

class NotificationProvider with ChangeNotifier {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;
  final FirebaseMessaging _messaging = FirebaseMessaging.instance;

  List<NotificationModel> _notifications = [];
  bool _isLoading = false;
  String? _error;
  StreamSubscription? _notificationsSubscription;

  List<NotificationModel> get notifications => _notifications;
  bool get isLoading => _isLoading;
  String? get error => _error;
  String? get currentUserId => _auth.currentUser?.uid;

  int get unreadCount => _notifications.where((n) => !n.read).length;

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }

  void _setError(String? value) {
    _error = value;
    notifyListeners();
  }

  Future<void> initialize() async {
    // Initialize Firebase Messaging Service
    await FirebaseMessagingService.initialize();

    // Start listening to notifications
    listenToNotifications();
  }

  Future<void> _updateFcmToken(String token) async {
    final userId = currentUserId;
    if (userId == null) return;

    try {
      await _firestore.collection('users').doc(userId).update({
        'fcmToken': token,
        'lastTokenUpdate': DateTime.now().millisecondsSinceEpoch,
      });
    } catch (_) {}
  }

  void listenToNotifications() {
    final userId = currentUserId;
    if (userId == null) return;

    _notificationsSubscription?.cancel();
    _notificationsSubscription = _firestore
        .collection('notifications')
        .where('userId', isEqualTo: userId)
        .orderBy('timestamp', descending: true)
        .limit(50)
        .snapshots()
        .listen(
          (snapshot) {
            _notifications = snapshot.docs
                .map((doc) => NotificationModel.fromFirestore(doc))
                .toList();
            notifyListeners();
          },
          onError: (e) {
            _setError('Failed to load notifications');
          },
        );
  }

  Future<void> loadNotifications() async {
    try {
      _setLoading(true);
      final userId = currentUserId;
      if (userId == null) return;

      final snapshot = await _firestore
          .collection('notifications')
          .where('userId', isEqualTo: userId)
          .orderBy('timestamp', descending: true)
          .limit(50)
          .get();

      _notifications = snapshot.docs
          .map((doc) => NotificationModel.fromFirestore(doc))
          .toList();
    } catch (e) {
      _setError('Failed to load notifications');
    } finally {
      _setLoading(false);
    }
  }

  Future<void> markAsRead(String notificationId) async {
    try {
      await _firestore.collection('notifications').doc(notificationId).update({
        'read': true,
      });
    } catch (_) {}
  }

  Future<void> markAllAsRead() async {
    try {
      final userId = currentUserId;
      if (userId == null) return;

      final batch = _firestore.batch();
      final unreadNotifications = _notifications.where((n) => !n.read);

      for (final notification in unreadNotifications) {
        if (notification.id != null) {
          final ref = _firestore
              .collection('notifications')
              .doc(notification.id);
          batch.update(ref, {'read': true});
        }
      }

      await batch.commit();
    } catch (e) {
      _setError('Failed to mark notifications as read');
    }
  }

  Future<void> deleteNotification(String notificationId) async {
    try {
      await _firestore.collection('notifications').doc(notificationId).delete();
    } catch (e) {
      _setError('Failed to delete notification');
    }
  }

  Future<void> clearAllNotifications() async {
    try {
      final userId = currentUserId;
      if (userId == null) return;

      final batch = _firestore.batch();

      for (final notification in _notifications) {
        if (notification.id != null) {
          final ref = _firestore
              .collection('notifications')
              .doc(notification.id);
          batch.delete(ref);
        }
      }

      await batch.commit();
    } catch (e) {
      _setError('Failed to clear notifications');
    }
  }

  Future<void> createNotification({
    required String userId,
    required String title,
    required String message,
    required String type,
    String? referenceId,
  }) async {
    try {
      final notification = NotificationModel(
        userId: userId,
        title: title,
        message: message,
        type: type,
        referenceId: referenceId,
        timestamp: DateTime.now(),
      );

      await _firestore.collection('notifications').add(notification.toMap());
    } catch (_) {}
  }

  void stopListening() {
    _notificationsSubscription?.cancel();
  }

  void clearError() => _setError(null);

  @override
  void dispose() {
    stopListening();
    super.dispose();
  }
}
