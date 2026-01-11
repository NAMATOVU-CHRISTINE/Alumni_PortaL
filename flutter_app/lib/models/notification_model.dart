import 'package:cloud_firestore/cloud_firestore.dart';

class NotificationModel {
  final String? id;
  final String? userId;
  final String? title;
  final String? message;
  final String? type;
  final String? referenceId;
  final DateTime? timestamp;
  final bool read;

  NotificationModel({
    this.id,
    this.userId,
    this.title,
    this.message,
    this.type,
    this.referenceId,
    this.timestamp,
    this.read = false,
  });

  factory NotificationModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>? ?? {};
    return NotificationModel(
      id: doc.id,
      userId: data['userId'] as String?,
      title: data['title'] as String?,
      message: data['message'] as String?,
      type: data['type'] as String?,
      referenceId: data['referenceId'] as String?,
      timestamp: _parseTimestamp(data['timestamp']),
      read: data['read'] ?? false,
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
      'userId': userId,
      'title': title,
      'message': message,
      'type': type,
      'referenceId': referenceId,
      'timestamp':
          timestamp?.millisecondsSinceEpoch ??
          DateTime.now().millisecondsSinceEpoch,
      'read': read,
    };
  }

  String get timeAgo {
    if (timestamp == null) return '';
    final diff = DateTime.now().difference(timestamp!);
    if (diff.inMinutes < 1) return 'Just now';
    if (diff.inMinutes < 60) return '${diff.inMinutes}m ago';
    if (diff.inHours < 24) return '${diff.inHours}h ago';
    if (diff.inDays < 7) return '${diff.inDays}d ago';
    return '${timestamp!.month}/${timestamp!.day}/${timestamp!.year}';
  }

  String get icon {
    switch (type) {
      case 'message':
        return '💬';
      case 'mentorship_request':
        return '🤝';
      case 'event':
        return '📅';
      case 'job':
        return '💼';
      case 'connection':
        return '👥';
      default:
        return '🔔';
    }
  }
}
