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
    if (location.startsWith('/feed')) return 1;
    if (location.startsWith('/directory')) return 2;
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
        context.go('/feed');
        break;
      case 2:
        context.go('/directory');
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
          backgroundColor:
              Theme.of(context).navigationRailTheme.backgroundColor,
          selectedIconTheme:
              Theme.of(context).navigationRailTheme.selectedIconTheme,
          selectedLabelTextStyle:
              Theme.of(context).navigationRailTheme.selectedLabelTextStyle,
          unselectedIconTheme:
              Theme.of(context).navigationRailTheme.unselectedIconTheme,
          unselectedLabelTextStyle:
              Theme.of(context).navigationRailTheme.unselectedLabelTextStyle,
          destinations: [
            const NavigationRailDestination(
              icon: Icon(Icons.home_outlined),
              selectedIcon: Icon(Icons.home),
              label: Text('Home'),
            ),
            const NavigationRailDestination(
              icon: Icon(Icons.dynamic_feed_outlined),
              selectedIcon: Icon(Icons.dynamic_feed),
              label: Text('Feed'),
            ),
            const NavigationRailDestination(
              icon: Icon(Icons.people_outline),
              selectedIcon: Icon(Icons.people),
              label: Text('Network'),
            ),
            NavigationRailDestination(
              icon: unreadChats > 0
                  ? Badge(
                      label: Text('$unreadChats'),
                      child: const Icon(Icons.chat_bubble_outline),
                    )
                  : const Icon(Icons.chat_bubble_outline),
              selectedIcon: const Icon(Icons.chat_bubble),
              label: const Text('Messages'),
            ),
            const NavigationRailDestination(
              icon: Icon(Icons.person_outline),
              selectedIcon: Icon(Icons.person),
              label: Text('Profile'),
            ),
          ],
        );
      },
    );
  }

  Widget _buildBottomNav() {
    return Consumer<ChatProvider>(
      builder: (context, chatProvider, _) {
        final unreadChats = chatProvider.totalUnreadCount;

        return Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // MUST colored lines
            Container(
              height: 4,
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    AppColors.accent,      // Blue
                    AppColors.accent,      // Blue
                    AppColors.secondary,   // Orange
                    AppColors.secondary,   // Orange
                  ],
                  stops: [0.0, 0.5, 0.5, 1.0],
                ),
              ),
            ),
            NavigationBar(
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
                  icon: Icon(Icons.dynamic_feed_outlined),
                  selectedIcon: Icon(Icons.dynamic_feed),
                  label: 'Feed',
                ),
                const NavigationDestination(
                  icon: Icon(Icons.people_outline),
                  selectedIcon: Icon(Icons.people),
                  label: 'Network',
                ),
                NavigationDestination(
                  icon: unreadChats > 0
                      ? Badge(
                          label: Text('$unreadChats'),
                          child: const Icon(Icons.chat_bubble_outline),
                        )
                      : const Icon(Icons.chat_bubble_outline),
                  selectedIcon: const Icon(Icons.chat_bubble),
                  label: 'Messages',
                ),
                const NavigationDestination(
                  icon: Icon(Icons.person_outline),
                  selectedIcon: Icon(Icons.person),
                  label: 'Profile',
                ),
              ],
            ),
          ],
        );
      },
    );
  }
}
