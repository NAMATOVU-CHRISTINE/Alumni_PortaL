import 'package:cloud_firestore/cloud_firestore.dart';

class ReferralModel {
  final String id;
  final String jobId;
  final String jobTitle;
  final String company;
  final String referrerId;
  final String referrerName;
  final String? referrerImageUrl;
  final String referredUserId;
  final String referredUserName;
  final String? referredUserEmail;
  final String status; // 'pending', 'accepted', 'applied', 'hired', 'declined'
  final String? note;
  final DateTime createdAt;
  final DateTime? updatedAt;

  ReferralModel({
    required this.id,
    required this.jobId,
    required this.jobTitle,
    required this.company,
    required this.referrerId,
    required this.referrerName,
    this.referrerImageUrl,
    required this.referredUserId,
    required this.referredUserName,
    this.referredUserEmail,
    this.status = 'pending',
    this.note,
    required this.createdAt,
    this.updatedAt,
  });

  factory ReferralModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return ReferralModel(
      id: doc.id,
      jobId: data['jobId'] ?? '',
      jobTitle: data['jobTitle'] ?? '',
      company: data['company'] ?? '',
      referrerId: data['referrerId'] ?? '',
      referrerName: data['referrerName'] ?? 'Unknown',
      referrerImageUrl: data['referrerImageUrl'],
      referredUserId: data['referredUserId'] ?? '',
      referredUserName: data['referredUserName'] ?? 'Unknown',
      referredUserEmail: data['referredUserEmail'],
      status: data['status'] ?? 'pending',
      note: data['note'],
      createdAt: data['createdAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['createdAt'])
          : DateTime.now(),
      updatedAt: data['updatedAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['updatedAt'])
          : null,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'jobId': jobId,
      'jobTitle': jobTitle,
      'company': company,
      'referrerId': referrerId,
      'referrerName': referrerName,
      'referrerImageUrl': referrerImageUrl,
      'referredUserId': referredUserId,
      'referredUserName': referredUserName,
      'referredUserEmail': referredUserEmail,
      'status': status,
      'note': note,
      'createdAt': createdAt.millisecondsSinceEpoch,
      'updatedAt': updatedAt?.millisecondsSinceEpoch,
    };
  }

  String get statusLabel {
    switch (status) {
      case 'pending':
        return 'Pending';
      case 'accepted':
        return 'Accepted';
      case 'applied':
        return 'Applied';
      case 'hired':
        return 'Hired! 🎉';
      case 'declined':
        return 'Declined';
      default:
        return status;
    }
  }
}
