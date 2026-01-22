import 'dart:async';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';

class ActivityService {
  static final ActivityService _instance = ActivityService._internal();
  factory ActivityService() => _instance;
  ActivityService._internal();

  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;
  Timer? _activityTimer;

  void startActivityTracking() {
    // Update user activity every 2 minutes
    _activityTimer = Timer.periodic(const Duration(minutes: 2), (timer) {
      _updateUserActivity();
    });

    // Initial update
    _updateUserActivity();
  }

  void stopActivityTracking() {
    _activityTimer?.cancel();
    _setUserOffline();
  }

  Future<void> _updateUserActivity() async {
    try {
      final userId = _auth.currentUser?.uid;
      if (userId == null) return;

      await _firestore.collection('users').doc(userId).update({
        'lastActive': DateTime.now().millisecondsSinceEpoch,
        'isOnline': true,
      });
    } catch (e) {
      // Silently handle errors for activity updates
    }
  }

  Future<void> _setUserOffline() async {
    try {
      final userId = _auth.currentUser?.uid;
      if (userId == null) return;

      await _firestore.collection('users').doc(userId).update({
        'isOnline': false,
        'lastActive': DateTime.now().millisecondsSinceEpoch,
      });
    } catch (e) {
      // Silently handle errors
    }
  }

  // Get real-time active users count (active in last 5 minutes)
  Stream<int> getActiveUsersCount() {
    final fiveMinutesAgo = DateTime.now().subtract(const Duration(minutes: 5));

    return _firestore
        .collection('users')
        .where('lastActive',
            isGreaterThan: fiveMinutesAgo.millisecondsSinceEpoch)
        .snapshots()
        .map((snapshot) => snapshot.docs.length);
  }

  // Get real-time online users count (active in last 24 hours)
  Stream<int> getOnlineUsersCount() {
    final twentyFourHoursAgo =
        DateTime.now().subtract(const Duration(hours: 24));

    return _firestore
        .collection('users')
        .where('lastActive',
            isGreaterThan: twentyFourHoursAgo.millisecondsSinceEpoch)
        .snapshots()
        .map((snapshot) => snapshot.docs.length);
  }
}
