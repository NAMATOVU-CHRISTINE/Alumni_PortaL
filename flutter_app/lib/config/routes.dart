import 'package:flutter/foundation.dart';
import 'package:go_router/go_router.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:alumni_portal/screens/landing_screen.dart';
import 'package:alumni_portal/screens/splash_screen.dart';
import 'package:alumni_portal/screens/auth/login_screen.dart';
import 'package:alumni_portal/screens/auth/signup_screen.dart';
import 'package:alumni_portal/screens/auth/forgot_password_screen.dart';
import 'package:alumni_portal/screens/auth/complete_google_signup_screen.dart';
import 'package:alumni_portal/screens/home/home_screen.dart';
import 'package:alumni_portal/screens/home/main_navigation.dart';
import 'package:alumni_portal/screens/profile/profile_screen.dart';
import 'package:alumni_portal/screens/profile/edit_profile_screen.dart';
import 'package:alumni_portal/screens/profile/view_profile_screen.dart';
import 'package:alumni_portal/screens/directory/alumni_directory_screen.dart';
import 'package:alumni_portal/screens/directory/almater_directory_screen.dart';
import 'package:alumni_portal/screens/jobs/jobs_screen.dart';
import 'package:alumni_portal/screens/jobs/job_details_screen.dart';
import 'package:alumni_portal/screens/jobs/post_job_screen.dart';
import 'package:alumni_portal/screens/events/events_screen.dart';
import 'package:alumni_portal/screens/events/event_details_screen.dart';
import 'package:alumni_portal/screens/events/create_event_screen.dart';
import 'package:alumni_portal/screens/chat/chat_list_screen.dart';
import 'package:alumni_portal/screens/chat/chat_screen.dart';
import 'package:alumni_portal/screens/mentorship/mentorship_screen.dart';
import 'package:alumni_portal/screens/mentorship/mentor_search_screen.dart';
import 'package:alumni_portal/screens/news/news_feed_screen.dart';
import 'package:alumni_portal/screens/news/article_details_screen.dart';
import 'package:alumni_portal/screens/groups/alumni_groups_screen.dart';
import 'package:alumni_portal/screens/groups/group_details_screen.dart';
import 'package:alumni_portal/screens/groups/create_group_screen.dart';
import 'package:alumni_portal/screens/career/career_tips_screen.dart';
import 'package:alumni_portal/screens/career/knowledge_screen.dart';
import 'package:alumni_portal/screens/notifications/notifications_screen.dart';
import 'package:alumni_portal/screens/settings/settings_screen.dart';
import 'package:alumni_portal/screens/settings/notification_settings_screen.dart';
import 'package:alumni_portal/screens/settings/privacy_settings_screen.dart';
import 'package:alumni_portal/screens/feed/feed_screen.dart';
import 'package:alumni_portal/screens/feed/create_post_screen.dart';
import 'package:alumni_portal/screens/profile/badges_screen.dart';
import 'package:alumni_portal/screens/profile/profile_viewers_screen.dart';
import 'package:alumni_portal/screens/polls/polls_screen.dart';
import 'package:alumni_portal/screens/stories/create_story_screen.dart';
import 'package:alumni_portal/screens/jobs/referrals_screen.dart';
import 'package:alumni_portal/screens/marketplace/support_donations_screen.dart';
import 'package:alumni_portal/screens/discussions/discussions_screen.dart';
import 'package:alumni_portal/screens/gamification/achievements_screen.dart';
import 'package:alumni_portal/screens/career/enhanced_career_center.dart';
import 'package:alumni_portal/screens/content/interactive_content_screen.dart';
import 'package:alumni_portal/screens/convocation/enhanced_convocation_team_screen.dart';
import 'package:alumni_portal/screens/guest_lecture/enhanced_guest_lecture_screen.dart';

