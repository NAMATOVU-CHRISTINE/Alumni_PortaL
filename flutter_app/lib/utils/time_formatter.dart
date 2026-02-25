class TimeFormatter {
  static String formatLastSeen(DateTime? lastSeenTime) {
    if (lastSeenTime == null) return 'Never';

    final now = DateTime.now();
    final difference = now.difference(lastSeenTime);

    if (difference.inMinutes < 1) {
      return 'Just now';
    } else if (difference.inMinutes < 60) {
      final minutes = difference.inMinutes;
      return '$minutes ${minutes == 1 ? 'minute' : 'minutes'} ago';
    } else if (difference.inHours < 24) {
      final hours = difference.inHours;
      return '$hours ${hours == 1 ? 'hour' : 'hours'} ago';
    } else if (difference.inDays < 7) {
      final days = difference.inDays;
      return '$days ${days == 1 ? 'day' : 'days'} ago';
    } else if (difference.inDays < 30) {
      final weeks = (difference.inDays / 7).floor();
      return '$weeks ${weeks == 1 ? 'week' : 'weeks'} ago';
    } else if (difference.inDays < 365) {
      final months = (difference.inDays / 30).floor();
      return '$months ${months == 1 ? 'month' : 'months'} ago';
    } else {
      final years = (difference.inDays / 365).floor();
      return '$years ${years == 1 ? 'year' : 'years'} ago';
    }
  }

  static String formatLastSeenFromString(String? lastSeenString) {
    if (lastSeenString == null || lastSeenString.isEmpty) {
      return 'Never';
    }

    try {
      final lastSeenTime = DateTime.parse(lastSeenString);
      return formatLastSeen(lastSeenTime);
    } catch (e) {
      return 'Unknown';
    }
  }

  // Format online status for chat
  static String formatOnlineStatus(DateTime? lastActive) {
    if (lastActive == null) return 'Offline';

    final now = DateTime.now();
    final difference = now.difference(lastActive);

    // Consider online if active within last 2 minutes
    if (difference.inMinutes < 2) {
      return 'Online';
    }

    // Show last seen
    return 'Last seen ${formatLastSeen(lastActive)}';
  }

  // Format online status from milliseconds
  static String formatOnlineStatusFromMillis(int? lastActiveMillis) {
    if (lastActiveMillis == null) return 'Offline';
    
    final lastActive = DateTime.fromMillisecondsSinceEpoch(lastActiveMillis);
    return formatOnlineStatus(lastActive);
  }
}
