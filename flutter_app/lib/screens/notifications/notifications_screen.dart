import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:alumni_portal/providers/notification_provider.dart';
import 'package:alumni_portal/providers/chat_provider.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/widgets/app_bar_decoration.dart';
import 'package:alumni_portal/services/firebase_messaging_service.dart';
import 'package:alumni_portal/widgets/empty_state_widget.dart';

class NotificationsScreen extends StatelessWidget {
  const NotificationsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: MustAppBar(
        title: const Text('Notifications'),
        actions: [
          PopupMenuButton(
            icon: const Icon(Icons.more_vert),
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: 'mark_all_read',
                child: Row(
                  children: [
                    Icon(Icons.done_all, size: 20),
                    SizedBox(width: 8),
                    Text('Mark all as read'),
                  ],
                ),
              ),
              const PopupMenuItem(
                value: 'clear_all',
                child: Row(
                  children: [
                    Icon(Icons.clear_all, size: 20, color: Colors.red),
                    SizedBox(width: 8),
                    Text('Clear all', style: TextStyle(color: Colors.red)),
                  ],
                ),
              ),
            ],
            onSelected: (value) {
              final provider = context.read<NotificationProvider>();
              if (value == 'mark_all_read') {
                provider.markAllAsRead();
              } else if (value == 'clear_all') {
                _showClearConfirmation(context, provider);
              }
            },
          ),
        ],
      ),
      body: Consumer<NotificationProvider>(
        builder: (context, provider, _) {
          final notifications = provider.notifications;
          
          if (provider.isLoading && notifications.isEmpty) {
            return const Center(child: CircularProgressIndicator());
          }
          
          if (notifications.isEmpty) {
            return EmptyStates.noNotifications();
          }
          
          return RefreshIndicator(
            onRefresh: () async => provider.loadNotifications(),
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(vertical: 8),
              itemCount: notifications.length,
              separatorBuilder: (context, index) => Divider(
                height: 1,
                color: Colors.grey[200],
              ),
              itemBuilder: (context, index) {
                final notification = notifications[index];
                return Dismissible(
                  key: Key(notification.id ?? index.toString()),
                  direction: DismissDirection.endToStart,
                  onDismissed: (_) {
                    if (notification.id != null) {
                      provider.deleteNotification(notification.id!);
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('Notification deleted'),
                          duration: Duration(seconds: 2),
                        ),
                      );
                    }
                  },
                  background: Container(
                    color: Colors.red,
                    alignment: Alignment.centerRight,
                    padding: const EdgeInsets.only(right: 20),
                    child: const Icon(
                      Icons.delete,
                      color: Colors.white,
                      size: 28,
                    ),
                  ),
                  child: _buildNotificationTile(context, notification, provider),
                );
              },
            ),
          );
        },
      ),
    );
  }

  Widget _buildNotificationTile(
    BuildContext context,
    notification,
    NotificationProvider provider,
  ) {
    final icon = _getNotificationIcon(notification.type);
    final color = _getNotificationColor(notification.type);
    
    return InkWell(
      onTap: () {
        if (notification.id != null) {
          provider.markAsRead(notification.id!);
        }
        _handleNotificationTap(context, notification);
      },
      child: Container(
        color: notification.read ? Colors.white : AppColors.accent.withOpacity(0.05),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: color.withOpacity(0.1),
                shape: BoxShape.circle,
              ),
              child: Icon(
                icon,
                color: color,
                size: 24,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          notification.title ?? 'Notification',
                          style: TextStyle(
                            fontWeight: notification.read
                                ? FontWeight.w500
                                : FontWeight.w600,
                            fontSize: 14,
                          ),
                        ),
                      ),
                      if (!notification.read)
                        Container(
                          width: 8,
                          height: 8,
                          decoration: const BoxDecoration(
                            color: AppColors.accent,
                            shape: BoxShape.circle,
                          ),
                        ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    notification.message ?? '',
                    style: TextStyle(
                      fontSize: 13,
                      color: Colors.grey[700],
                      height: 1.3,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    notification.timeAgo,
                    style: TextStyle(
                      fontSize: 11,
                      color: Colors.grey[500],
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  IconData _getNotificationIcon(String type) {
    switch (type) {
      case 'profile_view':
        return Icons.visibility;
      case 'post_reaction':
        return Icons.thumb_up;
      case 'post_comment':
        return Icons.comment;
      case 'poll_vote':
        return Icons.poll;
      case 'job_referral':
        return Icons.work;
      case 'endorsement':
        return Icons.star;
      case 'recommendation':
        return Icons.rate_review;
      case 'mentorship_request':
      case 'mentorship_accepted':
        return Icons.school;
      case 'message':
        return Icons.message;
      case 'connection_request':
      case 'new_connection':
        return Icons.people;
      case 'event_reminder':
        return Icons.event;
      case 'story_view':
        return Icons.remove_red_eye;
      case 'badge_earned':
        return Icons.emoji_events;
      default:
        return Icons.notifications;
    }
  }

  Color _getNotificationColor(String type) {
    switch (type) {
      case 'post_reaction':
        return Colors.blue;
      case 'post_comment':
        return Colors.green;
      case 'poll_vote':
        return Colors.purple;
      case 'job_referral':
        return Colors.orange;
      case 'endorsement':
      case 'badge_earned':
        return Colors.amber;
      case 'mentorship_request':
      case 'mentorship_accepted':
        return AppColors.primary;
      case 'message':
        return Colors.teal;
      case 'connection_request':
      case 'new_connection':
        return AppColors.accent;
      default:
        return Colors.grey;
    }
  }

  void _handleNotificationTap(BuildContext context, notification) {
    // Navigate based on notification type
    switch (notification.type) {
      case 'post_reaction':
      case 'post_comment':
      case 'poll_vote':
        if (notification.referenceId != null) {
          // Navigate to post details
          // context.push('/post/${notification.referenceId}');
        }
        break;
      case 'message':
        if (notification.referenceId != null) {
          // referenceId contains the other user's ID for direct messages
          // We need to create or get the chat with this user
          final chatProvider = context.read<ChatProvider>();
          final otherUserId = notification.referenceId!;
          final otherUserName = notification.title ?? 'User';
          
          // Create or get chat
          chatProvider.createOrGetChat(otherUserId, otherUserName).then((chatId) {
            if (chatId != null) {
              context.push('/chat/$chatId');
            } else {
              // Fallback to chat list if chat creation fails
              context.push('/chat');
            }
          });
        } else {
          // Fallback to chat list if no specific user ID
          context.push('/chat');
        }
        break;
      case 'profile_view':
        if (notification.referenceId != null) {
          context.push('/view-profile/${notification.referenceId}');
        }
        break;
      case 'connection_request':
      case 'new_connection':
        if (notification.referenceId != null) {
          context.push('/view-profile/${notification.referenceId}');
        }
        break;
      case 'job_referral':
        if (notification.referenceId != null) {
          // Navigate to job details or referrals page
          context.push('/career/opportunities');
        }
        break;
      case 'endorsement':
      case 'recommendation':
        if (notification.referenceId != null) {
          context.push('/view-profile/${notification.referenceId}');
        } else {
          context.push('/profile');
        }
        break;
      case 'mentorship_request':
      case 'mentorship_accepted':
        // Navigate to mentorship section
        context.push('/mentorship');
        break;
      case 'event_reminder':
        if (notification.referenceId != null) {
          // Navigate to event details
          context.push('/events');
        }
        break;
      case 'badge_earned':
        // Navigate to gamification/achievements page
        context.push('/profile');
        break;
      default:
        // For unknown types, do nothing or go to home
        break;
    }
  }

  void _showClearConfirmation(BuildContext context, NotificationProvider provider) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Clear All Notifications'),
        content: const Text('Are you sure you want to delete all notifications? This action cannot be undone.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              provider.clearAllNotifications();
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('All notifications cleared'),
                  backgroundColor: AppColors.success,
                ),
              );
            },
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Clear All'),
          ),
        ],
      ),
    );
  }
}
