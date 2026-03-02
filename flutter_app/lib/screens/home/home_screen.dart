import 'dart:async';
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
        leading: Builder(
          builder: (context) => IconButton(
            icon: const Icon(Icons.menu),
            onPressed: () => Scaffold.of(context).openDrawer(),
          ),
        ),
        title: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              padding: const EdgeInsets.all(4),
              decoration: BoxDecoration(
                color: Theme.of(context).cardColor,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Image.asset(
                'assets/images/convocation_log.jpeg',
                height: 32,
                width: 32,
                errorBuilder: (context, error, stackTrace) {
                  return Container(
                    padding: const EdgeInsets.all(6),
                    decoration: BoxDecoration(
                      color: Theme.of(context).cardColor,
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: const Icon(Icons.school,
                        color: AppColors.primary, size: 20),
                  );
                },
              ),
            ),
            const SizedBox(width: 8),
            Flexible(
              child: Text(
                'MUST Alumni Portal',
                style: Theme.of(context).appBarTheme.titleTextStyle?.copyWith(
                      fontSize: 14,
                      fontWeight: FontWeight.bold,
                    ),
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.notifications_outlined, size: 24),
            onPressed: () => context.push('/notifications'),
            tooltip: 'Notifications',
            padding: const EdgeInsets.all(8),
          ),
          const SizedBox(width: 8),
        ],
      ),
      drawer: _buildDrawer(),
      body: RefreshIndicator(
        onRefresh: () async {
          await context.read<UserProvider>().loadCurrentUser();
        },
        child: CustomScrollView(
          slivers: [
            // Stories Header
            SliverToBoxAdapter(
              child: Container(
                color: Theme.of(context).cardColor,
                padding: const EdgeInsets.all(20),
                child: _buildInstagramStyleHeader(),
              ),
            ),
            const SliverToBoxAdapter(child: SizedBox(height: 12)),

            // Compact Motivational Tip
            SliverToBoxAdapter(
              child: Container(
                margin: const EdgeInsets.symmetric(horizontal: 20),
                child: _buildCompactTip(),
              ),
            ),
            const SliverToBoxAdapter(child: SizedBox(height: 12)),

            // Quick Actions Grid
            SliverToBoxAdapter(
              child: Container(
                color: Theme.of(context).cardColor,
                padding: const EdgeInsets.all(20),
                child: LayoutBuilder(
                  builder: (context, constraints) {
                    final isTablet = constraints.maxWidth > 600;
                    return _buildQuickActionsGrid(isTablet);
                  },
                ),
              ),
            ),
            const SliverToBoxAdapter(child: SizedBox(height: 12)),

            // Notable Alumni Carousel
            const SliverToBoxAdapter(
              child: NotableAlumniCarousel(),
            ),
            const SliverToBoxAdapter(child: SizedBox(height: 12)),

            // Start a post card
            SliverToBoxAdapter(
              child: Container(
                margin: const EdgeInsets.symmetric(horizontal: 20),
                child: _buildStartPostCard(context),
              ),
            ),
            const SliverToBoxAdapter(child: SizedBox(height: 20)),
          ],
        ),
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
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: AppColors.primary.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: AppColors.primary.withValues(alpha: 0.2)),
      ),
      child: Row(
        children: [
          Icon(Icons.lightbulb, color: AppColors.primary, size: 20),
          const SizedBox(width: 8),
          Expanded(
            child: AnimatedSwitcher(
              duration: const Duration(milliseconds: 500),
              child: Text(
                _motivationalTips[_currentTipIndex],
                key: ValueKey(_currentTipIndex),
                style: TextStyle(
                  color: AppColors.primary,
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStartPostCard(BuildContext context) {
    final user = context.watch<UserProvider>().currentUser;

    return Card(
      margin: EdgeInsets.zero,
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: Theme.of(context).dividerColor),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            CircleAvatar(
              radius: 24,
              backgroundImage: user?.profileImageUrl != null
                  ? CachedNetworkImageProvider(user!.profileImageUrl!)
                  : null,
              child: user?.profileImageUrl == null
                  ? Text(
                      user?.fullName?.substring(0, 1).toUpperCase() ?? 'A',
                      style: const TextStyle(
                          fontSize: 18, fontWeight: FontWeight.bold),
                    )
                  : null,
            ),
            const SizedBox(width: 16),
            Expanded(
              child: InkWell(
                onTap: () => context.push('/create-post'),
                child: Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
                  decoration: BoxDecoration(
                    border: Border.all(color: Theme.of(context).dividerColor),
                    borderRadius: BorderRadius.circular(25),
                    color: Theme.of(context).scaffoldBackgroundColor,
                  ),
                  child: Text(
                    'What\'s on your mind?',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          fontWeight: FontWeight.w500,
                        ),
                  ),
                ),
              ),
            ),
            const SizedBox(width: 12),
            IconButton(
              onPressed: () => context.push('/create-post'),
              icon: Icon(
                Icons.edit_outlined,
                color: AppColors.primary,
                size: 24,
              ),
              tooltip: 'Create Post',
            ),
          ],
        ),
      ),
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
                  user?.fullName ?? 'MUST Alumni',
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
                accountEmail: null,
              ),
              Expanded(
                child: ListView(
                  padding: EdgeInsets.zero,
                  children: [
                    _buildDrawerSection('Career', [
                      _buildDrawerItem(
                          Icons.work, 'Jobs', () => context.go('/jobs'), false),
                      _buildDrawerItem(Icons.share, 'Referrals',
                          () => context.push('/referrals'), false),
                    ]),
                    _buildDrawerSection('Discover', [
                      _buildDrawerItem(Icons.article, 'News',
                          () => context.push('/news'), false),
                      _buildDrawerItem(Icons.poll, 'Polls',
                          () => context.push('/polls'), false),
                      _buildDrawerItem(Icons.forum, 'Discussions',
                          () => context.push('/discussions'), false),
                      _buildDrawerItem(Icons.event, 'Events',
                          () => context.push('/events'), false),
                    ]),
                    _buildDrawerSection('Network', [
                      _buildDrawerItem(Icons.school, 'MUST Community',
                          () => context.push('/almater-directory'), false),
                      _buildDrawerItem(Icons.handshake, 'Mentorship',
                          () => context.push('/mentorship'), false),
                      _buildDrawerItem(Icons.group, 'Groups',
                          () => context.push('/groups'), false),
                    ]),
                    _buildDrawerSection('University', [
                      _buildDrawerItem(
                          Icons.workspace_premium,
                          'Convocation Team',
                          () => context.push('/convocation-team'),
                          false),
                      _buildDrawerItem(Icons.mic, 'Guest Lectures',
                          () => context.push('/guest-lectures'), false),
                    ]),
                    _buildDrawerSection('More', [
                      _buildDrawerItem(Icons.emoji_events, 'Achievements',
                          () => context.push('/achievements'), false),
                      _buildDrawerItem(Icons.favorite, 'Support & Donations',
                          () => context.push('/support-donations'), false),
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

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Quick Actions',
          style: Theme.of(context).textTheme.titleLarge,
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
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: _isPressed
                      ? AppColors.primary.withValues(alpha: 0.3)
                      : AppColors.primary.withValues(alpha: 0.2),
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  widget.icon,
                  color: AppColors.primary,
                  size: 24,
                ),
              ),
              const SizedBox(height: 6),
              Text(
                widget.label,
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.w600,
                  color: Colors.grey[800],
                ),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

