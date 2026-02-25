import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';

class NotificationService {
  static final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  static final FirebaseAuth _auth = FirebaseAuth.instance;

  static String? get _currentUserId => _auth.currentUser?.uid;

  static Future<void> _createNotification({
    required String userId,
    required String title,
    required String message,
    required String type,
    String? referenceId,
  }) async {
    // Don't notify yourself
    if (userId == _currentUserId) return;

    try {
      await _firestore.collection('notifications').add({
        'userId': userId,
        'title': title,
        'message': message,
        'type': type,
        'referenceId': referenceId,
        'read': false,
        'timestamp': DateTime.now().millisecondsSinceEpoch,
      });
    } catch (e) {
      // Silently fail - notifications are not critical
    }
  }

  // Profile view notification
  static Future<void> notifyProfileView({
    required String viewedUserId,
    required String viewerName,
  }) async {
    await _createNotification(
      userId: viewedUserId,
      title: 'Profile View',
      message: '$viewerName viewed your profile',
      type: 'profile_view',
      referenceId: _currentUserId,
    );
  }

  // Post reaction notification
  static Future<void> notifyPostReaction({
    required String postAuthorId,
    required String reactorName,
    required String reactionType,
    required String postId,
  }) async {
    final reactionEmoji = reactionType == 'likes'
        ? '👍'
        : reactionType == 'celebrates'
            ? '🎉'
            : '❤️';
    await _createNotification(
      userId: postAuthorId,
      title: 'New Reaction',
      message: '$reactorName $reactionEmoji your post',
      type: 'post_reaction',
      referenceId: postId,
    );
  }

  // Post comment notification
  static Future<void> notifyPostComment({
    required String postAuthorId,
    required String commenterName,
    required String postId,
  }) async {
    await _createNotification(
      userId: postAuthorId,
      title: 'New Comment',
      message: '$commenterName commented on your post',
      type: 'post_comment',
      referenceId: postId,
    );
  }

  // Job referral notification
  static Future<void> notifyJobReferral({
    required String referredUserId,
    required String referrerName,
    required String jobTitle,
    required String referralId,
  }) async {
    await _createNotification(
      userId: referredUserId,
      title: 'Job Referral! 🎯',
      message: '$referrerName referred you for $jobTitle',
      type: 'job_referral',
      referenceId: referralId,
    );
  }

  // Skill endorsement notification
  static Future<void> notifySkillEndorsement({
    required String endorsedUserId,
    required String endorserName,
    required String skill,
  }) async {
    await _createNotification(
      userId: endorsedUserId,
      title: 'Skill Endorsed ✨',
      message: '$endorserName endorsed you for $skill',
      type: 'endorsement',
    );
  }

  // Recommendation notification
  static Future<void> notifyRecommendation({
    required String recommendedUserId,
    required String recommenderName,
  }) async {
    await _createNotification(
      userId: recommendedUserId,
      title: 'New Recommendation 📝',
      message: '$recommenderName wrote you a recommendation',
      type: 'recommendation',
    );
  }

  // Mentorship request notification
  static Future<void> notifyMentorshipRequest({
    required String mentorId,
    required String menteeName,
    required String requestId,
  }) async {
    await _createNotification(
      userId: mentorId,
      title: 'Mentorship Request 🎓',
      message: '$menteeName wants you as their mentor',
      type: 'mentorship_request',
      referenceId: requestId,
    );
  }

  // Mentorship accepted notification
  static Future<void> notifyMentorshipAccepted({
    required String menteeId,
    required String mentorName,
  }) async {
    await _createNotification(
      userId: menteeId,
      title: 'Mentorship Accepted! 🎉',
      message: '$mentorName accepted your mentorship request',
      type: 'mentorship_accepted',
    );
  }

  // New message notification
  static Future<void> notifyNewMessage({
    required String recipientId,
    required String senderName,
    required String senderId,
    String? messagePreview,
  }) async {
    await _createNotification(
      userId: recipientId,
      title: senderName,
      message: messagePreview ?? 'sent you a message',
      type: 'message',
      referenceId: senderId, // Use sender's ID so we can navigate to their chat
    );
  }

  // Connection request notification
  static Future<void> notifyConnectionRequest({
    required String targetUserId,
    required String requesterName,
  }) async {
    await _createNotification(
      userId: targetUserId,
      title: 'Connection Request',
      message: '$requesterName wants to connect with you',
      type: 'connection_request',
    );
  }

  // Event reminder notification
  static Future<void> notifyEventReminder({
    required String userId,
    required String eventTitle,
    required String eventId,
  }) async {
    await _createNotification(
      userId: userId,
      title: 'Event Reminder 📅',
      message: '$eventTitle is starting soon',
      type: 'event_reminder',
      referenceId: eventId,
    );
  }

  // New follower/connection notification
  static Future<void> notifyNewConnection({
    required String userId,
    required String connectorName,
  }) async {
    await _createNotification(
      userId: userId,
      title: 'New Connection 🤝',
      message: '$connectorName is now connected with you',
      type: 'new_connection',
    );
  }

  // Poll vote notification (for poll creator)
  static Future<void> notifyPollVote({
    required String pollAuthorId,
    required String voterName,
    required String pollId,
  }) async {
    await _createNotification(
      userId: pollAuthorId,
      title: 'Poll Vote',
      message: '$voterName voted on your poll',
      type: 'poll_vote',
      referenceId: pollId,
    );
  }

  // Story view notification
  static Future<void> notifyStoryView({
    required String storyAuthorId,
    required String viewerName,
  }) async {
    await _createNotification(
      userId: storyAuthorId,
      title: 'Story Viewed 👀',
      message: '$viewerName viewed your story',
      type: 'story_view',
    );
  }

  // New follower notification
  static Future<void> notifyNewFollower({
    required String userId,
    required String followerName,
    required String followerId,
  }) async {
    await _createNotification(
      userId: userId,
      title: 'New Follower',
      message: '$followerName started following you',
      type: 'new_follower',
      referenceId: followerId,
    );
  }

  // Story like notification
  static Future<void> notifyStoryLike({
    required String storyAuthorId,
    required String likerName,
    required String storyId,
  }) async {
    await _createNotification(
      userId: storyAuthorId,
      title: 'Story Liked',
      message: '$likerName liked your story',
      type: 'story_like',
      referenceId: storyId,
    );
  }

  // Post like notification
  static Future<void> notifyPostLike({
    required String postAuthorId,
    required String likerName,
    required String postId,
  }) async {
    await _createNotification(
      userId: postAuthorId,
      title: 'Post Liked',
      message: '$likerName liked your post',
      type: 'post_like',
      referenceId: postId,
    );
  }

  // Badge earned notification
  static Future<void> notifyBadgeEarned({
    required String userId,
    required String badgeName,
    required String badgeIcon,
  }) async {
    await _createNotification(
      userId: userId,
      title: 'Badge Earned! $badgeIcon',
      message: 'You earned the "$badgeName" badge',
      type: 'badge_earned',
    );
  }
}
