import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/providers/chat_provider.dart';
import 'package:alumni_portal/models/chat_model.dart';
import 'package:alumni_portal/config/theme.dart';

class ChatListScreen extends StatefulWidget {
  const ChatListScreen({super.key});

  @override
  State<ChatListScreen> createState() => _ChatListScreenState();
}

class _ChatListScreenState extends State<ChatListScreen> {
  @override
  void initState() {
    super.initState();
    context.read<ChatProvider>().listenToChats();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Messages')),
      body: Consumer<ChatProvider>(
        builder: (context, provider, _) {
          final chats = provider.chats;
          final currentUserId = provider.currentUserId;

          if (chats.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(
                    Icons.chat_bubble_outline,
                    size: 64,
                    color: AppColors.textSecondary,
                  ),
                  const SizedBox(height: 16),
                  const Text(
                    'No conversations yet',
                    style: TextStyle(color: AppColors.textSecondary),
                  ),
                  const SizedBox(height: 8),
                  TextButton(
                    onPressed: () => context.go('/directory'),
                    child: const Text('Find alumni to connect'),
                  ),
                ],
              ),
            );
          }

          return ListView.builder(
            itemCount: chats.length,
            itemBuilder: (context, index) =>
                _buildChatTile(chats[index], currentUserId ?? ''),
          );
        },
      ),
    );
  }

  Widget _buildChatTile(ChatModel chat, String currentUserId) {
    final displayName = chat.getDisplayName(currentUserId);
    final displayImage = chat.isDirectChat
        ? chat.getOtherParticipantImage(currentUserId)
        : chat.chatImage;
    final unreadCount = chat.getUnreadCount(currentUserId);

    return ListTile(
      leading: CircleAvatar(
        backgroundColor: AppColors.primary.withOpacity(0.1),
        backgroundImage: displayImage != null
            ? CachedNetworkImageProvider(displayImage)
            : null,
        child: displayImage == null
            ? Text(
                displayName.substring(0, 1).toUpperCase(),
                style: const TextStyle(
                  color: AppColors.primary,
                  fontWeight: FontWeight.bold,
                ),
              )
            : null,
      ),
      title: Row(
        children: [
          Expanded(
            child: Text(
              displayName,
              style: TextStyle(
                fontWeight: unreadCount > 0
                    ? FontWeight.bold
                    : FontWeight.normal,
              ),
            ),
          ),
          Text(
            chat.lastMessageTimeAgo,
            style: const TextStyle(
              fontSize: 12,
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
      subtitle: Row(
        children: [
          Expanded(
            child: Text(
              chat.lastMessageDisplayText,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(
                color: unreadCount > 0
                    ? AppColors.textPrimary
                    : AppColors.textSecondary,
                fontWeight: unreadCount > 0
                    ? FontWeight.w500
                    : FontWeight.normal,
              ),
            ),
          ),
          if (unreadCount > 0)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
              decoration: BoxDecoration(
                color: AppColors.primary,
                borderRadius: BorderRadius.circular(10),
              ),
              child: Text(
                '$unreadCount',
                style: const TextStyle(color: Colors.white, fontSize: 12),
              ),
            ),
        ],
      ),
      onTap: () {
        context.read<ChatProvider>().markChatAsRead(chat.chatId!);
        context.push('/chat/${chat.chatId}', extra: displayName);
      },
    );
  }
}
