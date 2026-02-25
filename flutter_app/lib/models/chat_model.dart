import 'package:cloud_firestore/cloud_firestore.dart';

class ChatModel {
  final String? chatId;
  final List<String> participantIds;
  final Map<String, String> participantNames;
  final Map<String, String> participantImages;
  final Map<String, DateTime> participantLastSeen;
  final String? lastMessageText;
  final String? lastMessageSenderId;
  final String? lastMessageType;
  final DateTime? lastMessageTimestamp;
  final DateTime? createdAt;
  final DateTime? updatedAt;
  final Map<String, int> unreadCounts;
  final bool isActive;
  final String chatType;
  final String? chatName;
  final String? chatImage;

  ChatModel({
    this.chatId,
    this.participantIds = const [],
    this.participantNames = const {},
    this.participantImages = const {},
    this.participantLastSeen = const {},
    this.lastMessageText,
    this.lastMessageSenderId,
    this.lastMessageType,
    this.lastMessageTimestamp,
    this.createdAt,
    this.updatedAt,
    this.unreadCounts = const {},
    this.isActive = true,
    this.chatType = 'direct',
    this.chatName,
    this.chatImage,
  });

  factory ChatModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>? ?? {};

    // Parse participantLastSeen map
    final lastSeenData =
        data['participantLastSeen'] as Map<String, dynamic>? ?? {};
    final participantLastSeen = <String, DateTime>{};
    lastSeenData.forEach((key, value) {
      final parsedTime = _parseTimestamp(value);
      if (parsedTime != null) {
        participantLastSeen[key] = parsedTime;
      }
    });

    return ChatModel(
      chatId: doc.id,
      participantIds: List<String>.from(data['participantIds'] ?? []),
      participantNames: Map<String, String>.from(
        data['participantNames'] ?? {},
      ),
      participantImages: Map<String, String>.from(
        data['participantImages'] ?? {},
      ),
      participantLastSeen: participantLastSeen,
      lastMessageText: data['lastMessageText'] as String?,
      lastMessageSenderId: data['lastMessageSenderId'] as String?,
      lastMessageType: data['lastMessageType'] as String?,
      lastMessageTimestamp: _parseTimestamp(data['lastMessageTimestamp']),
      createdAt: _parseTimestamp(data['createdAt']),
      updatedAt: _parseTimestamp(data['updatedAt']),
      unreadCounts: Map<String, int>.from(data['unreadCounts'] ?? {}),
      isActive: data['isActive'] ?? true,
      chatType: data['chatType'] ?? 'direct',
      chatName: data['chatName'] as String?,
      chatImage: data['chatImage'] as String?,
    );
  }

  static DateTime? _parseTimestamp(dynamic value) {
    if (value == null) return null;
    if (value is Timestamp) return value.toDate();
    if (value is int) return DateTime.fromMillisecondsSinceEpoch(value);
    return null;
  }

  Map<String, dynamic> toMap() {
    // Convert participantLastSeen to map with timestamps
    final lastSeenMap = <String, int>{};
    participantLastSeen.forEach((key, value) {
      lastSeenMap[key] = value.millisecondsSinceEpoch;
    });

    return {
      'participantIds': participantIds,
      'participantNames': participantNames,
      'participantImages': participantImages,
      'participantLastSeen': lastSeenMap,
      'lastMessageText': lastMessageText,
      'lastMessageSenderId': lastMessageSenderId,
      'lastMessageType': lastMessageType,
      'lastMessageTimestamp': lastMessageTimestamp?.millisecondsSinceEpoch,
      'createdAt': createdAt?.millisecondsSinceEpoch ??
          DateTime.now().millisecondsSinceEpoch,
      'updatedAt': DateTime.now().millisecondsSinceEpoch,
      'unreadCounts': unreadCounts,
      'isActive': isActive,
      'chatType': chatType,
      'chatName': chatName,
      'chatImage': chatImage,
    };
  }

  bool get isDirectChat => chatType == 'direct';
  bool get isGroupChat => chatType == 'group';

  String? getOtherParticipantId(String currentUserId) {
    if (!isDirectChat || participantIds.length != 2) return null;
    return participantIds.firstWhere(
      (id) => id != currentUserId,
      orElse: () => '',
    );
  }

  String getOtherParticipantName(String currentUserId) {
    final otherId = getOtherParticipantId(currentUserId);
    return otherId != null
        ? (participantNames[otherId] ?? 'Unknown User')
        : 'Unknown User';
  }

  String? getOtherParticipantImage(String currentUserId) {
    final otherId = getOtherParticipantId(currentUserId);
    return otherId != null ? participantImages[otherId] : null;
  }

  String? getOtherParticipantLastSeen(String currentUserId) {
    final otherId = getOtherParticipantId(currentUserId);
    if (otherId == null) return null;
    final lastSeenTime = participantLastSeen[otherId];
    return lastSeenTime?.toIso8601String();
  }

  String getDisplayName(String currentUserId) {
    if (isDirectChat) return getOtherParticipantName(currentUserId);
    return chatName?.isNotEmpty == true ? chatName! : 'Group Chat';
  }

  int getUnreadCount(String userId) => unreadCounts[userId] ?? 0;

  bool isLastMessageRead(String currentUserId) {
    // If current user sent the last message, check if other user has read it
    if (lastMessageSenderId == currentUserId) {
      final otherId = getOtherParticipantId(currentUserId);
      if (otherId == null) return false;
      // If other user has 0 unread messages, they've read it
      return getUnreadCount(otherId) == 0;
    }
    return false;
  }

  String get lastMessageDisplayText {
    if (lastMessageText == null || lastMessageText!.isEmpty) {
      return 'No messages yet';
    }
    switch (lastMessageType) {
      case 'image':
        return '📷 Photo';
      case 'file':
        return '📎 File';
      case 'location':
        return '📍 Location';
      default:
        return lastMessageText!.length > 50
            ? '${lastMessageText!.substring(0, 47)}...'
            : lastMessageText!;
    }
  }

  String get lastMessageTimeAgo {
    if (lastMessageTimestamp == null) return '';
    final diff = DateTime.now().difference(lastMessageTimestamp!);
    if (diff.inMinutes < 1) return 'Now';
    if (diff.inMinutes < 60) return '${diff.inMinutes}m';
    if (diff.inHours < 24) return '${diff.inHours}h';
    if (diff.inDays < 7) return '${diff.inDays}d';
    return '${lastMessageTimestamp!.month}/${lastMessageTimestamp!.day}';
  }

  static String generateChatId(String user1Id, String user2Id) {
    final ids = [user1Id, user2Id]..sort();
    return '${ids[0]}_${ids[1]}';
  }
}

