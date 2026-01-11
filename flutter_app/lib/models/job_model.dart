import 'package:cloud_firestore/cloud_firestore.dart';

class JobModel {
  final String? jobId;
  final String? title;
  final String? company;
  final String? description;
  final String? requirements;
  final String? location;
  final String? salary;
  final String? jobType;
  final String? experienceLevel;
  final String? postedBy;
  final String? postedByName;
  final DateTime? postedAt;
  final DateTime? expiresAt;
  final bool isActive;
  final bool isRemote;
  final String? applicationUrl;
  final String? contactEmail;
  final List<String> tags;
  final int viewCount;
  final int applicationCount;

  JobModel({
    this.jobId,
    this.title,
    this.company,
    this.description,
    this.requirements,
    this.location,
    this.salary,
    this.jobType,
    this.experienceLevel,
    this.postedBy,
    this.postedByName,
    this.postedAt,
    this.expiresAt,
    this.isActive = true,
    this.isRemote = false,
    this.applicationUrl,
    this.contactEmail,
    this.tags = const [],
    this.viewCount = 0,
    this.applicationCount = 0,
  });

  factory JobModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>? ?? {};
    return JobModel(
      jobId: doc.id,
      title: data['title'] as String?,
      company: data['company'] as String?,
      description: data['description'] as String?,
      requirements: data['requirements'] as String?,
      location: data['location'] as String?,
      salary: data['salary'] as String?,
      jobType: data['jobType'] as String?,
      experienceLevel: data['experienceLevel'] as String?,
      postedBy: data['postedBy'] as String?,
      postedByName: data['postedByName'] as String?,
      postedAt: _parseTimestamp(data['postedAt']),
      expiresAt: _parseTimestamp(data['expiresAt']),
      isActive: data['isActive'] ?? true,
      isRemote: data['isRemote'] ?? false,
      applicationUrl: data['applicationUrl'] as String?,
      contactEmail: data['contactEmail'] as String?,
      tags: List<String>.from(data['tags'] ?? []),
      viewCount: data['viewCount'] ?? 0,
      applicationCount: data['applicationCount'] ?? 0,
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
      'title': title,
      'company': company,
      'description': description,
      'requirements': requirements,
      'location': location,
      'salary': salary,
      'jobType': jobType,
      'experienceLevel': experienceLevel,
      'postedBy': postedBy,
      'postedByName': postedByName,
      'postedAt':
          postedAt?.millisecondsSinceEpoch ??
          DateTime.now().millisecondsSinceEpoch,
      'expiresAt':
          expiresAt?.millisecondsSinceEpoch ??
          DateTime.now().add(const Duration(days: 90)).millisecondsSinceEpoch,
      'isActive': isActive,
      'isRemote': isRemote,
      'applicationUrl': applicationUrl,
      'contactEmail': contactEmail,
      'tags': tags,
      'viewCount': viewCount,
      'applicationCount': applicationCount,
    };
  }

  bool get isExpired => expiresAt != null && DateTime.now().isAfter(expiresAt!);

  bool get isValidForDisplay => isActive && !isExpired;

  String get formattedLocation {
    if (isRemote) {
      return location != null ? '$location (Remote)' : 'Remote';
    }
    return location ?? 'Location not specified';
  }

  String get formattedSalary =>
      salary?.isNotEmpty == true ? salary! : 'Salary not specified';

  String get timeAgo {
    if (postedAt == null) return '';
    final diff = DateTime.now().difference(postedAt!);
    if (diff.inDays < 1) {
      return diff.inHours < 1 ? 'Just now' : '${diff.inHours}h ago';
    } else if (diff.inDays < 7) {
      return '${diff.inDays}d ago';
    } else if (diff.inDays < 30) {
      return '${diff.inDays ~/ 7}w ago';
    } else {
      return '${diff.inDays ~/ 30}m ago';
    }
  }

  String get daysUntilExpiry {
    if (expiresAt == null) return '';
    final diff = expiresAt!.difference(DateTime.now());
    if (diff.inDays <= 0) return 'Expired';
    if (diff.inDays == 1) return '1 day left';
    return '${diff.inDays} days left';
  }
}
