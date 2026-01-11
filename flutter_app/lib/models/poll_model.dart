import 'package:cloud_firestore/cloud_firestore.dart';

class PollModel {
  final String id;
  final String authorId;
  final String authorName;
  final String? authorImageUrl;
  final String question;
  final List<PollOption> options;
  final DateTime createdAt;
  final DateTime expiresAt;
  final bool isActive;
  final List<String> votedUserIds;

  PollModel({
    required this.id,
    required this.authorId,
    required this.authorName,
    this.authorImageUrl,
    required this.question,
    required this.options,
    required this.createdAt,
    required this.expiresAt,
    this.isActive = true,
    this.votedUserIds = const [],
  });

  int get totalVotes => options.fold(0, (sum, opt) => sum + opt.votes);
  bool get isExpired => DateTime.now().isAfter(expiresAt);

  factory PollModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return PollModel(
      id: doc.id,
      authorId: data['authorId'] ?? '',
      authorName: data['authorName'] ?? 'Unknown',
      authorImageUrl: data['authorImageUrl'],
      question: data['question'] ?? '',
      options: (data['options'] as List<dynamic>?)
              ?.map((o) => PollOption.fromMap(o as Map<String, dynamic>))
              .toList() ??
          [],
      createdAt: data['createdAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['createdAt'])
          : DateTime.now(),
      expiresAt: data['expiresAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['expiresAt'])
          : DateTime.now().add(const Duration(days: 7)),
      isActive: data['isActive'] ?? true,
      votedUserIds: List<String>.from(data['votedUserIds'] ?? []),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'authorId': authorId,
      'authorName': authorName,
      'authorImageUrl': authorImageUrl,
      'question': question,
      'options': options.map((o) => o.toMap()).toList(),
      'createdAt': createdAt.millisecondsSinceEpoch,
      'expiresAt': expiresAt.millisecondsSinceEpoch,
      'isActive': isActive,
      'votedUserIds': votedUserIds,
    };
  }
}

class PollOption {
  final String id;
  final String text;
  final int votes;

  PollOption({
    required this.id,
    required this.text,
    this.votes = 0,
  });

  factory PollOption.fromMap(Map<String, dynamic> data) {
    return PollOption(
      id: data['id'] ?? '',
      text: data['text'] ?? '',
      votes: data['votes'] ?? 0,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'text': text,
      'votes': votes,
    };
  }
}