class AppRouter {
  static final GoRouter router = GoRouter(
    initialLocation: '/',
    refreshListenable: _AuthStateNotifier(),
    redirect: (context, state) async {
      // Check if user is logged in
      final user = FirebaseAuth.instance.currentUser;
      final isLoggedIn = user != null;

      final isOnAuthPage = state.uri.path == '/landing' ||
          state.uri.path == '/login' ||
          state.uri.path == '/signup' ||
          state.uri.path == '/forgot-password' ||
          state.uri.path == '/complete-google-signup';

      // If on splash and logged in, check if profile exists
      if (isLoggedIn && state.uri.path == '/') {
        // Check if user has a Firestore document
        try {
          final doc = await FirebaseFirestore.instance
              .collection('users')
              .doc(user.uid)
              .get();
          if (!doc.exists) {
            return '/complete-google-signup';
          }
        } catch (e) {
          // If error checking, let them through
        }
        return '/home';
      }

      // If logged in and on auth pages, check if profile exists
      if (isLoggedIn &&
          isOnAuthPage &&
          state.uri.path != '/complete-google-signup') {
        try {
          final doc = await FirebaseFirestore.instance
              .collection('users')
              .doc(user.uid)
              .get();
          if (!doc.exists) {
            return '/complete-google-signup';
          }
        } catch (e) {
          // If error checking, let them through
        }
        return '/home';
      }

      // If not logged in and trying to access protected routes, redirect to landing
      if (!isLoggedIn && !isOnAuthPage && state.uri.path != '/') {
        return '/landing';
      }

      return null; // No redirect needed
    },
    routes: [
      GoRoute(path: '/', builder: (context, state) => const SplashScreen()),
      GoRoute(
        path: '/landing',
        builder: (context, state) => const LandingScreen(),
      ),

      // Auth routes
      GoRoute(path: '/login', builder: (context, state) => const LoginScreen()),
      GoRoute(
        path: '/signup',
        builder: (context, state) => const SignupScreen(),
      ),
      GoRoute(
        path: '/forgot-password',
        builder: (context, state) => const ForgotPasswordScreen(),
      ),
      GoRoute(
        path: '/complete-google-signup',
        builder: (context, state) {
          final extra = state.extra as Map<String, dynamic>?;
          final email = (extra?['email'] as String?) ?? '';
          return CompleteGoogleSignupScreen(googleEmail: email);
        },
      ),

      // Main navigation shell
      ShellRoute(
        builder: (context, state, child) => MainNavigation(child: child),
        routes: [
          GoRoute(
            path: '/home',
            builder: (context, state) => const HomeScreen(),
          ),
          GoRoute(
            path: '/feed',
            builder: (context, state) => const FeedScreen(),
          ),
          GoRoute(
            path: '/directory',
            builder: (context, state) => const AlumniDirectoryScreen(),
          ),
          GoRoute(
            path: '/jobs',
            builder: (context, state) => const JobsScreen(),
          ),
          GoRoute(
            path: '/chats',
            builder: (context, state) => const ChatListScreen(),
          ),
          GoRoute(
            path: '/profile',
            builder: (context, state) => const ProfileScreen(),
          ),
        ],
      ),

      // Profile routes
      GoRoute(
        path: '/edit-profile',
        builder: (context, state) => const EditProfileScreen(),
      ),
      GoRoute(
        path: '/view-profile/:userId',
        builder: (context, state) {
          final userId = state.pathParameters['userId']!;
          return ViewProfileScreen(userId: userId);
        },
      ),

      // Job routes
      GoRoute(
        path: '/job-details/:jobId',
        builder: (context, state) {
          final jobId = state.pathParameters['jobId']!;
          return JobDetailsScreen(jobId: jobId);
        },
      ),
      GoRoute(
        path: '/post-job',
        builder: (context, state) => const PostJobScreen(),
      ),

      // Event routes
      GoRoute(
        path: '/events',
        builder: (context, state) => const EventsScreen(),
      ),
      GoRoute(
        path: '/event-details/:eventId',
        builder: (context, state) {
          final eventId = state.pathParameters['eventId']!;
          return EventDetailsScreen(eventId: eventId);
        },
      ),
      GoRoute(
        path: '/create-event',
        builder: (context, state) => const CreateEventScreen(),
      ),

      // Chat routes
      GoRoute(
        path: '/chat/:chatId',
        builder: (context, state) {
          final chatId = state.pathParameters['chatId']!;
          final extra = state.extra as Map<String, dynamic>?;
          final otherUserName = (extra?['name'] as String?) ?? 'Chat';
          final otherUserImage = extra?['image'] as String?;
          final lastSeen = extra?['lastSeen'] as String?;
          return ChatScreen(
            chatId: chatId,
            otherUserName: otherUserName,
            otherUserImage: otherUserImage,
            lastSeen: lastSeen,
          );
        },
      ),

      // Mentorship routes
      GoRoute(
        path: '/mentorship',
        builder: (context, state) => const MentorshipScreen(),
      ),
      GoRoute(
        path: '/mentor-search',
        builder: (context, state) => const MentorSearchScreen(),
      ),

      // Almater Directory route
      GoRoute(
        path: '/almater-directory',
        builder: (context, state) => const AlmaterDirectoryScreen(),
      ),

      // Feed routes
      GoRoute(
        path: '/feed',
        builder: (context, state) => const FeedScreen(),
      ),
      GoRoute(
        path: '/create-post',
        builder: (context, state) => const CreatePostScreen(),
      ),

      // Gamification routes
      GoRoute(
        path: '/badges',
        builder: (context, state) => const BadgesScreen(),
      ),
      GoRoute(
        path: '/profile-viewers',
        builder: (context, state) => const ProfileViewersScreen(),
      ),

      // Polls routes
      GoRoute(
        path: '/polls',
        builder: (context, state) => const PollsScreen(),
      ),
      GoRoute(
        path: '/create-poll',
        builder: (context, state) => const CreatePostScreen(postType: 'poll'),
      ),

      // Stories route
      GoRoute(
        path: '/create-story',
        builder: (context, state) => const CreateStoryScreen(),
      ),

      // Referrals route
      GoRoute(
        path: '/referrals',
        builder: (context, state) => const ReferralsScreen(),
      ),

      // News routes
      GoRoute(
        path: '/news',
        builder: (context, state) => const NewsFeedScreen(),
      ),
      GoRoute(
        path: '/article/:articleId',
        builder: (context, state) {
          final articleId = state.pathParameters['articleId']!;
          return ArticleDetailsScreen(articleId: articleId);
        },
      ),

      // Groups routes
      GoRoute(
        path: '/groups',
        builder: (context, state) => const AlumniGroupsScreen(),
      ),
      GoRoute(
        path: '/group/:groupId',
        builder: (context, state) {
          final groupId = state.pathParameters['groupId']!;
          return GroupDetailsScreen(groupId: groupId);
        },
      ),
      GoRoute(
        path: '/create-group',
        builder: (context, state) => const CreateGroupScreen(),
      ),

      // Career routes
      GoRoute(
        path: '/career-tips',
        builder: (context, state) => const CareerTipsScreen(),
      ),
      GoRoute(
        path: '/knowledge',
        builder: (context, state) => const KnowledgeScreen(),
      ),

      // Notifications
      GoRoute(
        path: '/notifications',
        builder: (context, state) => const NotificationsScreen(),
      ),

      // Support & Donations
      GoRoute(
        path: '/support-donations',
        builder: (context, state) => const SupportDonationsScreen(),
      ),

      // Discussions
      GoRoute(
        path: '/discussions',
        builder: (context, state) => const DiscussionsScreen(),
      ),

      // Achievements & Gamification
      GoRoute(
        path: '/achievements',
        builder: (context, state) => const AchievementsScreen(),
      ),

      // Enhanced Career Center
      GoRoute(
        path: '/enhanced-career-center',
        builder: (context, state) => const EnhancedCareerCenter(),
      ),

      // Interactive Content
      GoRoute(
        path: '/interactive-content',
        builder: (context, state) => const InteractiveContentScreen(),
      ),

      // Convocation Team
      GoRoute(
        path: '/convocation-team',
        builder: (context, state) => const EnhancedConvocationTeamScreen(),
      ),

      // Guest Lectures
      GoRoute(
        path: '/guest-lectures',
        builder: (context, state) => const EnhancedGuestLectureScreen(),
      ),

      // Settings routes
      GoRoute(
        path: '/settings',
        builder: (context, state) => const SettingsScreen(),
      ),
      GoRoute(
        path: '/notification-settings',
        builder: (context, state) => const NotificationSettingsScreen(),
      ),
      GoRoute(
        path: '/privacy-settings',
        builder: (context, state) => const PrivacySettingsScreen(),
      ),
    ],
  );
}

// Auth state notifier to refresh router when auth state changes
class _AuthStateNotifier extends ChangeNotifier {
  _AuthStateNotifier() {
    FirebaseAuth.instance.authStateChanges().listen((_) {
      notifyListeners();
    });
  }
}
