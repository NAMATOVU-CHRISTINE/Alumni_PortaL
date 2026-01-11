import 'dart:async';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/providers/auth_provider.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/providers/notification_provider.dart';
import 'package:alumni_portal/config/theme.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _currentTipIndex = 0;
  Timer? _tipTimer;

  final List<String> _motivationalTips = [
    "Keep connecting, keep growing! 🌱",
    "Your network is your net worth 💎",
    "Every connection is a new opportunity 🚀",
    "Success is a journey, not a destination 🎯",
    "Learn from those who've walked your path 👣",
    "Great things never come from comfort zones 💪",
    "The alumni network is your secret weapon 🔑",
    "Today's networking is tomorrow's opportunity 🌟",
    "Small steps lead to big dreams ✨",
    "Stay curious, stay connected 🔗",
  ];

  @override
  void initState() {
    super.initState();
    _loadUserData();
    _startTipRotation();
  }

  void _loadUserData() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<UserProvider>().loadCurrentUser();
    });
  }

  void _startTipRotation() {
    _tipTimer = Timer.periodic(const Duration(seconds: 6), (timer) {
      if (mounted) {
        setState(() {
          _currentTipIndex = (_currentTipIndex + 1) % _motivationalTips.length;
        });
      }
    });
  }

  @override
  void dispose() {
    _tipTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _buildAppBar(),
      drawer: _buildDrawer(),
      body: RefreshIndicator(
        onRefresh: () async {
          await context.read<UserProvider>().loadCurrentUser();
        },
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildWelcomeSection(),
              const SizedBox(height: 24),
              _buildMotivationalTip(),
              const SizedBox(height: 24),
              _buildQuickStats(),
              const SizedBox(height: 24),
              _buildQuickActions(),
              const SizedBox(height: 24),
              _buildFeatureCards(),
            ],
          ),
        ),
      ),
    );
  }

  PreferredSizeWidget _buildAppBar() {
    return AppBar(
      title: const Text('Alumni Portal'),
      actions: [
        Consumer<NotificationProvider>(
          builder: (context, provider, _) => IconButton(
            icon: Badge(
              isLabelVisible: provider.unreadCount > 0,
              label: Text('${provider.unreadCount}'),
              child: const Icon(Icons.notifications_outlined),
            ),
            onPressed: () => context.push('/notifications'),
          ),
        ),
        IconButton(
          icon: const Icon(Icons.settings_outlined),
          onPressed: () => context.push('/settings'),
        ),
      ],
    );
  }

  Widget _buildDrawer() {
    return Consumer<UserProvider>(
      builder: (context, userProvider, _) {
        final user = userProvider.currentUser;
        return Drawer(
          child: ListView(
            padding: EdgeInsets.zero,
            children: [
              UserAccountsDrawerHeader(
                decoration: const BoxDecoration(color: AppColors.primary),
                currentAccountPicture: CircleAvatar(
                  backgroundColor: Colors.white,
                  backgroundImage: user?.profileImageUrl != null
                      ? CachedNetworkImageProvider(user!.profileImageUrl!)
                      : null,
                  child: user?.profileImageUrl == null
                      ? Text(
                          user?.fullName?.substring(0, 1).toUpperCase() ?? 'A',
                          style: const TextStyle(
                            fontSize: 32,
                            color: AppColors.primary,
                          ),
                        )
                      : null,
                ),
                accountName: Text(user?.fullName ?? 'Alumni User'),
                accountEmail: Text(user?.email ?? ''),
              ),
              _buildDrawerItem(Icons.home, 'Home', () => context.go('/home')),
              _buildDrawerItem(
                Icons.person,
                'Profile',
                () => context.go('/profile'),
              ),
              _buildDrawerItem(
                Icons.people,
                'Alumni Directory',
                () => context.go('/directory'),
              ),
              _buildDrawerItem(
                Icons.school,
                'Almater Directory',
                () => context.push('/almater-directory'),
              ),
              _buildDrawerItem(
                Icons.handshake,
                'Mentorship',
                () => context.push('/mentorship'),
              ),
              _buildDrawerItem(Icons.work, 'Jobs', () => context.go('/jobs')),
              _buildDrawerItem(
                Icons.lightbulb,
                'Career Tips',
                () => context.push('/career-tips'),
              ),
              _buildDrawerItem(
                Icons.book,
                'Knowledge Hub',
                () => context.push('/knowledge'),
              ),
              _buildDrawerItem(
                Icons.event,
                'Events',
                () => context.push('/events'),
              ),
              _buildDrawerItem(
                Icons.article,
                'News',
                () => context.push('/news'),
              ),
              _buildDrawerItem(
                Icons.group,
                'Groups',
                () => context.push('/groups'),
              ),
              const Divider(),
              _buildDrawerItem(
                Icons.settings,
                'Settings',
                () => context.push('/settings'),
              ),
              _buildDrawerItem(Icons.logout, 'Logout', () => _handleLogout()),
            ],
          ),
        );
      },
    );
  }

  Widget _buildDrawerItem(IconData icon, String title, VoidCallback onTap) {
    return ListTile(
      leading: Icon(icon),
      title: Text(title),
      onTap: () {
        Navigator.pop(context);
        onTap();
      },
    );
  }

  Widget _buildWelcomeSection() {
    return Consumer<UserProvider>(
      builder: (context, provider, _) {
        final user = provider.currentUser;
        return Row(
          children: [
            CircleAvatar(
              radius: 32,
              backgroundColor: AppColors.primary.withOpacity(0.1),
              backgroundImage: user?.profileImageUrl != null
                  ? CachedNetworkImageProvider(user!.profileImageUrl!)
                  : null,
              child: user?.profileImageUrl == null
                  ? Text(
                      user?.fullName?.substring(0, 1).toUpperCase() ?? 'A',
                      style: const TextStyle(
                        fontSize: 24,
                        color: AppColors.primary,
                        fontWeight: FontWeight.bold,
                      ),
                    )
                  : null,
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Welcome back,',
                    style: TextStyle(
                      color: AppColors.textSecondary,
                      fontSize: 14,
                    ),
                  ),
                  Text(
                    user?.fullName ?? 'Alumni',
                    style: const TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildMotivationalTip() {
    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 500),
      child: Container(
        key: ValueKey(_currentTipIndex),
        width: double.infinity,
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            colors: [AppColors.primary, AppColors.primaryDark],
          ),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          children: [
            const Icon(Icons.lightbulb, color: Colors.white, size: 28),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                _motivationalTips[_currentTipIndex],
                style: const TextStyle(color: Colors.white, fontSize: 16),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildQuickStats() {
    return Row(
      children: [
        Expanded(child: _buildStatCard('Connections', '0', Icons.people)),
        const SizedBox(width: 12),
        Expanded(child: _buildStatCard('Profile Views', '0', Icons.visibility)),
      ],
    );
  }

  Widget _buildStatCard(String label, String value, IconData icon) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Icon(icon, color: AppColors.primary, size: 28),
            const SizedBox(height: 8),
            Text(
              value,
              style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            Text(
              label,
              style: const TextStyle(
                color: AppColors.textSecondary,
                fontSize: 12,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildQuickActions() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Quick Actions',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(
              child: _buildActionButton(
                'Find Mentor',
                Icons.school,
                () => context.push('/mentor-search'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: _buildActionButton(
                'Browse Jobs',
                Icons.work,
                () => context.go('/jobs'),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(
              child: _buildActionButton(
                'Events',
                Icons.event,
                () => context.push('/events'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: _buildActionButton(
                'News',
                Icons.article,
                () => context.push('/news'),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildActionButton(String label, IconData icon, VoidCallback onTap) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: AppColors.primary.withOpacity(0.1),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, color: AppColors.primary),
            const SizedBox(width: 8),
            Text(label, style: const TextStyle(fontWeight: FontWeight.w500)),
          ],
        ),
      ),
    );
  }

  Widget _buildFeatureCards() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Explore',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        _buildFeatureCard(
          'Alumni Directory',
          'Connect with fellow alumni from your university',
          Icons.people,
          () => context.go('/directory'),
        ),
        const SizedBox(height: 12),
        _buildFeatureCard(
          'Career Tips',
          'Get insights and advice for your career growth',
          Icons.lightbulb,
          () => context.push('/career-tips'),
        ),
        const SizedBox(height: 12),
        _buildFeatureCard(
          'Alumni Groups',
          'Join groups based on your interests and graduation year',
          Icons.group,
          () => context.push('/groups'),
        ),
      ],
    );
  }

  Widget _buildFeatureCard(
    String title,
    String subtitle,
    IconData icon,
    VoidCallback onTap,
  ) {
    return Card(
      child: ListTile(
        leading: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: AppColors.primary.withOpacity(0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(icon, color: AppColors.primary),
        ),
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Text(subtitle, style: const TextStyle(fontSize: 12)),
        trailing: const Icon(Icons.chevron_right),
        onTap: onTap,
      ),
    );
  }

  void _handleLogout() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Logout'),
        content: const Text('Are you sure you want to logout?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.pop(context);
              await context.read<AuthProvider>().signOut();
              if (mounted) context.go('/login');
            },
            child: const Text('Logout'),
          ),
        ],
      ),
    );
  }
}
