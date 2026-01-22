import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/models/post_model.dart';
import 'package:alumni_portal/services/notification_service.dart';
import 'package:alumni_portal/services/cloudinary_service.dart';

class PostProvider with ChangeNotifier {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;

  List<PostModel> _posts = [];
  List<CommentModel> _comments = [];
  bool _isLoading = false;
  String? _error;

  List<PostModel> get posts => _posts;
  List<CommentModel> get comments => _comments;
  bool get isLoading => _isLoading;
  String? get error => _error;

  String? get currentUserId => _auth.currentUser?.uid;

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }

  Future<void> loadPosts() async {
    try {
      _setLoading(true);
      final snapshot = await _firestore
          .collection('posts')
          .orderBy('createdAt', descending: true)
          .limit(50)
          .get();

      _posts =
          snapshot.docs.map((doc) => PostModel.fromFirestore(doc)).toList();
    } catch (e) {
      _error = 'Failed to load posts';
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> createPost({
    required String content,
    File? imageFile,
    String postType = 'update',
    String? linkUrl,
    Map<String, dynamic>? pollData,
  }) async {
    try {
      _setLoading(true);
      final user = _auth.currentUser;
      if (user == null) return false;

      // Get user data
      final userDoc = await _firestore.collection('users').doc(user.uid).get();
      final userData = userDoc.data() ?? {};

      String? imageUrl;
      if (imageFile != null) {
        imageUrl = await CloudinaryService.uploadImage(
          imageFile,
          folder: 'posts',
        );
        if (imageUrl == null) {
          throw Exception('Failed to upload image');
        }
      }

      final post = PostModel(
        id: '',
        authorId: user.uid,
        authorName: userData['fullName'] ?? 'Alumni',
        authorImageUrl: userData['profileImageUrl'],
        authorTitle: userData['currentJob'] ?? userData['major'],
        content: content,
        imageUrl: imageUrl,
        createdAt: DateTime.now(),
        postType: postType,
        linkUrl: linkUrl,
        pollData: pollData != null ? PollData.fromMap(pollData) : null,
      );

      await _firestore.collection('posts').add(post.toMap());
      await loadPosts();
      return true;
    } catch (e) {
      _error = 'Failed to create post';
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<void> toggleReaction(String postId, String reactionType) async {
    try {
      final userId = _auth.currentUser?.uid;
      if (userId == null) return;

      final postRef = _firestore.collection('posts').doc(postId);
      final postDoc = await postRef.get();
      if (!postDoc.exists) return;

      final data = postDoc.data()!;
      List<String> reactions = List<String>.from(data[reactionType] ?? []);

      if (reactions.contains(userId)) {
        reactions.remove(userId);
      } else {
        // Remove from other reaction types first
        for (final type in ['likes', 'celebrates', 'supports']) {
          if (type != reactionType) {
            List<String> otherReactions = List<String>.from(data[type] ?? []);
            if (otherReactions.contains(userId)) {
              otherReactions.remove(userId);
              await postRef.update({type: otherReactions});
            }
          }
        }
        reactions.add(userId);

        // Send notification to post author
        final authorId = data['authorId'] as String?;
        if (authorId != null && authorId != userId) {
          final userDoc =
              await _firestore.collection('users').doc(userId).get();
          final userName = userDoc.data()?['fullName'] ?? 'Someone';
          NotificationService.notifyPostReaction(
            postAuthorId: authorId,
            reactorName: userName,
            reactionType: reactionType,
            postId: postId,
          );
        }
      }

      await postRef.update({reactionType: reactions});

      // Update local state
      final index = _posts.indexWhere((p) => p.id == postId);
      if (index != -1) {
        await loadPosts();
      }
    } catch (e) {
      _error = 'Failed to update reaction';
    }
  }

  Future<void> loadComments(String postId) async {
    try {
      final snapshot = await _firestore
          .collection('posts')
          .doc(postId)
          .collection('comments')
          .orderBy('createdAt', descending: false)
          .get();

      _comments =
          snapshot.docs.map((doc) => CommentModel.fromFirestore(doc)).toList();
      notifyListeners();
    } catch (e) {
      _error = 'Failed to load comments';
    }
  }

  Future<bool> addComment(String postId, String content) async {
    try {
      final user = _auth.currentUser;
      if (user == null) return false;

      final userDoc = await _firestore.collection('users').doc(user.uid).get();
      final userData = userDoc.data() ?? {};

      final comment = CommentModel(
        id: '',
        postId: postId,
        authorId: user.uid,
        authorName: userData['fullName'] ?? 'Alumni',
        authorImageUrl: userData['profileImageUrl'],
        content: content,
        createdAt: DateTime.now(),
      );

      await _firestore
          .collection('posts')
          .doc(postId)
          .collection('comments')
          .add(comment.toMap());

      // Update comment count
      await _firestore.collection('posts').doc(postId).update({
        'commentCount': FieldValue.increment(1),
      });

      // Send notification to post author
      final postDoc = await _firestore.collection('posts').doc(postId).get();
      final postAuthorId = postDoc.data()?['authorId'] as String?;
      if (postAuthorId != null && postAuthorId != user.uid) {
        NotificationService.notifyPostComment(
          postAuthorId: postAuthorId,
          commenterName: userData['fullName'] ?? 'Someone',
          postId: postId,
        );
      }

      await loadComments(postId);
      await loadPosts();
      return true;
    } catch (e) {
      _error = 'Failed to add comment';
      return false;
    }
  }

  Future<bool> deletePost(String postId) async {
    try {
      final userId = _auth.currentUser?.uid;
      if (userId == null) return false;

      final postDoc = await _firestore.collection('posts').doc(postId).get();
      if (!postDoc.exists) return false;

      final postData = postDoc.data()!;
      if (postData['authorId'] != userId) return false;

      await _firestore.collection('posts').doc(postId).delete();
      await loadPosts();
      return true;
    } catch (e) {
      _error = 'Failed to delete post';
      return false;
    }
  }

  bool hasUserReacted(PostModel post, String reactionType) {
    final userId = currentUserId;
    if (userId == null) return false;

    switch (reactionType) {
      case 'likes':
        return post.likes.contains(userId);
      case 'celebrates':
        return post.celebrates.contains(userId);
      case 'supports':
        return post.supports.contains(userId);
      default:
        return false;
    }
  }

  void clearError() => _error = null;
}
