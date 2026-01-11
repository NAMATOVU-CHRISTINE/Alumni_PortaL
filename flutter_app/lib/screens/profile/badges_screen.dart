import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:alumni_portal/providers/gamification_provider.dart';
import 'package:alumni_portal/models/badge_model.dart';
import 'package:alumni_portal/config/theme.dart';

class BadgesScreen extends StatefulWidget {
  const BadgesScreen({super.key});

  @override
  State<BadgesScreen> createState() => _BadgesScreenState();
}

class _BadgesScreenState extends State<BadgesScreen> {
  @override
  void initState() {
    super.initState();
    context.read<GamificationProvider>().loadAchievements();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Badges & Achievements'),
      ),
      body: Consumer<GamificationProvider>(
        builder: (context, provider, _) {
          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildPointsCard(provider),
                const SizedBox(height: 24),
                _buildEarnedBadges(provider),
                const SizedBox(height: 24),
                _buildAllBadges(provider),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildPointsCard(GamificationProvider provider) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [AppColors.primary, AppColors.primaryDark],
        ),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        children: [
          const Icon(Icons.stars, color: Colors.amber, size: 48),
          const SizedBox(height: 12),
          Text(
            '${provider.achievements.totalPoints}',
            style: const TextStyle(
              color: Colors.white,
              fontSize: 48,
              fontWeight: FontWeight.bold,
            ),
          ),
          const Text(
            'Total Points',
            style: TextStyle(color: Colors.white70, fontSize: 16),
          ),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              _buildStatItem('Posts', provider.achievements.postsCount),
              _buildStatItem(
                  'Connections', provider.achievements.connectionsCount),
              _buildStatItem('Badges', provider.earnedBadges.length),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildStatItem(String label, int value) {
    return Column(
      children: [
        Text(
          '$value',
          style: const TextStyle(
            color: Colors.white,
            fontSize: 20,
            fontWeight: FontWeight.bold,
          ),
        ),
        Text(
          label,
          style: const TextStyle(color: Colors.white70, fontSize: 12),
        ),
      ],
    );
  }

  Widget _buildEarnedBadges(GamificationProvider provider) {
    if (provider.earnedBadges.isEmpty) {
      return const SizedBox.shrink();
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Your Badges',
          style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 12,
          runSpacing: 12,
          children: provider.earnedBadges
              .map((badge) => _buildBadgeChip(badge, true))
              .toList(),
        ),
      ],
    );
  }

  Widget _buildAllBadges(GamificationProvider provider) {
    final categories = ['engagement', 'community', 'mentorship', 'career'];
    final categoryNames = {
      'engagement': 'Engagement',
      'community': 'Community',
      'mentorship': 'Mentorship',
      'career': 'Career',
    };

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'All Badges',
          style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 16),
        ...categories.map((category) {
          final badges = BadgeModel.allBadges
              .where((b) => b.category == category)
              .toList();
          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                categoryNames[category]!,
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                  color: Colors.grey[700],
                ),
              ),
              const SizedBox(height: 8),
              ...badges.map((badge) {
                final isEarned =
                    provider.achievements.earnedBadgeIds.contains(badge.id);
                return _buildBadgeListItem(badge, isEarned);
              }),
              const SizedBox(height: 16),
            ],
          );
        }),
      ],
    );
  }

  Widget _buildBadgeChip(BadgeModel badge, bool isEarned) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: isEarned ? Colors.amber[50] : Colors.grey[100],
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: isEarned ? Colors.amber : Colors.grey[300]!,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(badge.icon, style: const TextStyle(fontSize: 24)),
          const SizedBox(width: 8),
          Text(
            badge.name,
            style: TextStyle(
              fontWeight: FontWeight.w600,
              color: isEarned ? Colors.amber[800] : Colors.grey[600],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBadgeListItem(BadgeModel badge, bool isEarned) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        leading: Container(
          width: 48,
          height: 48,
          decoration: BoxDecoration(
            color: isEarned ? Colors.amber[50] : Colors.grey[100],
            borderRadius: BorderRadius.circular(12),
          ),
          child: Center(
            child: Text(
              badge.icon,
              style: TextStyle(
                fontSize: 24,
                color: isEarned ? null : Colors.grey,
              ),
            ),
          ),
        ),
        title: Text(
          badge.name,
          style: TextStyle(
            fontWeight: FontWeight.w600,
            color: isEarned ? null : Colors.grey,
          ),
        ),
        subtitle: Text(
          badge.description,
          style: TextStyle(
            fontSize: 12,
            color: isEarned ? Colors.grey[600] : Colors.grey[400],
          ),
        ),
        trailing: isEarned
            ? const Icon(Icons.check_circle, color: Colors.green)
            : Text(
                '${badge.pointsRequired} pts',
                style: TextStyle(color: Colors.grey[400], fontSize: 12),
              ),
      ),
    );
  }
}
