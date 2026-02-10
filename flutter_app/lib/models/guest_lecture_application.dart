import 'package:cloud_firestore/cloud_firestore.dart';

enum ApplicationStatus {
  pending,
  underReview,
  approved,
  rejected,
  scheduled,
}

class GuestLectureApplication {
  final String id;
  final String applicantId;
  final String speakerName;
  final String speakerTitle;
  final String organization;
  final String email;
  final String phone;
  final String bio;
  final String lectureTitle;
  final String lectureAbstract;
  final int duration;
  final List<DateTime> preferredDates;
  final String targetAudience;
  final String faculty;
  final String department;
  final String cvUrl;
  final String? presentationOutlineUrl;
  final ApplicationStatus status;
  final DateTime submittedAt;
  final DateTime? reviewedAt;
  final String? reviewedBy;
  final String? reviewComments;
  final DateTime? scheduledDate;
  final String? venue;
  final int attendeeCount;
  final double? rating;

  GuestLectureApplication({
    required this.id,
    required this.applicantId,
    required this.speakerName,
    required this.speakerTitle,
    required this.organization,
    required this.email,
    required this.phone,
    required this.bio,
    required this.lectureTitle,
    required this.lectureAbstract,
    required this.duration,
    required this.preferredDates,
    required this.targetAudience,
    required this.faculty,
    required this.department,
    required this.cvUrl,
    this.presentationOutlineUrl,
    required this.status,
    required this.submittedAt,
    this.reviewedAt,
    this.reviewedBy,
    this.reviewComments,
    this.scheduledDate,
    this.venue,
    this.attendeeCount = 0,
    this.rating,
  });

  factory GuestLectureApplication.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return GuestLectureApplication(
      id: doc.id,
      applicantId: data['applicantId'] ?? '',
      speakerName: data['speakerName'] ?? '',
      speakerTitle: data['speakerTitle'] ?? '',
      organization: data['organization'] ?? '',
      email: data['email'] ?? '',
      phone: data['phone'] ?? '',
      bio: data['bio'] ?? '',
      lectureTitle: data['lectureTitle'] ?? '',
      lectureAbstract: data['lectureAbstract'] ?? '',
      duration: data['duration'] ?? 60,
      preferredDates: (data['preferredDates'] as List<dynamic>)
          .map((e) => (e as Timestamp).toDate())
          .toList(),
      targetAudience: data['targetAudience'] ?? '',
      faculty: data['faculty'] ?? '',
      department: data['department'] ?? '',
      cvUrl: data['cvUrl'] ?? '',
      presentationOutlineUrl: data['presentationOutlineUrl'],
      status: _statusFromString(data['status'] ?? 'pending'),
      submittedAt: (data['submittedAt'] as Timestamp).toDate(),
      reviewedAt: data['reviewedAt'] != null
          ? (data['reviewedAt'] as Timestamp).toDate()
          : null,
      reviewedBy: data['reviewedBy'],
      reviewComments: data['reviewComments'],
      scheduledDate: data['scheduledDate'] != null
          ? (data['scheduledDate'] as Timestamp).toDate()
          : null,
      venue: data['venue'],
      attendeeCount: data['attendeeCount'] ?? 0,
      rating: data['rating']?.toDouble(),
    );
  }

  Map<String, dynamic> toFirestore() {
    return {
      'applicantId': applicantId,
      'speakerName': speakerName,
      'speakerTitle': speakerTitle,
      'organization': organization,
      'email': email,
      'phone': phone,
      'bio': bio,
      'lectureTitle': lectureTitle,
      'lectureAbstract': lectureAbstract,
      'duration': duration,
      'preferredDates':
          preferredDates.map((e) => Timestamp.fromDate(e)).toList(),
      'targetAudience': targetAudience,
      'faculty': faculty,
      'department': department,
      'cvUrl': cvUrl,
      'presentationOutlineUrl': presentationOutlineUrl,
      'status': status.name,
      'submittedAt': Timestamp.fromDate(submittedAt),
      'reviewedAt': reviewedAt != null ? Timestamp.fromDate(reviewedAt!) : null,
      'reviewedBy': reviewedBy,
      'reviewComments': reviewComments,
      'scheduledDate':
          scheduledDate != null ? Timestamp.fromDate(scheduledDate!) : null,
      'venue': venue,
      'attendeeCount': attendeeCount,
      'rating': rating,
    };
  }

  static ApplicationStatus _statusFromString(String status) {
    switch (status) {
      case 'pending':
        return ApplicationStatus.pending;
      case 'underReview':
      case 'under_review':
        return ApplicationStatus.underReview;
      case 'approved':
        return ApplicationStatus.approved;
      case 'rejected':
        return ApplicationStatus.rejected;
      case 'scheduled':
        return ApplicationStatus.scheduled;
      default:
        return ApplicationStatus.pending;
    }
  }

  String get statusDisplay {
    switch (status) {
      case ApplicationStatus.pending:
        return 'Pending';
      case ApplicationStatus.underReview:
        return 'Under Review';
      case ApplicationStatus.approved:
        return 'Approved';
      case ApplicationStatus.rejected:
        return 'Rejected';
      case ApplicationStatus.scheduled:
        return 'Scheduled';
    }
  }
}
