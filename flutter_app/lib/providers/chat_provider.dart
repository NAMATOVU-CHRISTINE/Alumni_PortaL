import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/models/chat_model.dart';
import 'package:alumni_portal/services/notification_service.dart';
import 'package:alumni_portal/services/firebase_messaging_service.dart';

class ChatProvider with ChangeNotifier {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;

  List<ChatModel> _chats = [];
  List<ChatMessageModel> _messages = [];
  bool _isLoading = false;
  String? _error;
  StreamSubscription? _chatsSubscription;
  StreamSubscription? _messagesSubscription;

  List<ChatModel> get chats => _chats;
  List<ChatMessageModel> get messages => _messages;
  bool get isLoading => _isLoading;
  String? get error => _error;
  String? get currentUserId => _auth.currentUser?.uid;

  int get totalUnreadCount {
    final userId = currentUserId;
    if (userId == null) return 0;
    return _chats.fold(0, (sum, chat) => sum + chat.getUnreadCount(userId));
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }

  void _setError(String? value) {
    _error = value;
    notifyListeners();
  }

  void listenToChats() {
    final userId = currentUserId;
    if (userId == null) return;

    _chatsSubscription?.cancel();
    _chatsSubscription = _firestore
        .collection('chats')
        .where('participantIds', arrayContains: userId)
        .where('isActive', isEqualTo: true)
        .orderBy('lastMessageTimestamp', descending: true)
        .snapshots()
        .listen(
      (snapshot) {
        // Check for new messages and show notifications
        for (var change in snapshot.docChanges) {
          if (change.type == DocumentChangeType.modified) {
            final chat = ChatModel.fromFirestore(change.doc);
            // Only notify if the last message is not from current user
            if (chat.lastMessageSenderId != userId && 
                chat.lastMessageSenderId != null) {
              _showMessageNotification(chat);
            }
          }
        }
        
        _chats =
            snapshot.docs.map((doc) => ChatModel.fromFirestore(doc)).toList();
        notifyListeners();
      },
      onError: (e) {
        _setError('Failed to load chats');
      },
    );
  }

  Future<void> _showMessageNotification(ChatModel chat) async {
    try {
      final userId = currentUserId;
      if (userId == null) return;

      // Get sender name
      final senderId = chat.lastMessageSenderId;
      final senderName = chat.participantNames[senderId] ?? 'Someone';
      
      // Prepare message preview
      String messagePreview = chat.lastMessageText ?? '';
      if (chat.lastMessageType == 'image') {
        messagePreview = '📷 Photo';
      } else if (chat.lastMessageType == 'file') {
        messagePreview = '📎 File';
      }

      // Import and use FirebaseMessagingService to show notification
      await FirebaseMessagingService.showNotification(
        title: senderName,
        body: messagePreview,
        notificationType: 'message',
        payload: chat.chatId,
      );
    } catch (e) {
      print('Error showing notification: $e');
    }
  }

  void listenToMessages(String chatId) {
    _messagesSubscription?.cancel();
    _messagesSubscription = _firestore
        .collection('chats')
        .doc(chatId)
        .collection('messages')
        .orderBy('timestamp', descending: false)
        .snapshots()
        .listen(
      (snapshot) {
        _messages = snapshot.docs
            .map((doc) => ChatMessageModel.fromFirestore(doc))
            .toList();
        notifyListeners();
      },
      onError: (e) {
        _setError('Failed to load messages');
      },
    );
  }

