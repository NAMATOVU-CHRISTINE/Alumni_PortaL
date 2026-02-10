import 'package:cloud_firestore/cloud_firestore.dart';

class ConvocationTeamMember {
  final String id;
  final String userId;
  final String fullName;
  final String position;
  final String role;
  final List<String> responsibilities;
  final String photoUrl;
  final String email;
  final String phone;
  final String bio;
  final String qualifications;
  final DateTime tenureStart;
  final DateTime? tenureEnd;
  final String committee;
  final bool isActive;
  final int displayOrder;
  final DateTime createdAt;
  final DateTime updatedAt;

  ConvocationTeamMember({
    required this.id,
    required this.userId,
    required this.fullName,
    required this.position,
    required this.role,
    required this.responsibilities,
    required this.photoUrl,
    required this.email,
    required this.phone,
    required this.bio,
    required this.qualifications,
    required this.tenureStart,
    this.tenureEnd,
    required this.committee,
    required this.isActive,
    required this.displayOrder,
    required this.createdAt,
    required this.updatedAt,
  });

  factory ConvocationTeamMember.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return ConvocationTeamMember(
      id: doc.id,
      userId: data['userId'] ?? '',
      fullName: data['fullName'] ?? '',
      position: data['position'] ?? '',
      role: data['role'] ?? '',
      responsibilities: List<String>.from(data['responsibilities'] ?? []),
      photoUrl: data['photoUrl'] ?? '',
      email: data['email'] ?? '',
      phone: data['phone'] ?? '',
      bio: data['bio'] ?? '',
      qualifications: data['qualifications'] ?? '',
      tenureStart: (data['tenureStart'] as Timestamp).toDate(),
      tenureEnd: data['tenureEnd'] != null
          ? (data['tenureEnd'] as Timestamp).toDate()
          : null,
      committee: data['committee'] ?? '',
      isActive: data['isActive'] ?? true,
      displayOrder: data['displayOrder'] ?? 0,
      createdAt: (data['createdAt'] as Timestamp).toDate(),
      updatedAt: (data['updatedAt'] as Timestamp).toDate(),
    );
  }

  Map<String, dynamic> toFirestore() {
    return {
      'userId': userId,
      'fullName': fullName,
      'position': position,
      'role': role,
      'responsibilities': responsibilities,
      'photoUrl': photoUrl,
      'email': email,
      'phone': phone,
      'bio': bio,
      'qualifications': qualifications,
      'tenureStart': Timestamp.fromDate(tenureStart),
      'tenureEnd': tenureEnd != null ? Timestamp.fromDate(tenureEnd!) : null,
      'committee': committee,
      'isActive': isActive,
      'displayOrder': displayOrder,
      'createdAt': Timestamp.fromDate(createdAt),
      'updatedAt': Timestamp.fromDate(updatedAt),
    };
  }
}
