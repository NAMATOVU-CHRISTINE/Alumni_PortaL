import 'package:cloud_firestore/cloud_firestore.dart';

enum NewsCategory {
  university('University News'),
  alumni('Alumni Stories'),
  research('Research'),
  academics('Academics'),
  sports('Sports'),
  technology('Technology'),
  health('Health'),
  general('General');

  final String displayName;
  const NewsCategory(this.displayName);
}

class NewsModel {
  final String? id;
  final String? title;
  final String? content;
  final String? summary;
  final String? imageUrl;
  final String? sourceUrl;
  final NewsCategory? category;
  final String? author;
  final DateTime? publishedAt;
  final DateTime? createdAt;
  final bool isFeatured;
  final int viewCount;
  final bool isExternal;

  NewsModel({
    this.id,
    this.title,
    this.content,
    this.summary,
    this.imageUrl,
    this.sourceUrl,
    this.category,
    this.author,
    this.publishedAt,
    this.createdAt,
    this.isFeatured = false,
    this.viewCount = 0,
    this.isExternal = false,
  });

  factory NewsModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>? ?? {};
    return NewsModel(
      id: doc.id,
      title: data['title'] as String?,
      content: data['content'] as String?,
      summary: data['summary'] as String?,
      imageUrl: data['imageUrl'] as String?,
      sourceUrl: data['sourceUrl'] as String?,
      category: _parseCategory(data['category']),
      author: data['author'] as String?,
      publishedAt: _parseTimestamp(data['publishedAt']),
      createdAt: _parseTimestamp(data['createdAt']),
      isFeatured: data['isFeatured'] ?? false,
      viewCount: data['viewCount'] ?? 0,
      isExternal: data['isExternal'] ?? false,
    );
  }

  static DateTime? _parseTimestamp(dynamic value) {
    if (value == null) return null;
    if (value is Timestamp) return value.toDate();
    if (value is int) return DateTime.fromMillisecondsSinceEpoch(value);
    return null;
  }

  static NewsCategory? _parseCategory(dynamic value) {
    if (value == null) return null;
    final str = value.toString().toLowerCase().replaceAll(' ', '');
    return NewsCategory.values.firstWhere(
      (e) =>
          e.name.toLowerCase() == str ||
          e.displayName.toLowerCase().replaceAll(' ', '') == str,
      orElse: () => NewsCategory.general,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'title': title,
      'content': content,
      'summary': summary,
      'imageUrl': imageUrl,
      'sourceUrl': sourceUrl,
      'category': category?.name,
      'author': author,
      'publishedAt':
          publishedAt?.millisecondsSinceEpoch ??
          DateTime.now().millisecondsSinceEpoch,
      'createdAt':
          createdAt?.millisecondsSinceEpoch ??
          DateTime.now().millisecondsSinceEpoch,
      'isFeatured': isFeatured,
      'viewCount': viewCount,
      'isExternal': isExternal,
    };
  }

  String get timeAgo {
    if (publishedAt == null) return '';
    final diff = DateTime.now().difference(publishedAt!);
    if (diff.inMinutes < 1) return 'Just now';
    if (diff.inMinutes < 60) return '${diff.inMinutes} min ago';
    if (diff.inHours < 24) {
      return '${diff.inHours} hour${diff.inHours > 1 ? 's' : ''} ago';
    }
    if (diff.inDays < 7) {
      return '${diff.inDays} day${diff.inDays > 1 ? 's' : ''} ago';
    }
    return formattedPublishDate;
  }

  String get formattedPublishDate {
    if (publishedAt == null) return '';
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
    return '${months[publishedAt!.month - 1]} ${publishedAt!.day}, ${publishedAt!.year}';
  }
}
