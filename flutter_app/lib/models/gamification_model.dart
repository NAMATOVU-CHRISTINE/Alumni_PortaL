import 'package:cloud_firestore/cloud_firestore.dart';

class BadgeModel {
  final String id;
  final String name;
  final String description;
  final String iconUrl;
  final String category;
  final int pointsRequired;
  final String rarity; // common, rare, epic, legendary
  final List<String> requirements;
  final DateTime? unlockedAt;
  final bool isUnlocked;

  BadgeModel({
    required this.id,
    required this.name,
    required this.description,
    required this.iconUrl,
    required this.category,
    required this.pointsRequired,
    required this.rarity,
    this.requirements = const [],
    this.unlockedAt,
    this.isUnlocked = false,
  });

  factory BadgeModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return BadgeModel(
      id: doc.id,
      name: data['name'] ?? '',
      description: data['description'] ?? '',
      iconUrl: data['iconUrl'] ?? '',
      category: data['category'] ?? '',
      pointsRequired: data['pointsRequired'] ?? 0,
      rarity: data['rarity'] ?? 'common',
      requirements: List<String>.from(data['requirements'] ?? []),
      unlockedAt: data['unlockedAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['unlockedAt'])
          : null,
      isUnlocked: data['isUnlocked'] ?? false,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'name': name,
      'description': description,
      'iconUrl': iconUrl,
      'category': category,
      'pointsRequired': pointsRequired,
      'rarity': rarity,
      'requirements': requirements,
      'unlockedAt': unlockedAt?.millisecondsSinceEpoch,
      'isUnlocked': isUnlocked,
    };
  }
}

class UserStatsModel {
  final String userId;
  final int totalPoints;
  final int level;
  final int postsCreated;
  final int commentsPosted;
  final int eventsAttended;
  final int connectionsAdded;
  final int mentorshipSessions;
  final int donationsMade;
  final int discussionsStarted;
  final List<String> unlockedBadges;
  final Map<String, int> categoryPoints;
  final DateTime lastUpdated;

  UserStatsModel({
    required this.userId,
    this.totalPoints = 0,
    this.level = 1,
    this.postsCreated = 0,
    this.commentsPosted = 0,
    this.eventsAttended = 0,
    this.connectionsAdded = 0,
    this.mentorshipSessions = 0,
    this.donationsMade = 0,
    this.discussionsStarted = 0,
    this.unlockedBadges = const [],
    this.categoryPoints = const {},
    required this.lastUpdated,
  });

  factory UserStatsModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return UserStatsModel(
      userId: doc.id,
      totalPoints: data['totalPoints'] ?? 0,
      level: data['level'] ?? 1,
      postsCreated: data['postsCreated'] ?? 0,
      commentsPosted: data['commentsPosted'] ?? 0,
      eventsAttended: data['eventsAttended'] ?? 0,
      connectionsAdded: data['connectionsAdded'] ?? 0,
      mentorshipSessions: data['mentorshipSessions'] ?? 0,
      donationsMade: data['donationsMade'] ?? 0,
      discussionsStarted: data['discussionsStarted'] ?? 0,
      unlockedBadges: List<String>.from(data['unlockedBadges'] ?? []),
      categoryPoints: Map<String, int>.from(data['categoryPoints'] ?? {}),
      lastUpdated: data['lastUpdated'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['lastUpdated'])
          : DateTime.now(),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'totalPoints': totalPoints,
      'level': level,
      'postsCreated': postsCreated,
      'commentsPosted': commentsPosted,
      'eventsAttended': eventsAttended,
      'connectionsAdded': connectionsAdded,
      'mentorshipSessions': mentorshipSessions,
      'donationsMade': donationsMade,
      'discussionsStarted': discussionsStarted,
      'unlockedBadges': unlockedBadges,
      'categoryPoints': categoryPoints,
      'lastUpdated': lastUpdated.millisecondsSinceEpoch,
    };
  }
}

class ChallengeModel {
  final String id;
  final String title;
  final String description;
  final String type; // daily, weekly, monthly, special
  final int pointsReward;
  final Map<String, dynamic> requirements;
  final DateTime startDate;
  final DateTime endDate;
  final bool isActive;
  final List<String> participants;
  final String? badgeReward;

  ChallengeModel({
    required this.id,
    required this.title,
    required this.description,
    required this.type,
    required this.pointsReward,
    required this.requirements,
    required this.startDate,
    required this.endDate,
    this.isActive = true,
    this.participants = const [],
    this.badgeReward,
  });

  factory ChallengeModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return ChallengeModel(
      id: doc.id,
      title: data['title'] ?? '',
      description: data['description'] ?? '',
      type: data['type'] ?? 'daily',
      pointsReward: data['pointsReward'] ?? 0,
      requirements: Map<String, dynamic>.from(data['requirements'] ?? {}),
      startDate: data['startDate'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['startDate'])
          : DateTime.now(),
      endDate: data['endDate'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['endDate'])
          : DateTime.now().add(const Duration(days: 1)),
      isActive: data['isActive'] ?? true,
      participants: List<String>.from(data['participants'] ?? []),
      badgeReward: data['badgeReward'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'title': title,
      'description': description,
      'type': type,
      'pointsReward': pointsReward,
      'requirements': requirements,
      'startDate': startDate.millisecondsSinceEpoch,
      'endDate': endDate.millisecondsSinceEpoch,
      'isActive': isActive,
      'participants': participants,
      'badgeReward': badgeReward,
    };
  }
}

class LeaderboardEntry {
  final String userId;
  final String userName;
  final String? userImage;
  final int points;
  final int level;
  final int rank;
  final String category; // overall, monthly, weekly

  LeaderboardEntry({
    required this.userId,
    required this.userName,
    this.userImage,
    required this.points,
    required this.level,
    required this.rank,
    required this.category,
  });

  factory LeaderboardEntry.fromMap(Map<String, dynamic> map) {
    return LeaderboardEntry(
      userId: map['userId'] ?? '',
      userName: map['userName'] ?? '',
      userImage: map['userImage'],
      points: map['points'] ?? 0,
      level: map['level'] ?? 1,
      rank: map['rank'] ?? 0,
      category: map['category'] ?? 'overall',
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'userId': userId,
      'userName': userName,
      'userImage': userImage,
      'points': points,
      'level': level,
      'rank': rank,
      'category': category,
    };
  }
}
