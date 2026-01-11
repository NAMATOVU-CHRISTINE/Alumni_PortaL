import 'package:cloud_firestore/cloud_firestore.dart';

class PostModel {
  final String id;
  final String authorId;
  final String authorName;
  final String? authorImageUrl;
  final String? authorTitle;
  final String content;
  final String? imageUrl;
  final List<String> likes;
  final List<String> celebrates;
  final List<String> supports;
  final int commentCount;
  final DateTime createdAt;
  final String? linkUrl;
  final String postType; // 'update', 'achievement', 'job_update', 'article'

  PostModel({
    required this.id,
    required this.authorId,
    required this.authorName,
    this.authorImageUrl,
    this.authorTitle,
    required this.content,
    this.imageUrl,
    this.likes = const [],
    this.celebrates = const [],
    this.supports = const [],
    this.commentCount = 0,
    required this.createdAt,
    this.linkUrl,
    this.postType = 'update',
  });

  int get totalReactions => likes.length + celebrates.length + supports.length;

  factory PostModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return PostModel(
      id: doc.id,
      authorId: data['authorId'] ?? '',
      authorName: data['authorName'] ?? 'Unknown',
      authorImageUrl: data['authorImageUrl'],
      authorTitle: data['authorTitle'],
      content: data['content'] ?? '',
      imageUrl: data['imageUrl'],
      likes: List<String>.from(data['likes'] ?? []),
      celebrates: List<String>.from(data['celebrates'] ?? []),
      supports: List<String>.from(data['supports'] ?? []),
      commentCount: data['commentCount'] ?? 0,
      createdAt: data['createdAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['createdAt'])
          : DateTime.now(),
      linkUrl: data['linkUrl'],
      postType: data['postType'] ?? 'update',
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'authorId': authorId,
      'authorName': authorName,
      'authorImageUrl': authorImageUrl,
      'authorTitle': authorTitle,
      'content': content,
      'imageUrl': imageUrl,
      'likes': likes,
      'celebrates': celebrates,
      'supports': supports,
      'commentCount': commentCount,
      'createdAt': createdAt.millisecondsSinceEpoch,
      'linkUrl': linkUrl,
      'postType': postType,
    };
  }
}

class CommentModel {
  final String id;
  final String postId;
  final String authorId;
  final String authorName;
  final String? authorImageUrl;
  final String content;
  final DateTime createdAt;
  final List<String> likes;

  CommentModel({
    required this.id,
    required this.postId,
    required this.authorId,
    required this.authorName,
    this.authorImageUrl,
    required this.content,
    required this.createdAt,
    this.likes = const [],
  });

  factory CommentModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return CommentModel(
      id: doc.id,
      postId: data['postId'] ?? '',
      authorId: data['authorId'] ?? '',
      authorName: data['authorName'] ?? 'Unknown',
      authorImageUrl: data['authorImageUrl'],
      content: data['content'] ?? '',
      createdAt: data['createdAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['createdAt'])
          : DateTime.now(),
      likes: List<String>.from(data['likes'] ?? []),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'postId': postId,
      'authorId': authorId,
      'authorName': authorName,
      'authorImageUrl': authorImageUrl,
      'content': content,
      'createdAt': createdAt.millisecondsSinceEpoch,
      'likes': likes,
    };
  }
}
