import 'dart:async';
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/providers/auth_provider.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/providers/chat_provider.dart';
import 'package:alumni_portal/providers/notification_provider.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/screens/stories/stories_widget.dart';
import 'package:alumni_portal/services/firebase_messaging_service.dart';
import 'package:alumni_portal/screens/home/notable_alumni_carousel.dart';
class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});
  @override
  State<HomeScreen> createState() => _HomeScreenState();
}
class _HomeScreenState extends State<HomeScreen> with TickerProviderStateMixin {
  int _currentTipIndex = 0;
  Timer? _tipTimer;
  Timer? _activityTimer;
  List<AnimationController>? _cardControllers;
  List<Animation<double>>? _cardAnimations;
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
    _startActivityTracking();
    _initializeAnimations();
    _initializeNotifications();
  }
  void _initializeNotifications() {
    // Initialize chat provider to listen for messages
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ChatProvider>().listenToChats();
      context.read<NotificationProvider>().initialize();
      // Check for pending notification navigation
      _checkPendingNotification();
    });
  }
  void _checkPendingNotification() {
    final pendingChatId = FirebaseMessagingService.getPendingChatId();
    if (pendingChatId != null && mounted) {
      print('Navigating to pending chat: $pendingChatId');
      // Navigate to the chat screen
      context.push('/chat/$pendingChatId');
    }
  }
  void _initializeAnimations() {
    _cardControllers = List.generate(
      9,
      (index) => AnimationController(
        duration: const Duration(milliseconds: 400),
        vsync: this,
      ),
    );
    _cardAnimations = _cardControllers!.map((controller) {
      return Tween<double>(begin: 0.0, end: 1.0).animate(
        CurvedAnimation(parent: controller, curve: Curves.easeOutBack),
      );
    }).toList();
    // Start staggered animations
    for (int i = 0; i < _cardControllers!.length; i++) {
      Future.delayed(Duration(milliseconds: i * 60), () {
        if (mounted) _cardControllers![i].forward();
      });
    }
  }
  void _loadData() {
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
  void _startActivityTracking() {
    // Update user activity every 2 minutes
    _activityTimer = Timer.periodic(const Duration(minutes: 2), (timer) {
      if (mounted) {
        context.read<UserProvider>().updateUserActivity();
      }
    });
    // Update immediately on start
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<UserProvider>().updateUserActivity();
    });
  }
  @override
  void dispose() {
    _tipTimer?.cancel();
    _activityTimer?.cancel();
    if (_cardControllers != null) {
      for (var controller in _cardControllers!) {
        controller.dispose();
      }
    }
    // Set user offline when leaving app
    context.read<UserProvider>().setUserOffline();
    super.dispose();
  }
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        elevation: 0,
        backgroundColor: Colors.white,
        surfaceTintColor: Colors.white,
        leading: Builder(
          builder: (context) => IconButton(
            icon: Icon(Icons.menu, color: Colors.grey[800]),
            onPressed: () => Scaffold.of(context).openDrawer(),
            tooltip: 'Menu',
          ),
        ),
        title: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(10),
              child: Image.asset(
                'assets/images/convocation_log.jpeg',
                height: 48,
                width: 48,
                fit: BoxFit.cover,
                errorBuilder: (context, error, stackTrace) {
                  return Container(
                    height: 48,
                    width: 48,
                    decoration: BoxDecoration(
                      color: AppColors.primary.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: const Icon(
                      Icons.school,
                      color: AppColors.primary,
                      size: 28,
                    ),
                  );
                },
              ),
            ),
            const SizedBox(width: 12),
            Text(
              'The Convocation',
              style: TextStyle(
                color: Colors.grey[900],
                fontSize: 18,
                fontWeight: FontWeight.w600,
                letterSpacing: -0.3,
              ),
            ),
          ],
        ),
        centerTitle: false,
        actions: [
          IconButton(
            icon: Icon(
              Icons.notifications_outlined,
              color: Colors.grey[700],
              size: 26,
            ),
            onPressed: () => context.push('/notifications'),
            tooltip: 'Notifications',
          ),
          const SizedBox(width: 4),
        ],
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(4),
          child: Container(
            height: 4,
            child: Row(
              children: [
                Expanded(
                  child: Container(color: AppColors.primary),
                ),
                Expanded(
                  child: Container(color: AppColors.accent),
                ),
                Expanded(
                  child: Container(color: AppColors.secondary),
                ),
              ],
            ),
          ),
        ),
      ),
      drawer: _buildDrawer(),
      body: Stack(
        children: [
          // Background image - MUST logo as watermark
          Positioned.fill(
            child: Opacity(
              opacity: 0.03,
              child: Image.asset(
                'assets/images/must_logo.png',
                fit: BoxFit.contain,
                alignment: Alignment.center,
                errorBuilder: (context, error, stackTrace) {
                  return Container(
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                        colors: [
                          AppColors.primary.withValues(alpha: 0.05),
                          AppColors.accent.withValues(alpha: 0.03),
                          AppColors.secondary.withValues(alpha: 0.05),
                          Colors.white,
                        ],
                        stops: const [0.0, 0.3, 0.6, 1.0],
                      ),
                    ),
                  );
                },
              ),
            ),
          ),
          // Main content
          RefreshIndicator(
            onRefresh: () async {
              await context.read<UserProvider>().loadCurrentUser();
            },
            child: LayoutBuilder(
              builder: (context, constraints) {
                // Responsive breakpoints
                final isTablet = constraints.maxWidth > 600;
                final isLargeScreen = constraints.maxWidth > 900;
                final horizontalPadding = isLargeScreen ? 40.0 : (isTablet ? 30.0 : 20.0);
                final maxWidth = isLargeScreen ? 1200.0 : double.infinity;
                
                return Center(
                  child: ConstrainedBox(
                    constraints: BoxConstraints(maxWidth: maxWidth),
                    child: CustomScrollView(
                      slivers: [
                        // Stories Header
                        SliverToBoxAdapter(
                          child: Container(
                            color: Colors.transparent,
                            padding: EdgeInsets.all(horizontalPadding),
                            child: _buildInstagramStyleHeader(),
                          ),
                        ),
                        SliverToBoxAdapter(child: SizedBox(height: isTablet ? 16 : 12)),
                        // Compact Motivational Tip
                        SliverToBoxAdapter(
                          child: Container(
                            margin: EdgeInsets.symmetric(horizontal: horizontalPadding),
                            child: _buildCompactTip(),
                          ),
                        ),
                        SliverToBoxAdapter(child: SizedBox(height: isTablet ? 16 : 12)),
                        // Quick Actions Grid
                        SliverToBoxAdapter(
                          child: Container(
                            color: Colors.transparent,
                            padding: EdgeInsets.all(horizontalPadding),
                            child: _buildQuickActionsGrid(isTablet, isLargeScreen),
                          ),
                        ),
                        SliverToBoxAdapter(child: SizedBox(height: isTablet ? 16 : 12)),
                        // Notable Alumni Carousel
                        const SliverToBoxAdapter(
                          child: NotableAlumniCarousel(),
                        ),
                        SliverToBoxAdapter(child: SizedBox(height: isTablet ? 16 : 12)),
                        // Start a post card
                        SliverToBoxAdapter(
                          child: Container(
                            margin: EdgeInsets.symmetric(horizontal: horizontalPadding),
                            child: _buildStartPostCard(context),
                          ),
                        ),
                        SliverToBoxAdapter(child: SizedBox(height: isTablet ? 24 : 20)),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
  Widget _buildInstagramStyleHeader() {
    return const Column(
      children: [
        // Stories section only
        StoriesWidget(),
      ],
    );
  }
  Widget _buildCompactTip() {
    // Cycle through theme colors for the tip
    final colors = [AppColors.primary, AppColors.accent, AppColors.secondary];
    final color = colors[_currentTipIndex % colors.length];
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: () {
          // Cycle to next tip on tap
          setState(() {
            _currentTipIndex = (_currentTipIndex + 1) % _motivationalTips.length;
          });
        },
        borderRadius: BorderRadius.circular(12),
        child: Ink(
          decoration: BoxDecoration(
            gradient: LinearGradient(
              colors: [
                color.withValues(alpha: 0.15),
                color.withValues(alpha: 0.08),
              ],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: color.withValues(alpha: 0.3), width: 1.5),
            boxShadow: [
              BoxShadow(
                color: color.withValues(alpha: 0.1),
                blurRadius: 8,
                offset: const Offset(0, 2),
              ),
            ],
          ),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: color.withValues(alpha: 0.2),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(Icons.lightbulb, color: color, size: 20),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: AnimatedSwitcher(
                    duration: const Duration(milliseconds: 400),
                    transitionBuilder: (child, animation) {
                      return FadeTransition(
                        opacity: animation,
                        child: SlideTransition(
                          position: Tween<Offset>(
                            begin: const Offset(0, 0.3),
                            end: Offset.zero,
                          ).animate(animation),
                          child: child,
                        ),
                      );
                    },
                    child: Text(
                      _motivationalTips[_currentTipIndex],
                      key: ValueKey(_currentTipIndex),
                      style: TextStyle(
                        color: color,
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                        height: 1.3,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Icon(
                  Icons.refresh,
                  color: color.withValues(alpha: 0.6),
                  size: 18,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
  Widget _buildStartPostCard(BuildContext context) {
    final user = context.watch<UserProvider>().currentUser;

    return Material(
      color: Colors.transparent,
      borderRadius: BorderRadius.circular(16),
      elevation: 2,
      shadowColor: Colors.black.withValues(alpha: 0.1),
      child: InkWell(
        onTap: () => context.push('/create-post'),
        borderRadius: BorderRadius.circular(16),
        splashColor: AppColors.primary.withValues(alpha: 0.1),
        highlightColor: AppColors.primary.withValues(alpha: 0.05),
        child: Ink(
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: Colors.grey[200]!, width: 1.5),
          ),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Hero(
                  tag: 'user_avatar_post',
                  child: Container(
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      boxShadow: [
                        BoxShadow(
                          color: AppColors.primary.withValues(alpha: 0.2),
                          blurRadius: 8,
                          offset: const Offset(0, 2),
                        ),
                      ],
                    ),
                    child: CircleAvatar(
                      radius: 24,
                      backgroundColor: AppColors.primary.withValues(alpha: 0.1),
                      backgroundImage: user?.profileImageUrl != null
                          ? CachedNetworkImageProvider(user!.profileImageUrl!)
                          : null,
                      child: user?.profileImageUrl == null
                          ? Text(
                              user?.fullName?.substring(0, 1).toUpperCase() ?? 'A',
                              style: TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.bold,
                                color: AppColors.primary,
                              ),
                            )
                          : null,
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
                    decoration: BoxDecoration(
                      color: Colors.grey[50],
                      borderRadius: BorderRadius.circular(25),
                      border: Border.all(color: Colors.grey[300]!),
                    ),
                    child: Text(
                      'What\'s on your mind?',
                      style: TextStyle(
                        color: Colors.grey[600],
                        fontSize: 15,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: AppColors.primary.withValues(alpha: 0.1),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(
                    Icons.edit_outlined,
                    color: AppColors.primary,
                    size: 22,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
  Widget _buildDrawer() {
    return Consumer<UserProvider>(
      builder: (context, userProvider, _) {
        final user = userProvider.currentUser;
        return Drawer(
          backgroundColor: Colors.grey[50],
          child: Column(
            children: [
              // LinkedIn-style clean header
              Container(
                color: Colors.white,
                child: SafeArea(
                  bottom: false,
                  child: Padding(
                    padding: const EdgeInsets.fromLTRB(16, 16, 16, 16),
                    child: Row(
                      children: [
                        // Profile picture on the left
                        GestureDetector(
                          onTap: () {
                            Navigator.pop(context);
                            context.go('/profile');
                          },
                          child: Container(
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              border: Border.all(
                                color: Colors.grey[300]!,
                                width: 2,
                              ),
                            ),
                            child: CircleAvatar(
                              radius: 28,
                              backgroundColor: Colors.grey[100],
                              backgroundImage: user?.profileImageUrl != null
                                  ? CachedNetworkImageProvider(user!.profileImageUrl!)
                                  : null,
                              child: user?.profileImageUrl == null
                                  ? Text(
                                      user?.fullName?.substring(0, 1).toUpperCase() ?? 'A',
                                      style: TextStyle(
                                        fontSize: 22,
                                        color: AppColors.primary,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    )
                                  : null,
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        // Name and email
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Text(
                                user?.fullName ?? 'MUST Alumni',
                                style: TextStyle(
                                  color: Colors.grey[900],
                                  fontSize: 15,
                                  fontWeight: FontWeight.w600,
                                ),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                              if (user?.email != null) ...[
                                const SizedBox(height: 2),
                                Text(
                                  user!.email!,
                                  style: TextStyle(
                                    color: Colors.grey[600],
                                    fontSize: 12,
                                  ),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ],
                              const SizedBox(height: 8),
                              // View Profile button
                              Material(
                                color: Colors.transparent,
                                child: InkWell(
                                  onTap: () {
                                    Navigator.pop(context);
                                    context.go('/profile');
                                  },
                                  borderRadius: BorderRadius.circular(16),
                                  child: Container(
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 12,
                                      vertical: 6,
                                    ),
                                    decoration: BoxDecoration(
                                      border: Border.all(
                                        color: AppColors.primary,
                                        width: 1.5,
                                      ),
                                      borderRadius: BorderRadius.circular(16),
                                    ),
                                    child: Text(
                                      'View Profile',
                                      style: TextStyle(
                                        color: AppColors.primary,
                                        fontSize: 12,
                                        fontWeight: FontWeight.w600,
                                      ),
                                    ),
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
              // Three-color theme line
              Container(
                height: 3,
                child: Row(
                  children: [
                    Expanded(
                      child: Container(color: AppColors.primary),
                    ),
                    Expanded(
                      child: Container(color: AppColors.accent),
                    ),
                    Expanded(
                      child: Container(color: AppColors.secondary),
                    ),
                  ],
                ),
              ),
              // Menu items with interactive styling
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.symmetric(vertical: 8),
                  children: [
                    _buildDrawerSection('CONNECT & ENGAGE', [
                      _buildInteractiveDrawerItem(
                        Icons.article_outlined,
                        'News',
                        () => context.push('/news'),
                        Colors.blue,
                      ),
                      _buildInteractiveDrawerItem(
                        Icons.event_outlined,
                        'Events',
                        () => context.push('/events'),
                        Colors.orange,
                      ),
                      _buildInteractiveDrawerItem(
                        Icons.forum_outlined,
                        'Discussions',
                        () => context.push('/discussions'),
                        Colors.purple,
                      ),
                      _buildInteractiveDrawerItem(
                        Icons.poll_outlined,
                        'Polls',
                        () => context.push('/polls'),
                        Colors.green,
                      ),
                    ]),
                    _buildDrawerSection('CAREER & GROWTH', [
                      _buildInteractiveDrawerItem(
                        Icons.work_outline,
                        'Jobs',
                        () => context.go('/jobs'),
                        Colors.indigo,
                      ),
                      _buildInteractiveDrawerItem(
                        Icons.handshake_outlined,
                        'Mentorship',
                        () => context.push('/mentorship'),
                        Colors.teal,
                      ),
                      _buildInteractiveDrawerItem(
                        Icons.share_outlined,
                        'Referrals',
                        () => context.push('/referrals'),
                        Colors.cyan,
                      ),
                      _buildInteractiveDrawerItem(
                        Icons.mic_outlined,
                        'Guest Lectures',
                        () => context.push('/guest-lectures'),
                        Colors.deepPurple,
                      ),
                    ]),
                    _buildDrawerSection('COMMUNITY', [
                      _buildInteractiveDrawerItem(
                        Icons.school_outlined,
                        'MUST Community',
                        () => context.push('/almater-directory'),
                        AppColors.primary,
                      ),
                      _buildInteractiveDrawerItem(
                        Icons.group_outlined,
                        'Groups',
                        () => context.push('/groups'),
                        Colors.pink,
                      ),
                      _buildInteractiveDrawerItem(
                        Icons.workspace_premium_outlined,
                        'Convocation Team',
                        () => context.push('/convocation-team'),
                        Colors.amber[700]!,
                      ),
                    ]),
                    _buildDrawerSection('MARKETPLACE & MORE', [
                      _buildInteractiveDrawerItem(
                        Icons.store_outlined,
                        'Marketplace',
                        () => context.push('/marketplace'),
                        Colors.red,
                      ),
                      _buildInteractiveDrawerItem(
                        Icons.emoji_events_outlined,
                        'Achievements',
                        () => context.push('/achievements'),
                        Colors.yellow[700]!,
                      ),
                      _buildInteractiveDrawerItem(
                        Icons.favorite_outline,
                        'Donate',
                        () => context.push('/support-donations'),
                        Colors.red[400]!,
                      ),
                      _buildInteractiveDrawerItem(
                        Icons.card_membership_outlined,
                        'Subscriptions',
                        () => context.push('/annual-subscriptions'),
                        Colors.deepOrange,
                      ),
                    ]),
                    const SizedBox(height: 8),
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      child: Divider(
                        color: Colors.grey[300],
                        height: 1,
                        thickness: 1,
                      ),
                    ),
                    const SizedBox(height: 8),
                    _buildInteractiveDrawerItem(
                      Icons.settings_outlined,
                      'Settings',
                      () => context.push('/settings'),
                      Colors.grey[700]!,
                    ),
                    _buildInteractiveDrawerItem(
                      Icons.logout,
                      'Logout',
                      () => _handleLogout(),
                      Colors.red[600]!,
                      isDestructive: true,
                    ),
                    const SizedBox(height: 8),
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
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 10),
          child: Text(
            title,
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w600,
              color: Colors.grey[600],
              letterSpacing: 0.5,
            ),
          ),
        ),
        ...items,
        const SizedBox(height: 4),
      ],
    );
  }
  Widget _buildInteractiveDrawerItem(
    IconData icon,
    String title,
    VoidCallback onTap,
    Color iconColor, {
    bool isDestructive = false,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 2),
      child: Material(
        color: Colors.transparent,
        borderRadius: BorderRadius.circular(8),
        child: InkWell(
          onTap: () {
            Navigator.pop(context);
            onTap();
          },
          borderRadius: BorderRadius.circular(8),
          splashColor: AppColors.primary.withValues(alpha: 0.1),
          highlightColor: AppColors.primary.withValues(alpha: 0.05),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
            child: Row(
              children: [
                // Simple icon without colored background
                Icon(
                  icon,
                  color: isDestructive ? Colors.red[600] : Colors.grey[700],
                  size: 22,
                ),
                const SizedBox(width: 16),
                // Title
                Expanded(
                  child: Text(
                    title,
                    style: TextStyle(
                      color: isDestructive ? Colors.red[600] : Colors.grey[800],
                      fontSize: 15,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
                // Subtle arrow
                Icon(
                  Icons.chevron_right,
                  color: Colors.grey[400],
                  size: 20,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
  Widget _buildQuickActionsGrid(bool isTablet, bool isLargeScreen) {
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
      {
        'icon': Icons.message,
        'label': 'Messages',
        'onTap': () => context.push('/chats')
      },
      {
        'icon': Icons.event,
        'label': 'Events',
        'onTap': () => context.push('/events')
      },
      {
        'icon': Icons.workspace_premium,
        'label': 'Convocation',
        'onTap': () => context.push('/convocation-team')
      },
      {
        'icon': Icons.mic,
        'label': 'Lectures',
        'onTap': () => context.push('/guest-lectures')
      },
      {
        'icon': Icons.rocket_launch,
        'label': 'Career',
        'onTap': () => context.push('/enhanced-career-center')
      },
      {
        'icon': Icons.forum,
        'label': 'Discuss',
        'onTap': () => context.push('/discussions')
      },
    ];

    // Responsive column count
    final crossAxisCount = isLargeScreen ? 8 : (isTablet ? 4 : 4);
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Quick Actions',
          style: Theme.of(context).textTheme.titleLarge?.copyWith(
            fontSize: isTablet ? 20 : 18,
            fontWeight: FontWeight.bold,
            color: Colors.grey[900],
          ),
        ),
        SizedBox(height: isTablet ? 20 : 16),
        GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: crossAxisCount,
            mainAxisSpacing: isTablet ? 20 : 16,
            crossAxisSpacing: isTablet ? 20 : 16,
            childAspectRatio: 0.85,
          ),
          itemCount: actions.length,
          itemBuilder: (context, index) {
            final action = actions[index];
            return _cardAnimations != null
                ? ScaleTransition(
                    scale: _cardAnimations![index],
                    child: _AnimatedQuickActionItem(
                      icon: action['icon'] as IconData,
                      label: action['label'] as String,
                      onTap: action['onTap'] as VoidCallback,
                      index: index,
                    ),
                  )
                : _AnimatedQuickActionItem(
                    icon: action['icon'] as IconData,
                    label: action['label'] as String,
                    onTap: action['onTap'] as VoidCallback,
                    index: index,
                  );
          },
        ),
      ],
    );
  }
  Widget _buildQuickActionItem(
      IconData icon, String label, VoidCallback onTap) {
    return _AnimatedQuickActionItem(icon: icon, label: label, onTap: onTap, index: 0);
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
              await context.read<UserProvider>().setUserOffline();
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
class _AnimatedQuickActionItem extends StatefulWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;
  final int index;
  const _AnimatedQuickActionItem({
    required this.icon,
    required this.label,
    required this.onTap,
    required this.index,
  });
  @override
  State<_AnimatedQuickActionItem> createState() => _AnimatedQuickActionItemState();
}
class _AnimatedQuickActionItemState extends State<_AnimatedQuickActionItem>
    with SingleTickerProviderStateMixin {
  bool _isPressed = false;
  late AnimationController _bounceController;
  late Animation<double> _bounceAnimation;
  @override
  void initState() {
    super.initState();
    _bounceController = AnimationController(
      duration: const Duration(milliseconds: 150),
      vsync: this,
    );
    _bounceAnimation = Tween<double>(begin: 1.0, end: 0.92).animate(
      CurvedAnimation(parent: _bounceController, curve: Curves.easeInOut),
    );
  }
  @override
  void dispose() {
    _bounceController.dispose();
    super.dispose();
  }
  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTapDown: (_) {
        setState(() => _isPressed = true);
        _bounceController.forward();
      },
      onTapUp: (_) {
        setState(() => _isPressed = false);
        _bounceController.reverse();
        widget.onTap();
      },
      onTapCancel: () {
        setState(() => _isPressed = false);
        _bounceController.reverse();
      },
      child: ScaleTransition(
        scale: _bounceAnimation,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          decoration: BoxDecoration(
            gradient: LinearGradient(
              colors: _isPressed
                  ? [AppColors.primary.withValues(alpha: 0.2), AppColors.primary.withValues(alpha: 0.15)]
                  : [AppColors.primary.withValues(alpha: 0.1), AppColors.primary.withValues(alpha: 0.05)],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
            borderRadius: BorderRadius.circular(16),
            border: Border.all(
              color: _isPressed ? AppColors.primary.withValues(alpha: 0.5) : AppColors.primary.withValues(alpha: 0.3),
              width: _isPressed ? 2 : 1,
            ),
            boxShadow: _isPressed
                ? [
                    BoxShadow(
                      color: AppColors.primary.withValues(alpha: 0.2),
                      blurRadius: 12,
                      offset: const Offset(0, 4),
                    ),
                  ]
                : [
                    BoxShadow(
                      color: Colors.grey.withValues(alpha: 0.1),
                      blurRadius: 4,
                      offset: const Offset(0, 2),
                    ),
                  ],
          ),
          padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 4),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: _isPressed
                      ? AppColors.primary.withValues(alpha: 0.3)
                      : AppColors.primary.withValues(alpha: 0.2),
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  widget.icon,
                  color: AppColors.primary,
                  size: 22,
                ),
              ),
              const SizedBox(height: 6),
              Text(
                widget.label,
                style: TextStyle(
                  fontSize: 10.5,
                  fontWeight: FontWeight.w600,
                  color: Colors.grey[900],
                  height: 1.1,
                ),
                textAlign: TextAlign.center,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
