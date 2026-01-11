import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:alumni_portal/providers/notification_provider.dart';
import 'package:alumni_portal/config/theme.dart';

class NotificationsScreen extends StatelessWidget {
  const NotificationsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Notifications'),
        actions: [
          TextButton(
            onPressed: () =>
                context.read<NotificationProvider>().markAllAsRead(),
            child: const Text('Mark all read'),
          ),
        ],
      ),
      body: Consumer<NotificationProvider>(
        builder: (context, provider, _) {
          final notifications = provider.notifications;
          if (notifications.isEmpty) {
            return const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.notifications_none,
                    size: 64,
                    color: AppColors.textSecondary,
                  ),
                  SizedBox(height: 16),
                  Text(
                    'No notifications',
                    style: TextStyle(color: AppColors.textSecondary),
                  ),
                ],
              ),
            );
          }
          return ListView.builder(
            itemCount: notifications.length,
            itemBuilder: (context, index) {
              final notification = notifications[index];
              return Dismissible(
                key: Key(notification.id ?? index.toString()),
                onDismissed: (_) =>
                    provider.deleteNotification(notification.id!),
                background: Container(
                  color: AppColors.error,
                  alignment: Alignment.centerRight,
                  padding: const EdgeInsets.only(right: 16),
                  child: const Icon(Icons.delete, color: Colors.white),
                ),
                child: ListTile(
                  leading: CircleAvatar(
                    backgroundColor: notification.read
                        ? Colors.grey[200]
                        : AppColors.primary.withOpacity(0.1),
                    child: Text(notification.icon),
                  ),
                  title: Text(
                    notification.title ?? 'Notification',
                    style: TextStyle(
                      fontWeight: notification.read
                          ? FontWeight.normal
                          : FontWeight.bold,
                    ),
                  ),
                  subtitle: Text(notification.message ?? ''),
                  trailing: Text(
                    notification.timeAgo,
                    style: const TextStyle(
                      fontSize: 12,
                      color: AppColors.textSecondary,
                    ),
                  ),
                  onTap: () => provider.markAsRead(notification.id!),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
