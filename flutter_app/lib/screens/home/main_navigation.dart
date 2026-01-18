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
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ChatProvider>().listenToChats();
      context.read<NotificationProvider>().initialize();
    });
  }

  int _getSelectedIndex(BuildContext context) {
    final location = GoRouterState.of(context).uri.path;
    if (location.startsWith('/home')) return 0;
    if (location.startsWith('/directory')) return 1;
    if (location.startsWith('/create-post')) return 2;
    if (location.startsWith('/notifications')) return 3;
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
        context.push('/create-post');
        break;
      case 3:
        context.push('/notifications');
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    final screenWidth = MediaQuery.of(context).size.width;
    final isTablet = screenWidth > 600;

    // Use NavigationRail for tablets, BottomNavigationBar for phones
    if (isTablet) {
      return Scaffold(
        body: Row(
          children: [
            _buildNavigationRail(),
            const VerticalDivider(width: 1),
            Expanded(child: widget.child),
          ],
        ),
      );
    }

    return Scaffold(
      body: widget.child,
      bottomNavigationBar: _buildBottomNav(),
    );
  }

  Widget _buildNavigationRail() {
    return Consumer2<ChatProvider, NotificationProvider>(
      builder: (context, chatProvider, notificationProvider, _) {
        final unreadChats = chatProvider.totalUnreadCount;
        final selectedIndex = _getSelectedIndex(context);

        return NavigationRail(
          selectedIndex: selectedIndex,
          onDestinationSelected: _onItemTapped,
          labelType: NavigationRailLabelType.all,
          backgroundColor: Theme.of(context).scaffoldBackgroundColor,
          selectedIconTheme: const IconThemeData(color: AppColors.primary),
          selectedLabelTextStyle: const TextStyle(
              color: AppColors.primary, fontWeight: FontWeight.w600),
          leading: Padding(
            padding: const EdgeInsets.symmetric(vertical: 16),
            child: FloatingActionButton(
              onPressed: () => context.push('/create-post'),
              mini: true,
              child: const Icon(Icons.add),
            ),
          ),
          destinations: [
            const NavigationRailDestination(
              icon: Icon(Icons.home_outlined),
              selectedIcon: Icon(Icons.home),
              label: Text('Home'),
            ),
            const NavigationRailDestination(
              icon: Icon(Icons.article_outlined),
              selectedIcon: Icon(Icons.article),
              label: Text('Feed'),
            ),
            const NavigationRailDestination(
              icon: Icon(Icons.people_outline),
              selectedIcon: Icon(Icons.people),
              label: Text('Network'),
            ),
            const NavigationRailDestination(
              icon: Icon(Icons.work_outline),
              selectedIcon: Icon(Icons.work),
              label: Text('Jobs'),
            ),
            NavigationRailDestination(
              icon: Badge(
                isLabelVisible: unreadChats > 0,
                label: Text(unreadChats > 9 ? '9+' : '$unreadChats'),
                child: const Icon(Icons.chat_bubble_outline),
              ),
              selectedIcon: Badge(
                isLabelVisible: unreadChats > 0,
                label: Text(unreadChats > 9 ? '9+' : '$unreadChats'),
                child: const Icon(Icons.chat_bubble),
              ),
              label: const Text('Messaging'),
            ),
            const NavigationRailDestination(
              icon: Icon(Icons.person_outline),
              selectedIcon: Icon(Icons.person),
              label: Text('Me'),
            ),
          ],
        );
      },
    );
  }

  Widget _buildBottomNav() {
    return Consumer<NotificationProvider>(
      builder: (context, notificationProvider, _) {
        final unreadNotifications = notificationProvider.unreadCount;

        return NavigationBar(
          selectedIndex: _getSelectedIndex(context),
          onDestinationSelected: _onItemTapped,
          animationDuration: const Duration(milliseconds: 300),
          destinations: [
            const NavigationDestination(
              icon: Icon(Icons.home_outlined),
              selectedIcon: Icon(Icons.home),
              label: 'Home',
            ),
            const NavigationDestination(
              icon: Icon(Icons.people_outline),
              selectedIcon: Icon(Icons.people),
              label: 'Network',
            ),
            const NavigationDestination(
              icon: Icon(Icons.add_box_outlined),
              selectedIcon: Icon(Icons.add_box),
              label: 'Post',
            ),
            NavigationDestination(
              icon: Badge(
                isLabelVisible: unreadNotifications > 0,
                label: Text(
                    unreadNotifications > 9 ? '9+' : '$unreadNotifications'),
                child: const Icon(Icons.notifications_outlined),
              ),
              selectedIcon: Badge(
                isLabelVisible: unreadNotifications > 0,
                label: Text(
                    unreadNotifications > 9 ? '9+' : '$unreadNotifications'),
                child: const Icon(Icons.notifications),
              ),
              label: 'Alerts',
            ),
          ],
        );
      },
    );
  }
}
