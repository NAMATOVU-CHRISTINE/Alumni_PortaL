import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_auth/firebase_auth.dart';
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

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await dotenv.load(fileName: ".env");
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);

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
          title: 'MUST Alumni Portal',
          debugShowCheckedModeBanner: false,
          theme: AppTheme.lightTheme,
          darkTheme: AppTheme.darkTheme,
          themeMode: themeProvider.themeMode,
          routerConfig: AppRouter.router,
        ),
      ),
    );
  }
}
