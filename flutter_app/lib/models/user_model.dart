import 'package:cloud_firestore/cloud_firestore.dart';

class UserModel {
  final String? userId;
  final String? fullName;
  final String? username;
  final String? email;
  final String? personalEmail;
  final String? studentId;
  final String? alumniId;
  final String? staffId;
  final String? profileImageUrl;
  final String? bio;
  final String? major;
  final String? graduationYear;
  final String? currentJob;
  final String? company;
  final String? workStatus;
  final String? location;
  final String? phoneNumber;
  final List<String> skills;
  final bool isVerified;
  final bool isAlumni;
  final String userType;
  final String role;
  final DateTime? createdAt;
  final DateTime? lastActive;
  final DateTime? updatedAt;
  final bool emailVerified;
  final Map<String, dynamic> socialLinks;
  final Map<String, dynamic> privacySettings;
  final String? fcmToken;
  final String? industry;

  UserModel({
    this.userId,
    this.fullName,
    this.username,
    this.email,
    this.personalEmail,
    this.studentId,
    this.alumniId,
    this.staffId,
    this.profileImageUrl,
    this.bio,
    this.major,
    this.graduationYear,
    this.currentJob,
    this.company,
    this.workStatus,
    this.location,
    this.phoneNumber,
    this.skills = const [],
    this.isVerified = false,
    this.isAlumni = false,
    this.userType = 'student',
    this.role = 'user',
    this.createdAt,
    this.lastActive,
    this.updatedAt,
    this.emailVerified = false,
    this.socialLinks = const {},
    this.privacySettings = const {},
    this.fcmToken,
    this.industry,
  });

  factory UserModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>? ?? {};
    return UserModel(
      userId: doc.id,
      fullName: data['fullName'] as String?,
      username: data['username'] as String?,
      email: data['email'] as String?,
      personalEmail: data['personalEmail'] as String?,
      studentId: data['studentId'] as String?,
      alumniId: data['alumniID'] as String?,
      staffId: data['staffID'] as String?,
      profileImageUrl: data['profileImageUrl'] as String?,
      bio: data['bio'] as String?,
      major: data['major'] as String?,
      graduationYear: data['graduationYear'] as String?,
      currentJob: data['currentJob'] as String?,
      company: data['company'] as String?,
      workStatus: data['workStatus'] as String?,
      location: data['location'] as String?,
      phoneNumber: data['phoneNumber'] as String?,
      skills: List<String>.from(data['skills'] ?? []),
      isVerified: data['isVerified'] ?? false,
      isAlumni: data['isAlumni'] ?? data['alumni'] ?? false,
      userType: data['userType'] ?? 'student',
      role: data['role'] ?? 'user',
      createdAt: _parseTimestamp(data['createdAt']),
      lastActive: _parseTimestamp(data['lastActive']),
      updatedAt: _parseTimestamp(data['updatedAt']),
      emailVerified: data['emailVerified'] ?? false,
      socialLinks: Map<String, dynamic>.from(data['socialLinks'] ?? {}),
      privacySettings: Map<String, dynamic>.from(data['privacySettings'] ?? {}),
      fcmToken: data['fcmToken'] as String?,
      industry: data['industry'] as String?,
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
      'fullName': fullName,
      'username': username,
      'email': email,
      'personalEmail': personalEmail,
      'studentId': studentId,
      'alumniID': alumniId,
      'staffID': staffId,
      'profileImageUrl': profileImageUrl,
      'bio': bio,
      'major': major,
      'graduationYear': graduationYear,
      'currentJob': currentJob,
      'company': company,
      'workStatus': workStatus,
      'location': location,
      'phoneNumber': phoneNumber,
      'skills': skills,
      'isVerified': isVerified,
      'isAlumni': isAlumni,
      'userType': userType,
      'role': role,
      'createdAt':
          createdAt?.millisecondsSinceEpoch ??
          DateTime.now().millisecondsSinceEpoch,
      'lastActive': DateTime.now().millisecondsSinceEpoch,
      'updatedAt': DateTime.now().millisecondsSinceEpoch,
      'emailVerified': emailVerified,
      'socialLinks': socialLinks,
      'privacySettings': privacySettings,
      'fcmToken': fcmToken,
      'industry': industry,
    };
  }

  UserModel copyWith({
    String? userId,
    String? fullName,
    String? username,
    String? email,
    String? personalEmail,
    String? studentId,
    String? alumniId,
    String? staffId,
    String? profileImageUrl,
    String? bio,
    String? major,
    String? graduationYear,
    String? currentJob,
    String? company,
    String? workStatus,
    String? location,
    String? phoneNumber,
    List<String>? skills,
    bool? isVerified,
    bool? isAlumni,
    String? userType,
    String? role,
    DateTime? createdAt,
    DateTime? lastActive,
    DateTime? updatedAt,
    bool? emailVerified,
    Map<String, dynamic>? socialLinks,
    Map<String, dynamic>? privacySettings,
    String? fcmToken,
    String? industry,
  }) {
    return UserModel(
      userId: userId ?? this.userId,
      fullName: fullName ?? this.fullName,
      username: username ?? this.username,
      email: email ?? this.email,
      personalEmail: personalEmail ?? this.personalEmail,
      studentId: studentId ?? this.studentId,
      alumniId: alumniId ?? this.alumniId,
      staffId: staffId ?? this.staffId,
      profileImageUrl: profileImageUrl ?? this.profileImageUrl,
      bio: bio ?? this.bio,
      major: major ?? this.major,
      graduationYear: graduationYear ?? this.graduationYear,
      currentJob: currentJob ?? this.currentJob,
      company: company ?? this.company,
      workStatus: workStatus ?? this.workStatus,
      location: location ?? this.location,
      phoneNumber: phoneNumber ?? this.phoneNumber,
      skills: skills ?? this.skills,
      isVerified: isVerified ?? this.isVerified,
      isAlumni: isAlumni ?? this.isAlumni,
      userType: userType ?? this.userType,
      role: role ?? this.role,
      createdAt: createdAt ?? this.createdAt,
      lastActive: lastActive ?? this.lastActive,
      updatedAt: updatedAt ?? this.updatedAt,
      emailVerified: emailVerified ?? this.emailVerified,
      socialLinks: socialLinks ?? this.socialLinks,
      privacySettings: privacySettings ?? this.privacySettings,
      fcmToken: fcmToken ?? this.fcmToken,
      industry: industry ?? this.industry,
    );
  }

  String get skillsAsString => skills.join(', ');

  String get displayName => fullName ?? username ?? 'Alumni User';

  String get idLabel {
    switch (userType.toLowerCase()) {
      case 'alumni':
        return 'Alumni ID';
      case 'staff':
        return 'Staff ID';
      default:
        return 'Student ID';
    }
  }

  String? get displayId {
    switch (userType.toLowerCase()) {
      case 'alumni':
        return alumniId;
      case 'staff':
        return staffId;
      default:
        return studentId;
    }
  }
}
