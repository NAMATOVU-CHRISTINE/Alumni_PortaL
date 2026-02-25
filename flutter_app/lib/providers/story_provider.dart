import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/models/story_model.dart';
import 'package:alumni_portal/services/notification_service.dart';
import 'package:alumni_portal/services/cloudinary_service.dart';
import 'package:alumni_portal/services/image_compression_service.dart';

class StoryProvider with ChangeNotifier {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;

  List<UserStories> _userStories = [];
  List<StoryModel> _myStories = [];
  bool _isLoading = false;

  List<UserStories> get userStories => _userStories;
  List<StoryModel> get myStories => _myStories;
  bool get isLoading => _isLoading;
  String? get currentUserId => _auth.currentUser?.uid;

  Future<void> loadStories() async {
    try {
      _isLoading = true;
      notifyListeners();

      final now = DateTime.now();
      final snapshot = await _firestore
          .collection('stories')
          .where('expiresAt', isGreaterThan: now.millisecondsSinceEpoch)
          .orderBy('expiresAt')
          .orderBy('createdAt', descending: true)
          .get();

      final stories =
          snapshot.docs.map((doc) => StoryModel.fromFirestore(doc)).toList();

      // Group by author
      final Map<String, List<StoryModel>> grouped = {};
      for (final story in stories) {
        grouped.putIfAbsent(story.authorId, () => []).add(story);
      }

      _userStories = grouped.entries.map((entry) {
        final authorStories = entry.value;
        final hasUnviewed = authorStories.any(
          (s) => !s.viewedBy.contains(currentUserId),
        );
        return UserStories(
          oderId: entry.key,
          authorName: authorStories.first.authorName,
          authorImageUrl: authorStories.first.authorImageUrl,
          stories: authorStories,
          hasUnviewed: hasUnviewed,
        );
      }).toList();

      // Sort: unviewed first, then by most recent
      _userStories.sort((a, b) {
        if (a.hasUnviewed && !b.hasUnviewed) return -1;
        if (!a.hasUnviewed && b.hasUnviewed) return 1;
        return b.stories.first.createdAt.compareTo(a.stories.first.createdAt);
      });

      // Load my stories
      _myStories = stories.where((s) => s.authorId == currentUserId).toList();
    } catch (e) {
      debugPrint('Error loading stories: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> createStory({
    String? content,
    File? imageFile,
    String backgroundColor = '#1E88E5',
  }) async {
    try {
      _isLoading = true;
      notifyListeners();

      final user = _auth.currentUser;
      if (user == null) return false;

      final userDoc = await _firestore.collection('users').doc(user.uid).get();
      final userData = userDoc.data() ?? {};

      String? imageUrl;
      if (imageFile != null) {
        // Compress image before upload
        final compressedImage = await ImageCompressionService.compressStoryImage(imageFile);
        
        imageUrl = await CloudinaryService.uploadImage(
          compressedImage,
          folder: 'stories',
        );
        if (imageUrl == null) {
          throw Exception('Failed to upload image');
        }
      }

      final story = StoryModel(
        id: '',
        authorId: user.uid,
        authorName: userData['fullName'] ?? 'Alumni',
        authorImageUrl: userData['profileImageUrl'],
        content: content,
        imageUrl: imageUrl,
        backgroundColor: backgroundColor,
        createdAt: DateTime.now(),
        expiresAt: DateTime.now().add(const Duration(hours: 24)),
      );

      await _firestore.collection('stories').add(story.toMap());
      await loadStories();
      return true;
    } catch (e) {
      debugPrint('Error creating story: $e');
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> markAsViewed(String storyId) async {
    try {
      final userId = currentUserId;
      if (userId == null) return;

      // Get story to check author
      final storyDoc =
          await _firestore.collection('stories').doc(storyId).get();
      final storyData = storyDoc.data();
      final authorId = storyData?['authorId'] as String?;
      final viewedBy = List<String>.from(storyData?['viewedBy'] ?? []);

      // Only notify if not already viewed
      if (authorId != null &&
          authorId != userId &&
          !viewedBy.contains(userId)) {
        final userDoc = await _firestore.collection('users').doc(userId).get();
        final viewerName = userDoc.data()?['fullName'] ?? 'Someone';
        NotificationService.notifyStoryView(
          storyAuthorId: authorId,
          viewerName: viewerName,
        );
      }

      await _firestore.collection('stories').doc(storyId).update({
        'viewedBy': FieldValue.arrayUnion([userId]),
      });
    } catch (e) {
      debugPrint('Error marking story as viewed: $e');
    }
  }

  Future<void> deleteStory(String storyId) async {
    try {
      await _firestore.collection('stories').doc(storyId).delete();
      await loadStories();
    } catch (e) {
      debugPrint('Error deleting story: $e');
    }
  }
}
