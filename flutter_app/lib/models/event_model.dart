import 'package:cloud_firestore/cloud_firestore.dart';

enum EventCategory {
  mentorship('Mentorship', '🤝'),
  leadership('Leadership', '👑'),
  networking('Networking', '🌐'),
  university('University', '🏛️'),
  career('Career', '💼'),
  technology('Technology', '💻'),
  social('Social', '🎉');

  final String displayName;
  final String icon;
  const EventCategory(this.displayName, this.icon);
}

enum EventStatus { upcoming, ongoing, completed, cancelled }

class EventModel {
  final String? id;
  final String? title;
  final String? description;
  final String? summary;
  final DateTime? dateTime;
  final String? location;
  final EventCategory? category;
  final EventStatus status;
  final String? imageUrl;
  final String? registrationUrl;
  final String? organizerName;
  final int maxParticipants;
  final int currentParticipants;
  final bool isOnline;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  EventModel({
    this.id,
    this.title,
    this.description,
    this.summary,
    this.dateTime,
    this.location,
    this.category,
    this.status = EventStatus.upcoming,
    this.imageUrl,
    this.registrationUrl,
    this.organizerName,
    this.maxParticipants = 50,
    this.currentParticipants = 0,
    this.isOnline = false,
    this.createdAt,
    this.updatedAt,
  });

  factory EventModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>? ?? {};
    return EventModel(
      id: doc.id,
      title: data['title'] as String?,
      description: data['description'] as String?,
      summary: data['summary'] as String?,
      dateTime: _parseTimestamp(data['dateTime']),
      location: data['location'] as String?,
      category: _parseCategory(data['category']),
      status: _parseStatus(data['status']),
      imageUrl: data['imageUrl'] as String?,
      registrationUrl: data['registrationUrl'] as String?,
      organizerName: data['organizerName'] as String?,
      maxParticipants: data['maxParticipants'] ?? 50,
      currentParticipants: data['currentParticipants'] ?? 0,
      isOnline: data['isOnline'] ?? false,
      createdAt: _parseTimestamp(data['createdAt']),
      updatedAt: _parseTimestamp(data['updatedAt']),
    );
  }

  static DateTime? _parseTimestamp(dynamic value) {
    if (value == null) return null;
    if (value is Timestamp) return value.toDate();
    if (value is int) return DateTime.fromMillisecondsSinceEpoch(value);
    return null;
  }

  static EventCategory? _parseCategory(dynamic value) {
    if (value == null) return null;
    final str = value.toString().toLowerCase();
    return EventCategory.values.firstWhere(
      (e) => e.name.toLowerCase() == str,
      orElse: () => EventCategory.university,
    );
  }

  static EventStatus _parseStatus(dynamic value) {
    if (value == null) return EventStatus.upcoming;
    final str = value.toString().toLowerCase();
    return EventStatus.values.firstWhere(
      (e) => e.name.toLowerCase() == str,
      orElse: () => EventStatus.upcoming,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'title': title,
      'description': description,
      'summary': summary,
      'dateTime': dateTime?.millisecondsSinceEpoch,
      'location': location,
      'category': category?.name,
      'status': status.name,
      'imageUrl': imageUrl,
      'registrationUrl': registrationUrl,
      'organizerName': organizerName,
      'maxParticipants': maxParticipants,
      'currentParticipants': currentParticipants,
      'isOnline': isOnline,
      'createdAt':
          createdAt?.millisecondsSinceEpoch ??
          DateTime.now().millisecondsSinceEpoch,
      'updatedAt': DateTime.now().millisecondsSinceEpoch,
    };
  }

  bool get isAvailableForRegistration =>
      status == EventStatus.upcoming && currentParticipants < maxParticipants;

  int get availableSpots =>
      (maxParticipants - currentParticipants).clamp(0, maxParticipants);

  bool get isExpired => dateTime != null && DateTime.now().isAfter(dateTime!);

  String get formattedDateTime {
    if (dateTime == null) return '';
    return '${_monthName(dateTime!.month)} ${dateTime!.day}, ${dateTime!.year} at ${_formatTime(dateTime!)}';
  }

  String _monthName(int month) {
    const months = [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec',
    ];
    return months[month - 1];
  }

  String _formatTime(DateTime dt) {
    final hour = dt.hour > 12 ? dt.hour - 12 : (dt.hour == 0 ? 12 : dt.hour);
    final period = dt.hour >= 12 ? 'PM' : 'AM';
    return '$hour:${dt.minute.toString().padLeft(2, '0')} $period';
  }
}
