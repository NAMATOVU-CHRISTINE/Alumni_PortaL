import 'package:flutter/foundation.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/models/badge_model.dart';
import 'package:alumni_portal/services/notification_service.dart';

class GamificationProvider with ChangeNotifier {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;

  UserAchievements _achievements = UserAchievements();
  List<BadgeModel> _earnedBadges = [];
  List<Map<String, dynamic>> _profileViewers = [];
  bool _isLoading = false;

  UserAchievements get achievements => _achievements;
  List<BadgeModel> get earnedBadges => _earnedBadges;
  List<Map<String, dynamic>> get profileViewers => _profileViewers;
  bool get isLoading => _isLoading;
  int get profileViewCount => _profileViewers.length;

  String? get currentUserId => _auth.currentUser?.uid;

  Future<void> loadAchievements() async {
    try {
      _isLoading = true;
      notifyListeners();

      final userId = currentUserId;
      if (userId == null) return;

      final doc = await _firestore.collection('users').doc(userId).get();
      if (doc.exists) {
        final data = doc.data()!;
        _achievements = UserAchievements.fromMap(data['achievements'] ?? {});

        // Load earned badges
        _earnedBadges = BadgeModel.allBadges
            .where((b) => _achievements.earnedBadgeIds.contains(b.id))
            .toList();
      }
    } catch (e) {
      debugPrint('Error loading achievements: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> addPoints(int points, String action) async {
    try {
      final userId = currentUserId;
      if (userId == null) return;

      await _firestore.collection('users').doc(userId).update({
        'achievements.totalPoints': FieldValue.increment(points),
        'achievements.lastAction': action,
        'achievements.lastActionAt': DateTime.now().millisecondsSinceEpoch,
      });

      await loadAchievements();
      await checkAndAwardBadges();
    } catch (e) {
      debugPrint('Error adding points: $e');
    }
  }

  Future<void> incrementStat(String stat) async {
    try {
      final userId = currentUserId;
      if (userId == null) return;

      await _firestore.collection('users').doc(userId).update({
        'achievements.$stat': FieldValue.increment(1),
      });

      await loadAchievements();
      await checkAndAwardBadges();
    } catch (e) {
      debugPrint('Error incrementing stat: $e');
    }
  }

  Future<void> checkAndAwardBadges() async {
    try {
      final userId = currentUserId;
      if (userId == null) return;

      final newBadges = <String>[];

      for (final badge in BadgeModel.allBadges) {
        if (_achievements.earnedBadgeIds.contains(badge.id)) continue;

        bool earned = false;
        switch (badge.id) {
          case 'newcomer':
            earned = true;
            break;
          case 'first_post':
            earned = _achievements.postsCount >= 1;
            break;
          case 'connector':
            earned = _achievements.connectionsCount >= 10;
            break;
          case 'super_connector':
            earned = _achievements.connectionsCount >= 50;
            break;
          case 'mentor':
            earned = _achievements.menteeCount >= 1;
            break;
          case 'top_mentor':
            earned = _achievements.menteeCount >= 5;
            break;
          case 'job_poster':
            earned = _achievements.jobsPosted >= 1;
            break;
          case 'event_organizer':
            earned = _achievements.eventsCreated >= 1;
            break;
          case 'top_contributor':
            earned = _achievements.postsCount >= 50;
            break;
          case 'influencer':
            earned = _achievements.reactionsReceived >= 100;
            break;
        }

        if (earned) {
          newBadges.add(badge.id);
        }
      }

      if (newBadges.isNotEmpty) {
        await _firestore.collection('users').doc(userId).update({
          'achievements.earnedBadgeIds': FieldValue.arrayUnion(newBadges),
        });
        await loadAchievements();
      }
    } catch (e) {
      debugPrint('Error checking badges: $e');
    }
  }

  // Profile Views
  Future<void> loadProfileViewers() async {
    try {
      final userId = currentUserId;
      if (userId == null) return;

      final snapshot = await _firestore
          .collection('users')
          .doc(userId)
          .collection('profileViews')
          .orderBy('viewedAt', descending: true)
          .limit(20)
          .get();

      _profileViewers = [];
      for (final doc in snapshot.docs) {
        final data = doc.data();
        // Get viewer details
        final viewerDoc =
            await _firestore.collection('users').doc(data['viewerId']).get();
        if (viewerDoc.exists) {
          final viewerData = viewerDoc.data()!;
          _profileViewers.add({
            'viewerId': data['viewerId'],
            'viewerName': viewerData['fullName'] ?? 'Alumni',
            'viewerImageUrl': viewerData['profileImageUrl'],
            'viewerTitle': viewerData['currentJob'],
            'viewedAt': DateTime.fromMillisecondsSinceEpoch(data['viewedAt']),
          });
        }
      }
      notifyListeners();
    } catch (e) {
      debugPrint('Error loading profile viewers: $e');
    }
  }

  Future<void> recordProfileView(String viewedUserId) async {
    try {
      final viewerId = currentUserId;
      if (viewerId == null || viewerId == viewedUserId) return;

      // Check if already viewed recently (within 24 hours)
      final existingView = await _firestore
          .collection('users')
          .doc(viewedUserId)
          .collection('profileViews')
          .doc(viewerId)
          .get();

      if (existingView.exists) {
        final lastView = DateTime.fromMillisecondsSinceEpoch(
          existingView.data()!['viewedAt'],
        );
        if (DateTime.now().difference(lastView).inHours < 24) return;
      }

      await _firestore
          .collection('users')
          .doc(viewedUserId)
          .collection('profileViews')
          .doc(viewerId)
          .set({
        'viewerId': viewerId,
        'viewedAt': DateTime.now().millisecondsSinceEpoch,
      });

      // Send notification
      final viewerDoc =
          await _firestore.collection('users').doc(viewerId).get();
      final viewerName = viewerDoc.data()?['fullName'] ?? 'Someone';
      NotificationService.notifyProfileView(
        viewedUserId: viewedUserId,
        viewerName: viewerName,
      );

      // Increment view count
      await _firestore.collection('users').doc(viewedUserId).update({
        'profileViewCount': FieldValue.increment(1),
      });
    } catch (e) {
      debugPrint('Error recording profile view: $e');
    }
  }
}
