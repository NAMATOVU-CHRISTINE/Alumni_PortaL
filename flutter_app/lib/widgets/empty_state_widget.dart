import 'package:flutter/material.dart';
import 'package:alumni_portal/config/theme.dart';

class EmptyStateWidget extends StatelessWidget {
  final IconData icon;
  final String title;
  final String message;
  final String? actionLabel;
  final VoidCallback? onAction;

  const EmptyStateWidget({
    super.key,
    required this.icon,
    required this.title,
    required this.message,
    this.actionLabel,
    this.onAction,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 80,
              color: Colors.grey[300],
            ),
            const SizedBox(height: 24),
            Text(
              title,
              style: const TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
                color: Colors.black87,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 12),
            Text(
              message,
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey[600],
              ),
              textAlign: TextAlign.center,
            ),
            if (actionLabel != null && onAction != null) ...[
              const SizedBox(height: 24),
              ElevatedButton.icon(
                onPressed: onAction,
                icon: const Icon(Icons.add),
                label: Text(actionLabel!),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 24,
                    vertical: 12,
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

/// Pre-built empty states for common scenarios
class EmptyStates {
  static Widget noPosts({VoidCallback? onCreatePost}) => EmptyStateWidget(
        icon: Icons.article_outlined,
        title: 'No Posts Yet',
        message: 'Be the first to share something with the community!',
        actionLabel: 'Create Post',
        onAction: onCreatePost,
      );

  static Widget noChats({VoidCallback? onStartChat}) => EmptyStateWidget(
        icon: Icons.chat_bubble_outline,
        title: 'No Conversations',
        message: 'Start chatting with alumni to build your network!',
        actionLabel: 'Start Chat',
        onAction: onStartChat,
      );

  static Widget noEvents({VoidCallback? onCreateEvent}) => EmptyStateWidget(
        icon: Icons.event_outlined,
        title: 'No Events',
        message: 'No upcoming events at the moment. Check back later!',
        actionLabel: 'Create Event',
        onAction: onCreateEvent,
      );

  static Widget noJobs({VoidCallback? onPostJob}) => EmptyStateWidget(
        icon: Icons.work_outline,
        title: 'No Job Listings',
        message: 'No job opportunities available right now.',
        actionLabel: 'Post a Job',
        onAction: onPostJob,
      );

  static Widget noNotifications() => const EmptyStateWidget(
        icon: Icons.notifications_none,
        title: 'No Notifications',
        message: 'You\'re all caught up! No new notifications.',
      );

  static Widget noSearchResults(String query) => EmptyStateWidget(
        icon: Icons.search_off,
        title: 'No Results Found',
        message: 'We couldn\'t find anything matching "$query"',
      );

  static Widget noStories({VoidCallback? onCreateStory}) => EmptyStateWidget(
        icon: Icons.auto_stories_outlined,
        title: 'No Stories',
        message: 'Share your moment with the community!',
        actionLabel: 'Create Story',
        onAction: onCreateStory,
      );

  static Widget error({
    required String message,
    VoidCallback? onRetry,
  }) =>
      EmptyStateWidget(
        icon: Icons.error_outline,
        title: 'Oops! Something went wrong',
        message: message,
        actionLabel: onRetry != null ? 'Try Again' : null,
        onAction: onRetry,
      );

  static Widget noConnection({VoidCallback? onRetry}) => EmptyStateWidget(
        icon: Icons.wifi_off,
        title: 'No Internet Connection',
        message: 'Please check your connection and try again.',
        actionLabel: 'Retry',
        onAction: onRetry,
      );
}
