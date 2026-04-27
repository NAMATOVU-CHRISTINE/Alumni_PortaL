import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:provider/provider.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:alumni_portal/firebase_options.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/config/routes.dart';
import 'package:alumni_portal/providers/auth_provider.dart' as app_auth;
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/providers/chat_provider.dart';
import 'package:alumni_portal/providers/notification_provider.dart';
import 'package:alumni_portal/providers/theme_provider.dart';
import 'package:alumni_portal/providers/post_provider.dart';
import 'package:alumni_portal/providers/gamification_provider.dart';
import 'package:alumni_portal/providers/poll_provider.dart';
import 'package:alumni_portal/providers/story_provider.dart';
import 'package:alumni_portal/providers/endorsement_provider.dart';
import 'package:alumni_portal/providers/referral_provider.dart';
import 'package:alumni_portal/providers/convocation_provider.dart';
import 'package:alumni_portal/providers/guest_lecture_provider.dart';
import 'package:alumni_portal/services/firebase_messaging_service.dart';
import 'package:alumni_portal/services/navigation_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  try {
    await dotenv.load(fileName: ".env");
    print('✅ .env file loaded successfully');
    print('✅ Pandora API Key: ${dotenv.env['PANDORA_API_KEY']?.isEmpty == false ? "LOADED (${dotenv.env['PANDORA_API_KEY']!.length} chars)" : "MISSING"}');
  } catch (e) {
    print('❌ Error loading .env file: $e');
  }
  
  try {
    await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);
    print('✅ Firebase initialized successfully');
  } catch (e) {
    print('❌ Error initializing Firebase: $e');
    // Continue anyway to show error screen
  }

  // Register background message handler BEFORE initializing Firebase Messaging
  // This ensures notifications work even when app is closed/terminated
  FirebaseMessaging.onBackgroundMessage(firebaseMessagingBackgroundHandler);
  print('✅ Background message handler registered');

  // Initialize Firebase Messaging
  try {
    await FirebaseMessagingService.initialize();
    print('✅ Firebase Messaging initialized');
  } catch (e) {
    print('❌ Error initializing Firebase Messaging: $e');
  }

  // Note: Firebase Auth persistence is automatic on mobile platforms
  // setPersistence() is only for web

  runApp(const AlumniPortalApp());
}

class AlumniPortalApp extends StatelessWidget {
  const AlumniPortalApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => app_auth.AuthProvider()),
        ChangeNotifierProvider(create: (_) => UserProvider()),
        ChangeNotifierProvider(create: (_) => ChatProvider()),
        ChangeNotifierProvider(create: (_) => NotificationProvider()),
        ChangeNotifierProvider(create: (_) => ThemeProvider()),
        ChangeNotifierProvider(create: (_) => PostProvider()),
        ChangeNotifierProvider(create: (_) => GamificationProvider()),
        ChangeNotifierProvider(create: (_) => PollProvider()),
        ChangeNotifierProvider(create: (_) => StoryProvider()),
        ChangeNotifierProvider(create: (_) => EndorsementProvider()),
        ChangeNotifierProvider(create: (_) => ReferralProvider()),
        ChangeNotifierProvider(create: (_) => ConvocationProvider()),
        ChangeNotifierProvider(create: (_) => GuestLectureProvider()),
      ],
      child: Consumer<ThemeProvider>(
        builder: (context, themeProvider, _) => MaterialApp.router(
          title: 'The Convocation',
          debugShowCheckedModeBanner: false,
          theme: AppTheme.lightTheme.copyWith(
            pageTransitionsTheme: const PageTransitionsTheme(
              builders: {
                TargetPlatform.android: FadeUpwardsPageTransitionsBuilder(),
                TargetPlatform.iOS: CupertinoPageTransitionsBuilder(),
              },
            ),
          ),
          darkTheme: AppTheme.darkTheme,
          themeMode: themeProvider.themeMode,
          routerConfig: AppRouter.router,
          builder: (context, child) {
            return Container(
              color: Colors.white,
              child: child,
            );
          },
        ),
      ),
    );
  }
}