class ChatMessageModel {
  final String? messageId;
  final String? chatId;
  final String? senderId;
  final String? senderName;
  final String? senderProfileImage;
  final String? receiverId;
  final String? messageText;
  final String messageType;
  final String? fileUrl;
  final String? fileName;
  final String? imageUrl;
  final DateTime? timestamp;
  final bool isRead;
  final bool isDelivered;
  final bool isEdited;
  final bool isDeleted;
  final String? replyToMessageId;
  final String? replyToText;

  ChatMessageModel({
    this.messageId,
    this.chatId,
    this.senderId,
    this.senderName,
    this.senderProfileImage,
    this.receiverId,
    this.messageText,
    this.messageType = 'text',
    this.fileUrl,
    this.fileName,
    this.imageUrl,
    this.timestamp,
    this.isRead = false,
    this.isDelivered = false,
    this.isEdited = false,
    this.isDeleted = false,
    this.replyToMessageId,
    this.replyToText,
  });

  factory ChatMessageModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>? ?? {};
    return ChatMessageModel(
      messageId: doc.id,
      chatId: data['chatId'] as String?,
      senderId: data['senderId'] as String?,
      senderName: data['senderName'] as String?,
      senderProfileImage: data['senderProfileImage'] as String?,
      receiverId: data['receiverId'] as String?,
      messageText: data['messageText'] as String?,
      messageType: data['messageType'] ?? 'text',
      fileUrl: data['fileUrl'] as String?,
      fileName: data['fileName'] as String?,
      imageUrl: data['imageUrl'] as String?,
      timestamp: _parseTimestamp(data['timestamp']),
      isRead: data['isRead'] ?? false,
      isDelivered: data['isDelivered'] ?? false,
      isEdited: data['isEdited'] ?? false,
      isDeleted: data['isDeleted'] ?? false,
      replyToMessageId: data['replyToMessageId'] as String?,
      replyToText: data['replyToText'] as String?,
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
      'chatId': chatId,
      'senderId': senderId,
      'senderName': senderName,
      'senderProfileImage': senderProfileImage,
      'receiverId': receiverId,
      'messageText': messageText,
      'messageType': messageType,
      'fileUrl': fileUrl,
      'fileName': fileName,
      'imageUrl': imageUrl,
      'timestamp': timestamp?.millisecondsSinceEpoch ??
          DateTime.now().millisecondsSinceEpoch,
      'isRead': isRead,
      'isDelivered': isDelivered,
      'isEdited': isEdited,
      'isDeleted': isDeleted,
      'replyToMessageId': replyToMessageId,
      'replyToText': replyToText,
    };
  }

  bool get isTextMessage => messageType == 'text';
  bool get isImageMessage => messageType == 'image';
  bool get isFileMessage => messageType == 'file';

  String get displayText {
    if (isDeleted) return 'Message deleted';
    switch (messageType) {
      case 'image':
        return '📷 Photo';
      case 'file':
        return '📎 ${fileName ?? "File"}';
      case 'voice':
        return '🎤 Voice message';
      default:
        return messageText ?? '';
    }
  }

  String get formattedTime {
    if (timestamp == null) return '';
    final hour = timestamp!.hour > 12
        ? timestamp!.hour - 12
        : (timestamp!.hour == 0 ? 12 : timestamp!.hour);
    final period = timestamp!.hour >= 12 ? 'PM' : 'AM';
    return '$hour:${timestamp!.minute.toString().padLeft(2, '0')} $period';
  }
}
