import 'package:go_router/go_router.dart';
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

class AppRouter {
  static final GoRouter router = GoRouter(
    initialLocation: '/',
    routes: [
      GoRoute(path: '/', builder: (context, state) => const SplashScreen()),

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
          final email = state.extra as String? ?? '';
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
          final otherUserName = state.extra as String? ?? 'Chat';
          return ChatScreen(chatId: chatId, otherUserName: otherUserName);
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
