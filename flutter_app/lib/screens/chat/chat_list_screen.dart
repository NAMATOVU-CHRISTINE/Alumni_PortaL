import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/providers/chat_provider.dart';
import 'package:alumni_portal/models/chat_model.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/widgets/empty_state_widget.dart';

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
            return EmptyStates.noChats(
              onStartChat: () => context.go('/directory'),
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

    return Container(
      decoration: BoxDecoration(
        color: unreadCount > 0
            ? AppColors.primary.withValues(alpha: 0.05)
            : Colors.transparent,
        border: Border(
          left: BorderSide(
            color: unreadCount > 0 ? AppColors.primary : Colors.transparent,
            width: 3,
          ),
        ),
      ),
      child: ListTile(
        leading: Stack(
          children: [
            CircleAvatar(
              radius: 24,
              backgroundColor: AppColors.primary.withValues(alpha: 0.1),
              backgroundImage: displayImage != null && displayImage.isNotEmpty
                  ? CachedNetworkImageProvider(displayImage)
                  : null,
              child: displayImage == null || displayImage.isEmpty
                  ? Text(
                      displayName.substring(0, 1).toUpperCase(),
                      style: const TextStyle(
                        color: AppColors.primary,
                        fontWeight: FontWeight.bold,
                        fontSize: 20,
                      ),
                    )
                  : null,
            ),
            if (unreadCount > 0)
              Positioned(
                right: 0,
                top: 0,
                child: Container(
                  width: 12,
                  height: 12,
                  decoration: const BoxDecoration(
                    color: Colors.green,
                    shape: BoxShape.circle,
                  ),
                ),
              ),
          ],
        ),
        title: Row(
          children: [
            Expanded(
              child: Text(
                displayName,
                style: TextStyle(
                  fontWeight:
                      unreadCount > 0 ? FontWeight.bold : FontWeight.normal,
                  fontSize: 16,
                ),
              ),
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  chat.lastMessageTimeAgo,
                  style: TextStyle(
                    fontSize: 12,
                    color: unreadCount > 0
                        ? AppColors.primary
                        : AppColors.textSecondary,
                    fontWeight:
                        unreadCount > 0 ? FontWeight.w600 : FontWeight.normal,
                  ),
                ),
                if (unreadCount > 0)
                  Container(
                    margin: const EdgeInsets.only(top: 4),
                    padding:
                        const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                    decoration: BoxDecoration(
                      color: AppColors.primary,
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Text(
                      '$unreadCount',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
              ],
            ),
          ],
        ),
        subtitle: Row(
          children: [
            if (chat.lastMessageSenderId == currentUserId) ...[
              Icon(
                Icons.done_all,
                size: 16,
                color: chat.lastMessageText != null ? Colors.blue : Colors.grey,
              ),
              const SizedBox(width: 4),
            ],
            Expanded(
              child: Text(
                chat.lastMessageDisplayText,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  color: unreadCount > 0
                      ? AppColors.textPrimary
                      : AppColors.textSecondary,
                  fontWeight:
                      unreadCount > 0 ? FontWeight.w500 : FontWeight.normal,
                ),
              ),
            ),
          ],
        ),
        onTap: () {
          context.read<ChatProvider>().markChatAsRead(chat.chatId!);
          final lastSeenTime = chat.getOtherParticipantLastSeen(currentUserId);

          context.push(
            '/chat/${chat.chatId}',
            extra: {
              'name': displayName,
              'image': displayImage,
              'lastSeen': lastSeenTime,
            },
          );
        },
      ),
    );
  }
}
