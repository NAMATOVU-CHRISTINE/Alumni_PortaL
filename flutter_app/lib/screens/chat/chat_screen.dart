import 'dart:io';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:image_picker/image_picker.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:alumni_portal/providers/chat_provider.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/models/chat_model.dart';
import 'package:alumni_portal/models/user_model.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/utils/time_formatter.dart';
import 'package:alumni_portal/services/image_compression_service.dart';
import 'package:alumni_portal/services/cloudinary_service.dart';

class ChatScreen extends StatefulWidget {
  final String chatId;
  final String otherUserName;
  final String? otherUserImage;
  final String? lastSeen;

  const ChatScreen({
    super.key,
    required this.chatId,
    required this.otherUserName,
    this.otherUserImage,
    this.lastSeen,
  });

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final _messageController = TextEditingController();
  final _scrollController = ScrollController();
  bool _isRecording = false;
  bool _hasText = false;
  UserModel? _otherUser;
  bool _isLoadingImage = false;

  @override
  void initState() {
    super.initState();
    final chatProvider = context.read<ChatProvider>();
    chatProvider.listenToMessages(widget.chatId);
    chatProvider.markChatAsRead(widget.chatId);

    _messageController.addListener(() {
      setState(() {
        _hasText = _messageController.text.trim().isNotEmpty;
      });
    });

    _loadOtherUser();
  }

  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    context.read<ChatProvider>().clearMessages();
    super.dispose();
  }

  Future<void> _loadOtherUser() async {
    final chatProvider = context.read<ChatProvider>();
    final userProvider = context.read<UserProvider>();
    
    final chat = chatProvider.chats.firstWhere(
      (c) => c.chatId == widget.chatId,
      orElse: () => ChatModel(),
    );
    
    final otherUserId = chat.getOtherParticipantId(
      chatProvider.currentUserId ?? '',
    );
    
    if (otherUserId != null) {
      final user = await userProvider.getUserById(otherUserId);
      if (mounted) {
        setState(() {
          _otherUser = user;
        });
      }
    }
  }

  Future<void> _makePhoneCall() async {
    if (_otherUser?.phoneNumber == null || _otherUser!.phoneNumber!.isEmpty) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Phone number not available'),
            backgroundColor: Colors.orange,
          ),
        );
      }
      return;
    }

    final phoneNumber = _otherUser!.phoneNumber!;
    final uri = Uri.parse('tel:$phoneNumber');
    
    try {
      if (await canLaunchUrl(uri)) {
        await launchUrl(uri);
      } else {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Cannot make phone calls on this device'),
              backgroundColor: Colors.red,
            ),
          );
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  void _sendMessage() {
    final text = _messageController.text.trim();
    if (text.isEmpty) return;

    final chatProvider = context.read<ChatProvider>();
    final chat = chatProvider.chats.firstWhere(
      (c) => c.chatId == widget.chatId,
      orElse: () => ChatModel(),
    );

    final receiverId = chat.getOtherParticipantId(
      chatProvider.currentUserId ?? '',
    );
    if (receiverId == null) return;

    chatProvider.sendMessage(
      chatId: widget.chatId,
      receiverId: receiverId,
      messageText: text,
    );

    _messageController.clear();
    _scrollToBottom();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  void _pickImage() async {
    final picker = ImagePicker();
    final image = await picker.pickImage(source: ImageSource.gallery);

    if (image != null && mounted) {
      setState(() => _isLoadingImage = true);
      
      try {
        // Compress image
        final imageFile = File(image.path);
        final compressedImage = await ImageCompressionService.compressPostImage(imageFile);
        
        // Upload to Cloudinary
        final imageUrl = await CloudinaryService.uploadImage(
          compressedImage,
          folder: 'chat_images',
        );
        
        if (imageUrl != null && mounted) {
          // Send image message
          final chatProvider = context.read<ChatProvider>();
          final chat = chatProvider.chats.firstWhere(
            (c) => c.chatId == widget.chatId,
            orElse: () => ChatModel(),
          );
          
          final receiverId = chat.getOtherParticipantId(
            chatProvider.currentUserId ?? '',
          );
          
          if (receiverId != null) {
            await chatProvider.sendMessage(
              chatId: widget.chatId,
              receiverId: receiverId,
              messageText: '📷 Photo',
              messageType: 'image',
              imageUrl: imageUrl,
            );
          }
        } else if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Failed to upload image'),
              backgroundColor: Colors.red,
            ),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Error: $e'),
              backgroundColor: Colors.red,
            ),
          );
        }
      } finally {
        if (mounted) {
          setState(() => _isLoadingImage = false);
        }
      }
    }
  }

  void _takePhoto() async {
    final picker = ImagePicker();
    final image = await picker.pickImage(source: ImageSource.camera);

    if (image != null && mounted) {
      setState(() => _isLoadingImage = true);
      
      try {
        // Compress image
        final imageFile = File(image.path);
        final compressedImage = await ImageCompressionService.compressPostImage(imageFile);
        
        // Upload to Cloudinary
        final imageUrl = await CloudinaryService.uploadImage(
          compressedImage,
          folder: 'chat_images',
        );
        
        if (imageUrl != null && mounted) {
          // Send image message
          final chatProvider = context.read<ChatProvider>();
          final chat = chatProvider.chats.firstWhere(
            (c) => c.chatId == widget.chatId,
            orElse: () => ChatModel(),
          );
          
          final receiverId = chat.getOtherParticipantId(
            chatProvider.currentUserId ?? '',
          );
          
          if (receiverId != null) {
            await chatProvider.sendMessage(
              chatId: widget.chatId,
              receiverId: receiverId,
              messageText: '📷 Photo',
              messageType: 'image',
              imageUrl: imageUrl,
            );
          }
        } else if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Failed to upload photo'),
              backgroundColor: Colors.red,
            ),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Error: $e'),
              backgroundColor: Colors.red,
            ),
          );
        }
      } finally {
        if (mounted) {
          setState(() => _isLoadingImage = false);
        }
      }
    }
  }

  void _toggleRecording() {
    setState(() {
      _isRecording = !_isRecording;
    });

    if (_isRecording) {
      // TODO: Start recording voice note
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Recording... Tap again to stop'),
          duration: Duration(seconds: 1),
        ),
      );
    } else {
      // TODO: Stop recording and send voice note
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Voice note sent!'),
          backgroundColor: AppColors.success,
          duration: Duration(seconds: 1),
        ),
      );
    }
  }

  void _showAttachmentOptions() {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => Container(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text(
              'Send Attachment',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 20),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _buildAttachmentOption(
                  icon: Icons.photo_library,
                  label: 'Gallery',
                  color: Colors.purple,
                  onTap: () {
                    Navigator.pop(context);
                    _pickImage();
                  },
                ),
                _buildAttachmentOption(
                  icon: Icons.camera_alt,
                  label: 'Camera',
                  color: Colors.pink,
                  onTap: () {
                    Navigator.pop(context);
                    _takePhoto();
                  },
                ),
                _buildAttachmentOption(
                  icon: Icons.insert_drive_file,
                  label: 'Document',
                  color: Colors.blue,
                  onTap: () {
                    Navigator.pop(context);
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('Document picker coming soon!'),
                      ),
                    );
                  },
                ),
              ],
            ),
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }

  Widget _buildAttachmentOption({
    required IconData icon,
    required String label,
    required Color color,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: color.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(icon, color: color, size: 32),
          ),
          const SizedBox(height: 8),
          Text(
            label,
            style: const TextStyle(fontSize: 12),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: AppColors.primary,
        elevation: 1,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.pop(context),
        ),
        title: GestureDetector(
          onTap: () {
            // Navigate to profile screen - we need to get the other user's ID
            final chatProvider = context.read<ChatProvider>();
            final chat = chatProvider.chats.firstWhere(
              (c) => c.chatId == widget.chatId,
              orElse: () => ChatModel(),
            );
            final otherUserId = chat.getOtherParticipantId(
              chatProvider.currentUserId ?? '',
            );
            if (otherUserId != null) {
              context.push('/view-profile/$otherUserId');
            }
          },
          child: Row(
            children: [
              CircleAvatar(
                radius: 18,
                backgroundColor: Colors.white,
                backgroundImage: widget.otherUserImage != null
                    ? CachedNetworkImageProvider(widget.otherUserImage!)
                    : null,
                child: widget.otherUserImage == null
                    ? Text(
                        widget.otherUserName.substring(0, 1).toUpperCase(),
                        style: const TextStyle(
                          color: AppColors.primary,
                          fontWeight: FontWeight.bold,
                        ),
                      )
                    : null,
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      widget.otherUserName,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                      overflow: TextOverflow.ellipsis,
                    ),
                    Consumer<ChatProvider>(
                      builder: (context, provider, _) {
                        // Get online status from other user
                        final onlineStatus = _otherUser != null
                            ? TimeFormatter.formatOnlineStatusFromMillis(
                                _otherUser!.lastActive?.millisecondsSinceEpoch,
                              )
                            : 'Offline';
                        
                        final messageCount = provider.messages.length;

                        return Row(
                          children: [
                            Text(
                              onlineStatus,
                              style: TextStyle(
                                color: onlineStatus == 'Online' 
                                    ? Colors.lightGreenAccent 
                                    : Colors.white70,
                                fontSize: 12,
                                fontWeight: onlineStatus == 'Online' 
                                    ? FontWeight.w600 
                                    : FontWeight.normal,
                              ),
                            ),
                            if (messageCount > 0) ...[
                              const SizedBox(width: 8),
                              Text(
                                '• $messageCount messages',
                                style: const TextStyle(
                                  color: Colors.white60,
                                  fontSize: 11,
                                ),
                              ),
                            ],
                          ],
                        );
                      },
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.call, color: Colors.white),
            onPressed: _makePhoneCall,
            tooltip: 'Voice call',
          ),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: Consumer<ChatProvider>(
              builder: (context, provider, _) {
                final messages = provider.messages;
                final currentUserId = provider.currentUserId;

                if (messages.isEmpty) {
                  return const Center(
                    child: Text(
                      'No messages yet. Say hello!',
                      style: TextStyle(color: AppColors.textSecondary),
                    ),
                  );
                }

                WidgetsBinding.instance.addPostFrameCallback(
                  (_) => _scrollToBottom(),
                );

                return ListView.builder(
                  controller: _scrollController,
                  padding: const EdgeInsets.all(16),
                  itemCount: messages.length,
                  itemBuilder: (context, index) {
                    final message = messages[index];
                    final isMe = message.senderId == currentUserId;
                    final previousMessage =
                        index > 0 ? messages[index - 1] : null;
                    final showDateSeparator =
                        _shouldShowDateSeparator(message, previousMessage);

                    return Column(
                      children: [
                        if (showDateSeparator)
                          _buildDateSeparator(message.timestamp!),
                        _buildMessageBubble(message, isMe),
                      ],
                    );
                  },
                );
              },
            ),
          ),
          _buildMessageInput(),
        ],
      ),
    );
  }

  bool _shouldShowDateSeparator(
      ChatMessageModel message, ChatMessageModel? previousMessage) {
    if (previousMessage == null) return true;
    if (message.timestamp == null || previousMessage.timestamp == null) {
      return false;
    }

    final messageDate = DateTime(
      message.timestamp!.year,
      message.timestamp!.month,
      message.timestamp!.day,
    );
    final previousDate = DateTime(
      previousMessage.timestamp!.year,
      previousMessage.timestamp!.month,
      previousMessage.timestamp!.day,
    );

    return !messageDate.isAtSameMomentAs(previousDate);
  }

  Widget _buildDateSeparator(DateTime date) {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final yesterday = today.subtract(const Duration(days: 1));
    final messageDate = DateTime(date.year, date.month, date.day);

    String dateText;
    if (messageDate.isAtSameMomentAs(today)) {
      dateText = 'Today';
    } else if (messageDate.isAtSameMomentAs(yesterday)) {
      dateText = 'Yesterday';
    } else {
      dateText = '${date.day}/${date.month}/${date.year}';
    }

    return Container(
      margin: const EdgeInsets.symmetric(vertical: 16),
      child: Row(
        children: [
          const Expanded(child: Divider(color: Colors.grey)),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            decoration: BoxDecoration(
              color: Colors.grey[200],
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              dateText,
              style: const TextStyle(
                fontSize: 12,
                color: Colors.grey,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
          const Expanded(child: Divider(color: Colors.grey)),
        ],
      ),
    );
  }

  Widget _buildMessageBubble(ChatMessageModel message, bool isMe) {
    return Align(
      alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.only(bottom: 8),
        padding: message.messageType == 'image' 
            ? EdgeInsets.zero 
            : const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        constraints: BoxConstraints(
          maxWidth: MediaQuery.of(context).size.width * 0.75,
        ),
        decoration: BoxDecoration(
          color: message.messageType == 'image' 
              ? Colors.transparent 
              : (isMe ? AppColors.primary : Colors.grey[200]),
          borderRadius: BorderRadius.only(
            topLeft: const Radius.circular(16),
            topRight: const Radius.circular(16),
            bottomLeft: Radius.circular(isMe ? 16 : 4),
            bottomRight: Radius.circular(isMe ? 4 : 16),
          ),
          boxShadow: message.messageType == 'image' 
              ? [] 
              : [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.1),
                    blurRadius: 2,
                    offset: const Offset(0, 1),
                  ),
                ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            // Image message
            if (message.messageType == 'image' && message.imageUrl != null)
              ClipRRect(
                borderRadius: BorderRadius.only(
                  topLeft: const Radius.circular(16),
                  topRight: const Radius.circular(16),
                  bottomLeft: Radius.circular(isMe ? 16 : 4),
                  bottomRight: Radius.circular(isMe ? 4 : 16),
                ),
                child: CachedNetworkImage(
                  imageUrl: message.imageUrl!,
                  fit: BoxFit.cover,
                  placeholder: (context, url) => Container(
                    height: 200,
                    color: Colors.grey[300],
                    child: const Center(
                      child: CircularProgressIndicator(),
                    ),
                  ),
                  errorWidget: (context, url, error) => Container(
                    height: 200,
                    color: Colors.grey[300],
                    child: const Icon(Icons.error),
                  ),
                ),
              ),
            // Text message
            if (message.messageType == 'text')
              Align(
                alignment: Alignment.centerLeft,
                child: Text(
                  message.displayText,
                  style: TextStyle(
                    color: isMe ? Colors.white : AppColors.textPrimary,
                    fontSize: 16,
                  ),
                ),
              ),
            const SizedBox(height: 6),
            Container(
              padding: message.messageType == 'image' 
                  ? const EdgeInsets.symmetric(horizontal: 8, vertical: 4)
                  : EdgeInsets.zero,
              decoration: message.messageType == 'image'
                  ? BoxDecoration(
                      color: Colors.black.withValues(alpha: 0.5),
                      borderRadius: BorderRadius.circular(12),
                    )
                  : null,
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    message.formattedTime,
                    style: TextStyle(
                      fontSize: 11,
                      color: message.messageType == 'image'
                          ? Colors.white
                          : (isMe ? Colors.white70 : AppColors.textSecondary),
                      fontWeight: FontWeight.w400,
                    ),
                  ),
                  if (isMe) ...[
                    const SizedBox(width: 4),
                    Icon(
                      message.isRead ? Icons.done_all : Icons.done,
                      size: 16,
                      color: message.messageType == 'image'
                          ? Colors.white
                          : (message.isRead ? Colors.lightBlue : Colors.white70),
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMessageInput() {
    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 10,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (_isLoadingImage)
              Container(
                padding: const EdgeInsets.all(8),
                child: Row(
                  children: [
                    const SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    ),
                    const SizedBox(width: 12),
                    Text(
                      'Uploading image...',
                      style: TextStyle(
                        color: AppColors.textSecondary,
                        fontSize: 14,
                      ),
                    ),
                  ],
                ),
              ),
            Row(
              children: [
                IconButton(
                  icon: Icon(
                    Icons.attach_file,
                    color: AppColors.primary,
                  ),
                  onPressed: _isLoadingImage ? null : _showAttachmentOptions,
                  tooltip: 'Attach file',
                ),
                Expanded(
                  child: TextField(
                    controller: _messageController,
                    enabled: !_isLoadingImage,
                    decoration: InputDecoration(
                      hintText: 'Type a message...',
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(24),
                        borderSide: BorderSide.none,
                      ),
                      filled: true,
                      fillColor: Colors.grey[100],
                      contentPadding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 8,
                      ),
                    ),
                    textCapitalization: TextCapitalization.sentences,
                    onSubmitted: (_) => _sendMessage(),
                  ),
                ),
                const SizedBox(width: 8),
                if (!_hasText)
                  CircleAvatar(
                    backgroundColor: _isRecording ? Colors.red : AppColors.primary,
                    child: IconButton(
                      icon: Icon(
                        _isRecording ? Icons.stop : Icons.mic,
                        color: Colors.white,
                      ),
                      onPressed: _isLoadingImage ? null : _toggleRecording,
                      tooltip:
                          _isRecording ? 'Stop recording' : 'Record voice note',
                    ),
                  )
                else
                  CircleAvatar(
                    backgroundColor: AppColors.primary,
                    child: IconButton(
                      icon: const Icon(Icons.send, color: Colors.white),
                      onPressed: _isLoadingImage ? null : _sendMessage,
                    ),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
