import 'dart:async';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/providers/auth_provider.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/providers/notification_provider.dart';
import 'package:alumni_portal/providers/post_provider.dart';
import 'package:alumni_portal/providers/gamification_provider.dart';
import 'package:alumni_portal/providers/poll_provider.dart';
import 'package:alumni_portal/providers/story_provider.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/screens/home/connection_suggestions_widget.dart';
import 'package:alumni_portal/screens/home/success_stories_widget.dart';
import 'package:alumni_portal/screens/home/active_polls_widget.dart';
import 'package:alumni_portal/screens/home/alumni_spotlight_widget.dart';
import 'package:alumni_portal/screens/stories/stories_widget.dart';
import 'package:alumni_portal/screens/profile/profile_completion_widget.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _currentTipIndex = 0;
  Timer? _tipTimer;

  final List<String> _motivationalTips = [
    "Keep connecting, keep growing!",
    "Your network is your net worth",
    "Every connection is a new opportunity �",
    "Success is a journey, not a destination",
    "Learn from those who've walked your path",
    "Great things never come from comfort zones",
    "The alumni network is your secret weapon",
    "Today's networking is tomorrow's opportunity",
    "Small steps lead to big dreams",
    "Stay curious, stay connected 🔗",
  ];

  @override
  void initState() {
    super.initState();
    _loadData();
    _startTipRotation();
  }

  void _loadData() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<UserProvider>().loadCurrentUser();
      context.read<PostProvider>().loadPosts();
      context.read<GamificationProvider>().loadAchievements();
      context.read<PollProvider>().loadPolls();
      context.read<StoryProvider>().loadStories();
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
    final screenWidth = MediaQuery.of(context).size.width;
    final isTablet = screenWidth > 600;

    return Scaffold(
      appBar: _buildAppBar(),
      drawer: _buildDrawer(),
      body: RefreshIndicator(
        onRefresh: () async {
          await context.read<UserProvider>().loadCurrentUser();
          await context.read<PostProvider>().loadPosts();
        },
        child: CustomScrollView(
          slivers: [
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _buildWelcomeSection(),
                    const SizedBox(height: 16),
                  ],
                ),
              ),
            ),
            // Profile completion widget
            const SliverToBoxAdapter(child: ProfileCompletionWidget()),
            // Stories
            const SliverToBoxAdapter(child: StoriesWidget()),
            const SliverToBoxAdapter(child: SizedBox(height: 16)),
            // Motivational tip
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: _buildMotivationalTip(),
              ),
            ),
            const SliverToBoxAdapter(child: SizedBox(height: 24)),
            // Quick actions - responsive grid
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: _buildQuickActionsGrid(isTablet),
              ),
            ),
            const SliverToBoxAdapter(child: SizedBox(height: 24)),
            // Connection suggestions
            const SliverToBoxAdapter(child: ConnectionSuggestionsWidget()),
            const SliverToBoxAdapter(child: SizedBox(height: 24)),
            // Success stories
            const SliverToBoxAdapter(child: SuccessStoriesWidget()),
            const SliverToBoxAdapter(child: SizedBox(height: 24)),
            // Alumni Spotlight
            const SliverToBoxAdapter(child: AlumniSpotlightWidget()),
            const SliverToBoxAdapter(child: SizedBox(height: 24)),
            // Active polls
            const SliverToBoxAdapter(child: ActivePollsWidget()),
            const SliverToBoxAdapter(child: SizedBox(height: 24)),
            // Recent posts preview
            SliverToBoxAdapter(child: _buildRecentPostsSection()),
            const SliverToBoxAdapter(child: SizedBox(height: 24)),
            // Feature cards
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: _buildFeatureCards(isTablet),
              ),
            ),
            const SliverToBoxAdapter(child: SizedBox(height: 32)),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/create-post'),
        icon: const Icon(Icons.edit),
        label: const Text('Post'),
      ),
    );
  }

  PreferredSizeWidget _buildAppBar() {
    return AppBar(
      title: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            padding: const EdgeInsets.all(6),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(8),
            ),
            child: const Icon(Icons.school, color: AppColors.primary, size: 20),
          ),
          const SizedBox(width: 8),
          const Flexible(
            child: Text(
              'Alumni Portal',
              overflow: TextOverflow.ellipsis,
            ),
          ),
        ],
      ),
      actions: [
        IconButton(
          icon: const Icon(Icons.search),
          onPressed: () => context.go('/directory'),
          tooltip: 'Search',
        ),
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
          child: Column(
            children: [
              UserAccountsDrawerHeader(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [AppColors.primary, AppColors.primaryDark],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                ),
                currentAccountPicture: GestureDetector(
                  onTap: () {
                    Navigator.pop(context);
                    context.go('/profile');
                  },
                  child: CircleAvatar(
                    backgroundColor: Colors.white,
                    backgroundImage: user?.profileImageUrl != null
                        ? CachedNetworkImageProvider(user!.profileImageUrl!)
                        : null,
                    child: user?.profileImageUrl == null
                        ? Text(
                            user?.fullName?.substring(0, 1).toUpperCase() ??
                                'A',
                            style: const TextStyle(
                                fontSize: 32, color: AppColors.primary),
                          )
                        : null,
                  ),
                ),
                accountName: Text(
                  user?.fullName ?? 'Alumni User',
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
                accountEmail: Text(user?.email ?? ''),
                otherAccountsPictures: [
                  IconButton(
                    icon: const Icon(Icons.edit, color: Colors.white),
                    onPressed: () {
                      Navigator.pop(context);
                      context.push('/edit-profile');
                    },
                  ),
                ],
              ),
              Expanded(
                child: ListView(
                  padding: EdgeInsets.zero,
                  children: [
                    _buildDrawerSection('Main', [
                      _buildDrawerItem(
                          Icons.home, 'Home', () => context.go('/home'), true),
                      _buildDrawerItem(Icons.dynamic_feed, 'Feed',
                          () => context.push('/feed'), false),
                      _buildDrawerItem(Icons.person, 'Profile',
                          () => context.go('/profile'), false),
                    ]),
                    _buildDrawerSection('Network', [
                      _buildDrawerItem(Icons.people, 'Alumni Directory',
                          () => context.go('/directory'), false),
                      _buildDrawerItem(Icons.school, 'Almater Directory',
                          () => context.push('/almater-directory'), false),
                      _buildDrawerItem(Icons.handshake, 'Mentorship',
                          () => context.push('/mentorship'), false),
                      _buildDrawerItem(Icons.group, 'Groups',
                          () => context.push('/groups'), false),
                    ]),
                    _buildDrawerSection('Career', [
                      _buildDrawerItem(
                          Icons.work, 'Jobs', () => context.go('/jobs'), false),
                      _buildDrawerItem(Icons.share, 'Referrals',
                          () => context.push('/referrals'), false),
                      _buildDrawerItem(Icons.lightbulb, 'Career Tips',
                          () => context.push('/career-tips'), false),
                      _buildDrawerItem(Icons.book, 'Knowledge Hub',
                          () => context.push('/knowledge'), false),
                    ]),
                    _buildDrawerSection('Discover', [
                      _buildDrawerItem(Icons.event, 'Events',
                          () => context.push('/events'), false),
                      _buildDrawerItem(Icons.article, 'News',
                          () => context.push('/news'), false),
                      _buildDrawerItem(Icons.poll, 'Polls',
                          () => context.push('/polls'), false),
                    ]),
                    _buildDrawerSection('Achievements', [
                      _buildDrawerItem(Icons.emoji_events, 'Badges',
                          () => context.push('/badges'), false),
                      _buildDrawerItem(Icons.visibility, 'Who Viewed Me',
                          () => context.push('/profile-viewers'), false),
                    ]),
                    const Divider(),
                    _buildDrawerItem(Icons.settings, 'Settings',
                        () => context.push('/settings'), false),
                    _buildDrawerItem(
                        Icons.logout, 'Logout', () => _handleLogout(), false,
                        isDestructive: true),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildDrawerSection(String title, List<Widget> items) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
          child: Text(
            title,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.bold,
              color: Colors.grey[600],
              letterSpacing: 1,
            ),
          ),
        ),
        ...items,
      ],
    );
  }

  Widget _buildDrawerItem(
      IconData icon, String title, VoidCallback onTap, bool isSelected,
      {bool isDestructive = false}) {
    return ListTile(
      leading: Icon(
        icon,
        color: isDestructive
            ? AppColors.error
            : (isSelected ? AppColors.primary : null),
      ),
      title: Text(
        title,
        style: TextStyle(
          color: isDestructive ? AppColors.error : null,
          fontWeight: isSelected ? FontWeight.bold : null,
        ),
      ),
      selected: isSelected,
      selectedTileColor: AppColors.primary.withValues(alpha: 0.1),
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
        final greeting = _getGreeting();
        return Row(
          children: [
            GestureDetector(
              onTap: () => context.go('/profile'),
              child: CircleAvatar(
                radius: 28,
                backgroundColor: AppColors.primary.withValues(alpha: 0.1),
                backgroundImage: user?.profileImageUrl != null
                    ? CachedNetworkImageProvider(user!.profileImageUrl!)
                    : null,
                child: user?.profileImageUrl == null
                    ? Text(
                        user?.fullName?.substring(0, 1).toUpperCase() ?? 'A',
                        style: const TextStyle(
                          fontSize: 22,
                          color: AppColors.primary,
                          fontWeight: FontWeight.bold,
                        ),
                      )
                    : null,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    greeting,
                    style: TextStyle(color: Colors.grey[600], fontSize: 13),
                  ),
                  Text(
                    user?.fullName?.split(' ').first ?? 'Alumni',
                    style: const TextStyle(
                        fontSize: 20, fontWeight: FontWeight.bold),
                  ),
                ],
              ),
            ),
            // Quick chat button
            IconButton(
              onPressed: () => context.go('/chats'),
              icon: const Icon(Icons.chat_bubble_outline),
              tooltip: 'Messages',
            ),
          ],
        );
      },
    );
  }

  String _getGreeting() {
    final hour = DateTime.now().hour;
    if (hour < 12) return 'Good morning,';
    if (hour < 17) return 'Good afternoon,';
    return 'Good evening,';
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
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: AppColors.primary.withValues(alpha: 0.3),
              blurRadius: 8,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: Colors.white24,
                borderRadius: BorderRadius.circular(8),
              ),
              child: const Icon(Icons.lightbulb, color: Colors.white, size: 24),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                _motivationalTips[_currentTipIndex],
                style: const TextStyle(color: Colors.white, fontSize: 15),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildQuickActionsGrid(bool isTablet) {
    final actions = [
      {
        'icon': Icons.edit,
        'label': 'Post',
        'onTap': () => context.push('/create-post')
      },
      {
        'icon': Icons.school,
        'label': 'Mentors',
        'onTap': () => context.push('/mentor-search')
      },
      {'icon': Icons.work, 'label': 'Jobs', 'onTap': () => context.go('/jobs')},
      {
        'icon': Icons.event,
        'label': 'Events',
        'onTap': () => context.push('/events')
      },
      {
        'icon': Icons.article,
        'label': 'News',
        'onTap': () => context.push('/news')
      },
      {
        'icon': Icons.group,
        'label': 'Groups',
        'onTap': () => context.push('/groups')
      },
    ];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Quick Actions',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: isTablet ? 6 : 3,
            mainAxisSpacing: 12,
            crossAxisSpacing: 12,
            childAspectRatio: 1,
          ),
          itemCount: actions.length,
          itemBuilder: (context, index) {
            final action = actions[index];
            return _buildQuickActionItem(
              action['icon'] as IconData,
              action['label'] as String,
              action['onTap'] as VoidCallback,
            );
          },
        ),
      ],
    );
  }

  Widget _buildQuickActionItem(
      IconData icon, String label, VoidCallback onTap) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16),
      child: Container(
        decoration: BoxDecoration(
          color: AppColors.primary.withValues(alpha: 0.08),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: AppColors.primary.withValues(alpha: 0.2)),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, color: AppColors.primary, size: 28),
            const SizedBox(height: 6),
            Text(
              label,
              style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w500),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildRecentPostsSection() {
    return Consumer<PostProvider>(
      builder: (context, provider, _) {
        final posts = provider.posts.take(3).toList();
        if (posts.isEmpty) return const SizedBox.shrink();

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Recent Posts',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  TextButton(
                    onPressed: () => context.push('/feed'),
                    child: const Text('See all'),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 8),
            ...posts.map((post) => _buildPostPreview(post)),
          ],
        );
      },
    );
  }

  Widget _buildPostPreview(dynamic post) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: ListTile(
        leading: CircleAvatar(
          backgroundImage: post.authorImageUrl != null
              ? CachedNetworkImageProvider(post.authorImageUrl!)
              : null,
          child: post.authorImageUrl == null
              ? Text(post.authorName.substring(0, 1).toUpperCase())
              : null,
        ),
        title: Text(
          post.authorName,
          style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
        ),
        subtitle: Text(
          post.content,
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(fontSize: 13),
        ),
        trailing: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.favorite, size: 16, color: Colors.grey[400]),
            Text('${post.totalReactions}',
                style: TextStyle(fontSize: 11, color: Colors.grey[600])),
          ],
        ),
        onTap: () => context.push('/feed'),
      ),
    );
  }

  Widget _buildFeatureCards(bool isTablet) {
    final features = [
      {
        'title': 'Feed',
        'subtitle': 'See what alumni are sharing',
        'icon': Icons.dynamic_feed,
        'route': '/feed'
      },
      {
        'title': 'Alumni Directory',
        'subtitle': 'Connect with fellow alumni',
        'icon': Icons.people,
        'route': '/directory'
      },
      {
        'title': 'Career Tips',
        'subtitle': 'Grow your career',
        'icon': Icons.lightbulb,
        'route': '/career-tips'
      },
      {
        'title': 'Alumni Groups',
        'subtitle': 'Join interest groups',
        'icon': Icons.group,
        'route': '/groups'
      },
    ];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Explore',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        if (isTablet)
          GridView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 2,
              mainAxisSpacing: 12,
              crossAxisSpacing: 12,
              childAspectRatio: 2.5,
            ),
            itemCount: features.length,
            itemBuilder: (context, index) {
              final f = features[index];
              return _buildFeatureCard(
                f['title'] as String,
                f['subtitle'] as String,
                f['icon'] as IconData,
                () => context.push(f['route'] as String),
              );
            },
          )
        else
          ...features.map((f) => Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: _buildFeatureCard(
                  f['title'] as String,
                  f['subtitle'] as String,
                  f['icon'] as IconData,
                  () => context.push(f['route'] as String),
                ),
              )),
      ],
    );
  }

  Widget _buildFeatureCard(
      String title, String subtitle, IconData icon, VoidCallback onTap) {
    return Card(
      child: ListTile(
        leading: Container(
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: AppColors.primary.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(12),
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
      builder: (dialogContext) => AlertDialog(
        title: const Text('Logout'),
        content: const Text('Are you sure you want to logout?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.pop(dialogContext);
              await context.read<AuthProvider>().signOut();
              if (mounted) context.go('/login');
            },
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.error),
            child: const Text('Logout'),
          ),
        ],
      ),
    );
  }
}
