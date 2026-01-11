import 'package:cloud_firestore/cloud_firestore.dart';

class BadgeModel {
  final String id;
  final String name;
  final String description;
  final String icon;
  final String category; // 'engagement', 'mentorship', 'career', 'community'
  final int pointsRequired;
  final DateTime? earnedAt;

  BadgeModel({
    required this.id,
    required this.name,
    required this.description,
    required this.icon,
    required this.category,
    required this.pointsRequired,
    this.earnedAt,
  });

  factory BadgeModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return BadgeModel(
      id: doc.id,
      name: data['name'] ?? '',
      description: data['description'] ?? '',
      icon: data['icon'] ?? '🏆',
      category: data['category'] ?? 'engagement',
      pointsRequired: data['pointsRequired'] ?? 0,
      earnedAt: data['earnedAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['earnedAt'])
          : null,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'name': name,
      'description': description,
      'icon': icon,
      'category': category,
      'pointsRequired': pointsRequired,
      'earnedAt': earnedAt?.millisecondsSinceEpoch,
    };
  }

  // Predefined badges
  static List<BadgeModel> get allBadges => [
        BadgeModel(
            id: 'newcomer',
            name: 'Newcomer',
            description: 'Welcome to Alumni Portal!',
            icon: '👋',
            category: 'engagement',
            pointsRequired: 0),
        BadgeModel(
            id: 'profile_complete',
            name: 'Profile Pro',
            description: 'Completed your profile 100%',
            icon: '✨',
            category: 'engagement',
            pointsRequired: 10),
        BadgeModel(
            id: 'first_post',
            name: 'First Voice',
            description: 'Made your first post',
            icon: '📝',
            category: 'engagement',
            pointsRequired: 5),
        BadgeModel(
            id: 'connector',
            name: 'Connector',
            description: 'Connected with 10 alumni',
            icon: '🤝',
            category: 'community',
            pointsRequired: 20),
        BadgeModel(
            id: 'super_connector',
            name: 'Super Connector',
            description: 'Connected with 50 alumni',
            icon: '🌟',
            category: 'community',
            pointsRequired: 100),
        BadgeModel(
            id: 'mentor',
            name: 'Mentor',
            description: 'Became a mentor',
            icon: '🎓',
            category: 'mentorship',
            pointsRequired: 30),
        BadgeModel(
            id: 'top_mentor',
            name: 'Top Mentor',
            description: 'Mentored 5+ students',
            icon: '👨‍🏫',
            category: 'mentorship',
            pointsRequired: 100),
        BadgeModel(
            id: 'job_poster',
            name: 'Opportunity Sharer',
            description: 'Posted a job opportunity',
            icon: '💼',
            category: 'career',
            pointsRequired: 15),
        BadgeModel(
            id: 'event_organizer',
            name: 'Event Organizer',
            description: 'Created an event',
            icon: '📅',
            category: 'community',
            pointsRequired: 20),
        BadgeModel(
            id: 'top_contributor',
            name: 'Top Contributor',
            description: 'Made 50+ posts',
            icon: '🏆',
            category: 'engagement',
            pointsRequired: 200),
        BadgeModel(
            id: 'influencer',
            name: 'Influencer',
            description: 'Got 100+ reactions on posts',
            icon: '⭐',
            category: 'engagement',
            pointsRequired: 150),
        BadgeModel(
            id: 'helper',
            name: 'Helpful Hand',
            description: 'Answered 10 questions',
            icon: '🙋',
            category: 'community',
            pointsRequired: 50),
      ];
}

class UserAchievements {
  final int totalPoints;
  final int postsCount;
  final int connectionsCount;
  final int reactionsReceived;
  final int menteeCount;
  final int eventsCreated;
  final int jobsPosted;
  final List<String> earnedBadgeIds;

  UserAchievements({
    this.totalPoints = 0,
    this.postsCount = 0,
    this.connectionsCount = 0,
    this.reactionsReceived = 0,
    this.menteeCount = 0,
    this.eventsCreated = 0,
    this.jobsPosted = 0,
    this.earnedBadgeIds = const [],
  });

  factory UserAchievements.fromMap(Map<String, dynamic> data) {
    return UserAchievements(
      totalPoints: data['totalPoints'] ?? 0,
      postsCount: data['postsCount'] ?? 0,
      connectionsCount: data['connectionsCount'] ?? 0,
      reactionsReceived: data['reactionsReceived'] ?? 0,
      menteeCount: data['menteeCount'] ?? 0,
      eventsCreated: data['eventsCreated'] ?? 0,
      jobsPosted: data['jobsPosted'] ?? 0,
      earnedBadgeIds: List<String>.from(data['earnedBadgeIds'] ?? []),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'totalPoints': totalPoints,
      'postsCount': postsCount,
      'connectionsCount': connectionsCount,
      'reactionsReceived': reactionsReceived,
      'menteeCount': menteeCount,
      'eventsCreated': eventsCreated,
      'jobsPosted': jobsPosted,
      'earnedBadgeIds': earnedBadgeIds,
    };
  }
}
