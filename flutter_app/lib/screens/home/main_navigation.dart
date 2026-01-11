import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:alumni_portal/providers/chat_provider.dart';
import 'package:alumni_portal/providers/notification_provider.dart';
import 'package:alumni_portal/config/theme.dart';

class MainNavigation extends StatefulWidget {
  final Widget child;

  const MainNavigation({super.key, required this.child});

  @override
  State<MainNavigation> createState() => _MainNavigationState();
}

class _MainNavigationState extends State<MainNavigation> {
  @override
  void initState() {
    super.initState();
    // Initialize providers
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ChatProvider>().listenToChats();
      context.read<NotificationProvider>().initialize();
    });
  }

  int _getSelectedIndex(BuildContext context) {
    final location = GoRouterState.of(context).uri.path;
    if (location.startsWith('/home')) return 0;
    if (location.startsWith('/directory')) return 1;
    if (location.startsWith('/jobs')) return 2;
    if (location.startsWith('/chats')) return 3;
    if (location.startsWith('/profile')) return 4;
    return 0;
  }

  void _onItemTapped(int index) {
    switch (index) {
      case 0:
        context.go('/home');
        break;
      case 1:
        context.go('/directory');
        break;
      case 2:
        context.go('/jobs');
        break;
      case 3:
        context.go('/chats');
        break;
      case 4:
        context.go('/profile');
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: widget.child,
      bottomNavigationBar: Consumer2<ChatProvider, NotificationProvider>(
        builder: (context, chatProvider, notificationProvider, _) {
          final unreadChats = chatProvider.totalUnreadCount;

          return BottomNavigationBar(
            currentIndex: _getSelectedIndex(context),
            onTap: _onItemTapped,
            type: BottomNavigationBarType.fixed,
            selectedItemColor: AppColors.primary,
            unselectedItemColor: AppColors.textSecondary,
            items: [
              const BottomNavigationBarItem(
                icon: Icon(Icons.home_outlined),
                activeIcon: Icon(Icons.home),
                label: 'Home',
              ),
              const BottomNavigationBarItem(
                icon: Icon(Icons.people_outline),
                activeIcon: Icon(Icons.people),
                label: 'Directory',
              ),
              const BottomNavigationBarItem(
                icon: Icon(Icons.work_outline),
                activeIcon: Icon(Icons.work),
                label: 'Jobs',
              ),
              BottomNavigationBarItem(
                icon: Badge(
                  isLabelVisible: unreadChats > 0,
                  label: Text(unreadChats > 9 ? '9+' : '$unreadChats'),
                  child: const Icon(Icons.chat_bubble_outline),
                ),
                activeIcon: Badge(
                  isLabelVisible: unreadChats > 0,
                  label: Text(unreadChats > 9 ? '9+' : '$unreadChats'),
                  child: const Icon(Icons.chat_bubble),
                ),
                label: 'Chats',
              ),
              const BottomNavigationBarItem(
                icon: Icon(Icons.person_outline),
                activeIcon: Icon(Icons.person),
                label: 'Profile',
              ),
            ],
          );
        },
      ),
    );
  }
}
