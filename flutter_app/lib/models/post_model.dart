import 'package:cloud_firestore/cloud_firestore.dart';

class PollData {
  final List<PollOption> options;
  final DateTime? expiresAt;
  final bool allowMultipleChoices;
  final int totalVotes;

  PollData({
    required this.options,
    this.expiresAt,
    this.allowMultipleChoices = false,
    this.totalVotes = 0,
  });

  factory PollData.fromMap(Map<String, dynamic> data) {
    return PollData(
      options: (data['options'] as List<dynamic>)
          .map((option) => PollOption.fromMap(option as Map<String, dynamic>))
          .toList(),
      expiresAt: data['expiresAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['expiresAt'])
          : null,
      allowMultipleChoices: data['allowMultipleChoices'] ?? false,
      totalVotes: data['totalVotes'] ?? 0,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'options': options.map((option) => option.toMap()).toList(),
      'expiresAt': expiresAt?.millisecondsSinceEpoch,
      'allowMultipleChoices': allowMultipleChoices,
      'totalVotes': totalVotes,
    };
  }
}

class PollOption {
  final String text;
  final List<String> voters;
  final int votes;

  PollOption({
    required this.text,
    this.voters = const [],
    this.votes = 0,
  });

  factory PollOption.fromMap(Map<String, dynamic> data) {
    return PollOption(
      text: data['text'] ?? '',
      voters: List<String>.from(data['voters'] ?? []),
      votes: data['votes'] ?? 0,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'text': text,
      'voters': voters,
      'votes': votes,
    };
  }
}

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
  final String
      postType; // 'update', 'achievement', 'job_update', 'article', 'poll'
  final PollData? pollData;

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
    this.pollData,
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
      pollData: data['pollData'] != null
          ? PollData.fromMap(data['pollData'] as Map<String, dynamic>)
          : null,
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
      'pollData': pollData?.toMap(),
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
