import 'package:cloud_firestore/cloud_firestore.dart';

class StoryModel {
  final String id;
  final String authorId;
  final String authorName;
  final String? authorImageUrl;
  final String? content;
  final String? imageUrl;
  final String? backgroundColor;
  final DateTime createdAt;
  final DateTime expiresAt;
  final List<String> viewedBy;
  final Map<String, String> reactions; // userId -> emoji

  StoryModel({
    required this.id,
    required this.authorId,
    required this.authorName,
    this.authorImageUrl,
    this.content,
    this.imageUrl,
    this.backgroundColor,
    required this.createdAt,
    required this.expiresAt,
    this.viewedBy = const [],
    this.reactions = const {},
  });

  bool get isExpired => DateTime.now().isAfter(expiresAt);
  int get viewCount => viewedBy.length;

  Duration get timeRemaining => expiresAt.difference(DateTime.now());
  String get timeRemainingText {
    final remaining = timeRemaining;
    if (remaining.isNegative) return 'Expired';
    if (remaining.inHours > 0) return '${remaining.inHours}h left';
    return '${remaining.inMinutes}m left';
  }

  factory StoryModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return StoryModel(
      id: doc.id,
      authorId: data['authorId'] ?? '',
      authorName: data['authorName'] ?? 'Unknown',
      authorImageUrl: data['authorImageUrl'],
      content: data['content'],
      imageUrl: data['imageUrl'],
      backgroundColor: data['backgroundColor'],
      createdAt: data['createdAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['createdAt'])
          : DateTime.now(),
      expiresAt: data['expiresAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['expiresAt'])
          : DateTime.now().add(const Duration(hours: 24)),
      viewedBy: List<String>.from(data['viewedBy'] ?? []),
      reactions: Map<String, String>.from(data['reactions'] ?? {}),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'authorId': authorId,
      'authorName': authorName,
      'authorImageUrl': authorImageUrl,
      'content': content,
      'imageUrl': imageUrl,
      'backgroundColor': backgroundColor,
      'createdAt': createdAt.millisecondsSinceEpoch,
      'expiresAt': expiresAt.millisecondsSinceEpoch,
      'viewedBy': viewedBy,
      'reactions': reactions,
    };
  }
}

class UserStories {
  final String oderId;
  final String authorName;
  final String? authorImageUrl;
  final List<StoryModel> stories;
  final bool hasUnviewed;

  UserStories({
    required this.oderId,
    required this.authorName,
    this.authorImageUrl,
    required this.stories,
    required this.hasUnviewed,
  });
}