  Future<String?> createOrGetChat(
    String otherUserId,
    String otherUserName,
  ) async {
    try {
      _setLoading(true);
      final userId = currentUserId;
      if (userId == null) return null;

      // Generate consistent chat ID
      final chatId = ChatModel.generateChatId(userId, otherUserId);

      // Check if chat exists
      final chatDoc = await _firestore.collection('chats').doc(chatId).get();

      if (chatDoc.exists) {
        return chatId;
      }

      // Get current user's name
      final currentUserDoc =
          await _firestore.collection('users').doc(userId).get();
      final currentUserName = currentUserDoc.data()?['fullName'] ?? 'User';
      final currentUserImage = currentUserDoc.data()?['profileImageUrl'];

      // Get other user's image
      final otherUserDoc =
          await _firestore.collection('users').doc(otherUserId).get();
      final otherUserImage = otherUserDoc.data()?['profileImageUrl'];

      // Create new chat
      final chat = ChatModel(
        chatId: chatId,
        participantIds: [userId, otherUserId],
        participantNames: {userId: currentUserName, otherUserId: otherUserName},
        participantImages: {
          if (currentUserImage != null) userId: currentUserImage,
          if (otherUserImage != null) otherUserId: otherUserImage,
        },
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
        unreadCounts: {userId: 0, otherUserId: 0},
      );

      await _firestore.collection('chats').doc(chatId).set(chat.toMap());

      return chatId;
    } catch (e) {
      _setError('Failed to create chat');
      return null;
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> sendMessage({
    required String chatId,
    required String receiverId,
    required String messageText,
    String messageType = 'text',
    String? fileUrl,
    String? fileName,
    String? imageUrl,
  }) async {
    try {
      final userId = currentUserId;
      if (userId == null) return false;

      // Get sender info
      final userDoc = await _firestore.collection('users').doc(userId).get();
      final senderName = userDoc.data()?['fullName'] ?? 'User';
      final senderImage = userDoc.data()?['profileImageUrl'];

      final message = ChatMessageModel(
        chatId: chatId,
        senderId: userId,
        senderName: senderName,
        senderProfileImage: senderImage,
        receiverId: receiverId,
        messageText: messageText,
        messageType: messageType,
        fileUrl: fileUrl,
        fileName: fileName,
        imageUrl: imageUrl,
        timestamp: DateTime.now(),
        isRead: false, // Explicitly set to false
        isDelivered: true, // Set to true when sent
      );

      // Add message to subcollection
      await _firestore
          .collection('chats')
          .doc(chatId)
          .collection('messages')
          .add(message.toMap());

      // Update chat with last message info
      await _firestore.collection('chats').doc(chatId).update({
        'lastMessageText': messageText,
        'lastMessageSenderId': userId,
        'lastMessageType': messageType,
        'lastMessageTimestamp': DateTime.now().millisecondsSinceEpoch,
        'updatedAt': DateTime.now().millisecondsSinceEpoch,
        'unreadCounts.$receiverId': FieldValue.increment(1),
      });

      // Send notification
      NotificationService.notifyNewMessage(
        recipientId: receiverId,
        senderName: senderName,
        senderId: userId,
        messagePreview: messageText,
      );

      return true;
    } catch (e) {
      _setError('Failed to send message');
      return false;
    }
  }

  Future<void> markChatAsRead(String chatId) async {
    try {
      final userId = currentUserId;
      if (userId == null) return;

      print('Marking chat as read for user: $userId');

      // Update chat document
      await _firestore.collection('chats').doc(chatId).update({
        'unreadCounts.$userId': 0,
        'lastSeenTimestamps.$userId': DateTime.now().millisecondsSinceEpoch,
      });

      // Mark all messages from other user as read (where current user is receiver)
      final messagesSnapshot = await _firestore
          .collection('chats')
          .doc(chatId)
          .collection('messages')
          .where('receiverId', isEqualTo: userId)
          .where('isRead', isEqualTo: false)
          .get();

      print('Found ${messagesSnapshot.docs.length} unread messages to mark as read');

      // Batch update all unread messages
      final batch = _firestore.batch();
      for (var doc in messagesSnapshot.docs) {
        print('Marking message ${doc.id} as read');
        batch.update(doc.reference, {'isRead': true});
      }
      await batch.commit();
      
      print('Successfully marked messages as read');
    } catch (e) {
      print('Error marking chat as read: $e');
    }
  }

  Future<void> deleteChat(String chatId) async {
    try {
      await _firestore.collection('chats').doc(chatId).update({
        'isActive': false,
      });
    } catch (e) {
      _setError('Failed to delete chat');
    }
  }

  void stopListening() {
    _chatsSubscription?.cancel();
    _messagesSubscription?.cancel();
  }

  void clearMessages() {
    _messages = [];
    notifyListeners();
  }

  void clearError() => _setError(null);

  @override
  void dispose() {
    stopListening();
    super.dispose();
  }
}
