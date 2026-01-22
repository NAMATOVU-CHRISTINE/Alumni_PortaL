import 'package:flutter/material.dart';
import 'package:alumni_portal/config/theme.dart';

class AchievementsScreen extends StatefulWidget {
  const AchievementsScreen({super.key});

  @override
  State<AchievementsScreen> createState() => _AchievementsScreenState();
}

class _AchievementsScreenState extends State<AchievementsScreen>
    with TickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      body: Column(
        children: [
          // Header Section - Above tabs
          Container(
            width: double.infinity,
            padding: const EdgeInsets.fromLTRB(16, 48, 16, 16),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [AppColors.primary, AppColors.primaryDark],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    IconButton(
                      icon: const Icon(Icons.arrow_back,
                          color: Colors.white, size: 22),
                      onPressed: () => Navigator.pop(context),
                      padding: EdgeInsets.zero,
                      constraints: const BoxConstraints(),
                    ),
                    const SizedBox(width: 12),
                    Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: Colors.white.withValues(alpha: 0.2),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.emoji_events,
                        color: Colors.white,
                        size: 24,
                      ),
                    ),
                    const SizedBox(width: 12),
                    const Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Achievements',
                            style: TextStyle(
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                              color: Colors.white,
                            ),
                          ),
                          Text(
                            'Track your progress and earn rewards',
                            style: TextStyle(
                              color: Colors.white70,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          // Tabs
          Container(
            color: Colors.white,
            child: TabBar(
              controller: _tabController,
              labelColor: AppColors.primary,
              unselectedLabelColor: Colors.grey,
              indicatorColor: AppColors.primary,
              tabs: const [
                Tab(text: 'Badges'),
                Tab(text: 'Challenges'),
                Tab(text: 'Leaderboard'),
              ],
            ),
          ),

          // Tab Content
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: [
                _buildBadgesTab(),
                _buildChallengesTab(),
                _buildLeaderboardTab(),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBadgesTab() {
    final badges = _getMockBadges();

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // User Stats Card
          Card(
            elevation: 2,
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Column(
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            colors: [AppColors.primary, AppColors.primaryDark],
                          ),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(
                          Icons.emoji_events,
                          color: Colors.white,
                          size: 24,
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Level 5 Contributor',
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            Text(
                              '2,450 points',
                              style: TextStyle(
                                color: AppColors.primary,
                                fontWeight: FontWeight.w600,
                                fontSize: 13,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),

                  // Progress to next level
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text('Progress to Level 6',
                              style: TextStyle(fontSize: 13)),
                          Text('550 points to go',
                              style: TextStyle(
                                  color: Colors.grey[600], fontSize: 12)),
                        ],
                      ),
                      const SizedBox(height: 8),
                      LinearProgressIndicator(
                        value: 0.7,
                        backgroundColor: Colors.grey[300],
                        valueColor:
                            AlwaysStoppedAnimation<Color>(AppColors.primary),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 24),

          // Badges Grid
          const Text(
            'Your Badges',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          GridView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 3,
              childAspectRatio: 0.8,
              crossAxisSpacing: 12,
              mainAxisSpacing: 12,
            ),
            itemCount: badges.length,
            itemBuilder: (context, index) {
              final badge = badges[index];
              return _buildBadgeCard(badge);
            },
          ),
        ],
      ),
    );
  }

  Widget _buildBadgeCard(Map<String, dynamic> badge) {
    final isUnlocked = badge['isUnlocked'] as bool;

    return Card(
      elevation: isUnlocked ? 2 : 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Container(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(12),
          color: isUnlocked ? Colors.white : Colors.grey[100],
        ),
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: isUnlocked
                      ? _getBadgeColor(badge['rarity']).withValues(alpha: 0.1)
                      : Colors.grey[300],
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  _getBadgeIcon(badge['category']),
                  size: 24,
                  color: isUnlocked
                      ? _getBadgeColor(badge['rarity'])
                      : Colors.grey[500],
                ),
              ),
              const SizedBox(height: 8),
              Text(
                badge['name'],
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w600,
                  color: isUnlocked ? Colors.black : Colors.grey[600],
                ),
                textAlign: TextAlign.center,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
              if (isUnlocked)
                Container(
                  margin: const EdgeInsets.only(top: 4),
                  padding:
                      const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(
                    color: _getBadgeColor(badge['rarity']),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    badge['rarity'].toUpperCase(),
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 8,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildChallengesTab() {
    final challenges = _getMockChallenges();

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: challenges.length,
      itemBuilder: (context, index) {
        final challenge = challenges[index];
        return _buildChallengeCard(challenge);
      },
    );
  }

  Widget _buildChallengeCard(Map<String, dynamic> challenge) {
    final progress = challenge['progress'] as double;
    final isCompleted = progress >= 1.0;

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: _getChallengeTypeColor(challenge['type'])
                        .withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Icon(
                    _getChallengeTypeIcon(challenge['type']),
                    color: _getChallengeTypeColor(challenge['type']),
                    size: 20,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        challenge['title'],
                        style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text(
                        challenge['description'],
                        style: TextStyle(
                          color: Colors.grey[600],
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                ),
                if (isCompleted)
                  Container(
                    padding: const EdgeInsets.all(4),
                    decoration: const BoxDecoration(
                      color: Colors.green,
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(
                      Icons.check,
                      color: Colors.white,
                      size: 16,
                    ),
                  ),
              ],
            ),

            const SizedBox(height: 12),

            // Progress
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'Progress: ${(progress * 100).toInt()}%',
                      style: const TextStyle(fontSize: 12),
                    ),
                    Text(
                      '${challenge['pointsReward']} points',
                      style: TextStyle(
                        fontSize: 12,
                        color: AppColors.primary,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                LinearProgressIndicator(
                  value: progress,
                  backgroundColor: Colors.grey[300],
                  valueColor: AlwaysStoppedAnimation<Color>(
                    isCompleted ? Colors.green : AppColors.primary,
                  ),
                ),
              ],
            ),

            const SizedBox(height: 8),

            // Time remaining
            Text(
              'Ends in ${challenge['timeRemaining']}',
              style: TextStyle(
                fontSize: 10,
                color: Colors.grey[600],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildLeaderboardTab() {
    final leaderboard = _getMockLeaderboard();

    return Column(
      children: [
        // Top 3 podium
        Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            gradient: LinearGradient(
              colors: [AppColors.primary, AppColors.primaryDark],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              _buildPodiumPosition(leaderboard[1], 2, 60),
              _buildPodiumPosition(leaderboard[0], 1, 80),
              _buildPodiumPosition(leaderboard[2], 3, 60),
            ],
          ),
        ),

        // Rest of leaderboard
        Expanded(
          child: ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: leaderboard.length - 3,
            itemBuilder: (context, index) {
              final entry = leaderboard[index + 3];
              return _buildLeaderboardEntry(entry, index + 4);
            },
          ),
        ),
      ],
    );
  }

  Widget _buildPodiumPosition(
      Map<String, dynamic> entry, int position, double height) {
    Color positionColor;
    IconData positionIcon;

    switch (position) {
      case 1:
        positionColor = Colors.amber;
        positionIcon = Icons.emoji_events;
        break;
      case 2:
        positionColor = Colors.grey[400]!;
        positionIcon = Icons.emoji_events;
        break;
      case 3:
        positionColor = Colors.brown;
        positionIcon = Icons.emoji_events;
        break;
      default:
        positionColor = Colors.grey;
        positionIcon = Icons.emoji_events;
    }

    return Column(
      children: [
        CircleAvatar(
          radius: 25,
          backgroundColor: Colors.white,
          child: Text(
            entry['name'].substring(0, 1).toUpperCase(),
            style: TextStyle(
              color: AppColors.primary,
              fontWeight: FontWeight.bold,
              fontSize: 18,
            ),
          ),
        ),
        const SizedBox(height: 8),
        Text(
          entry['name'],
          style: const TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
            fontSize: 12,
          ),
          textAlign: TextAlign.center,
        ),
        Text(
          '${entry['points']} pts',
          style: const TextStyle(
            color: Colors.white70,
            fontSize: 10,
          ),
        ),
        const SizedBox(height: 8),
        Container(
          width: 60,
          height: height,
          decoration: BoxDecoration(
            color: positionColor,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(8)),
          ),
          child: Icon(
            positionIcon,
            color: Colors.white,
            size: 24,
          ),
        ),
      ],
    );
  }

  Widget _buildLeaderboardEntry(Map<String, dynamic> entry, int position) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: AppColors.primary.withValues(alpha: 0.1),
          child: Text(
            '$position',
            style: TextStyle(
              color: AppColors.primary,
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
        title: Text(
          entry['name'],
          style: const TextStyle(fontWeight: FontWeight.w600),
        ),
        subtitle: Text('Level ${entry['level']}'),
        trailing: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text(
              '${entry['points']}',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: AppColors.primary,
              ),
            ),
            const Text(
              'points',
              style: TextStyle(fontSize: 10),
            ),
          ],
        ),
      ),
    );
  }

  Color _getBadgeColor(String rarity) {
    switch (rarity.toLowerCase()) {
      case 'common':
        return Colors.grey;
      case 'rare':
        return Colors.blue;
      case 'epic':
        return Colors.purple;
      case 'legendary':
        return Colors.orange;
      default:
        return AppColors.primary;
    }
  }

  IconData _getBadgeIcon(String category) {
    switch (category.toLowerCase()) {
      case 'networking':
        return Icons.people;
      case 'contribution':
        return Icons.favorite;
      case 'learning':
        return Icons.school;
      case 'mentorship':
        return Icons.handshake;
      default:
        return Icons.emoji_events;
    }
  }

  Color _getChallengeTypeColor(String type) {
    switch (type.toLowerCase()) {
      case 'daily':
        return Colors.green;
      case 'weekly':
        return Colors.blue;
      case 'monthly':
        return Colors.purple;
      case 'special':
        return Colors.orange;
      default:
        return AppColors.primary;
    }
  }

  IconData _getChallengeTypeIcon(String type) {
    switch (type.toLowerCase()) {
      case 'daily':
        return Icons.today;
      case 'weekly':
        return Icons.date_range;
      case 'monthly':
        return Icons.calendar_month;
      case 'special':
        return Icons.star;
      default:
        return Icons.flag;
    }
  }

  List<Map<String, dynamic>> _getMockBadges() {
    return [
      {
        'name': 'First Post',
        'category': 'contribution',
        'rarity': 'common',
        'isUnlocked': true,
      },
      {
        'name': 'Networker',
        'category': 'networking',
        'rarity': 'rare',
        'isUnlocked': true,
      },
      {
        'name': 'Mentor',
        'category': 'mentorship',
        'rarity': 'epic',
        'isUnlocked': false,
      },
      {
        'name': 'Scholar',
        'category': 'learning',
        'rarity': 'rare',
        'isUnlocked': true,
      },
      {
        'name': 'Supporter',
        'category': 'contribution',
        'rarity': 'common',
        'isUnlocked': true,
      },
      {
        'name': 'Legend',
        'category': 'contribution',
        'rarity': 'legendary',
        'isUnlocked': false,
      },
    ];
  }

  List<Map<String, dynamic>> _getMockChallenges() {
    return [
      {
        'title': 'Daily Connector',
        'description': 'Connect with 3 new alumni today',
        'type': 'daily',
        'progress': 0.6,
        'pointsReward': 50,
        'timeRemaining': '8 hours',
      },
      {
        'title': 'Weekly Contributor',
        'description': 'Make 5 posts this week',
        'type': 'weekly',
        'progress': 0.8,
        'pointsReward': 200,
        'timeRemaining': '2 days',
      },
      {
        'title': 'Monthly Mentor',
        'description': 'Help 10 alumni this month',
        'type': 'monthly',
        'progress': 0.3,
        'pointsReward': 500,
        'timeRemaining': '15 days',
      },
      {
        'title': 'MUST Anniversary',
        'description': 'Participate in anniversary events',
        'type': 'special',
        'progress': 1.0,
        'pointsReward': 1000,
        'timeRemaining': 'Completed',
      },
    ];
  }

  List<Map<String, dynamic>> _getMockLeaderboard() {
    return [
      {'name': 'Sarah Nakato', 'points': 5420, 'level': 8},
      {'name': 'David Mukasa', 'points': 4890, 'level': 7},
      {'name': 'Grace Namuli', 'points': 4650, 'level': 7},
      {'name': 'John Ssemakula', 'points': 4200, 'level': 6},
      {'name': 'Mary Nakirya', 'points': 3980, 'level': 6},
      {'name': 'Peter Okello', 'points': 3750, 'level': 5},
      {'name': 'Jane Akello', 'points': 3500, 'level': 5},
    ];
  }
}
