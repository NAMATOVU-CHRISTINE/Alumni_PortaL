import 'package:cloud_firestore/cloud_firestore.dart';

class GroupModel {
  final String? groupId;
  final String? groupName;
  final String? description;
  final String? groupType;
  final String? imageUrl;
  final String? creatorId;
  final String? creatorName;
  final List<String> memberIds;
  final List<String> adminIds;
  final int memberCount;
  final DateTime? createdAt;
  final DateTime? lastActivityAt;
  final bool isPrivate;
  final String? graduationYear;
  final String? department;

  GroupModel({
    this.groupId,
    this.groupName,
    this.description,
    this.groupType,
    this.imageUrl,
    this.creatorId,
    this.creatorName,
    this.memberIds = const [],
    this.adminIds = const [],
    this.memberCount = 0,
    this.createdAt,
    this.lastActivityAt,
    this.isPrivate = false,
    this.graduationYear,
    this.department,
  });

  factory GroupModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>? ?? {};
    return GroupModel(
      groupId: doc.id,
      groupName: data['groupName'] as String?,
      description: data['description'] as String?,
      groupType: data['groupType'] as String?,
      imageUrl: data['imageUrl'] as String?,
      creatorId: data['creatorId'] as String?,
      creatorName: data['creatorName'] as String?,
      memberIds: List<String>.from(data['memberIds'] ?? []),
      adminIds: List<String>.from(data['adminIds'] ?? []),
      memberCount: data['memberCount'] ?? 0,
      createdAt: _parseTimestamp(data['createdAt']),
      lastActivityAt: _parseTimestamp(data['lastActivityAt']),
      isPrivate: data['isPrivate'] ?? false,
      graduationYear: data['graduationYear'] as String?,
      department: data['department'] as String?,
    );
  }

  static DateTime? _parseTimestamp(dynamic value) {
    if (value == null) return null;
    if (value is Timestamp) return value.toDate();
    if (value is int) return DateTime.fromMillisecondsSinceEpoch(value);
    return null;
  }

  Map<String, dynamic> toMap() {
    return {
      'groupName': groupName,
      'description': description,
      'groupType': groupType,
      'imageUrl': imageUrl,
      'creatorId': creatorId,
      'creatorName': creatorName,
      'memberIds': memberIds,
      'adminIds': adminIds,
      'memberCount': memberCount,
      'createdAt':
          createdAt?.millisecondsSinceEpoch ??
          DateTime.now().millisecondsSinceEpoch,
      'lastActivityAt': DateTime.now().millisecondsSinceEpoch,
      'isPrivate': isPrivate,
      'graduationYear': graduationYear,
      'department': department,
    };
  }

  bool isMember(String userId) => memberIds.contains(userId);
  bool isAdmin(String userId) => adminIds.contains(userId);

  String get groupTypeIcon {
    switch (groupType?.toLowerCase()) {
      case 'class':
        return '🎓';
      case 'department':
        return '🏛️';
      case 'interest':
        return '⭐';
      case 'location':
        return '📍';
      default:
        return '👥';
    }
  }
}
