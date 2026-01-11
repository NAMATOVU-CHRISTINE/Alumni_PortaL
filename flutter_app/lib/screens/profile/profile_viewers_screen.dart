import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:alumni_portal/providers/gamification_provider.dart';
import 'package:alumni_portal/config/theme.dart';

class ProfileViewersScreen extends StatefulWidget {
  const ProfileViewersScreen({super.key});

  @override
  State<ProfileViewersScreen> createState() => _ProfileViewersScreenState();
}

class _ProfileViewersScreenState extends State<ProfileViewersScreen> {
  @override
  void initState() {
    super.initState();
    context.read<GamificationProvider>().loadProfileViewers();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Who Viewed Your Profile'),
      ),
      body: Consumer<GamificationProvider>(
        builder: (context, provider, _) {
          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.profileViewers.isEmpty) {
            return _buildEmptyState();
          }

          return Column(
            children: [
              _buildHeader(provider),
              Expanded(
                child: ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: provider.profileViewers.length,
                  itemBuilder: (context, index) {
                    return _buildViewerCard(provider.profileViewers[index]);
                  },
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  Widget _buildHeader(GamificationProvider provider) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [AppColors.primary, AppColors.primaryDark],
        ),
      ),
      child: Column(
        children: [
          const Icon(Icons.visibility, color: Colors.white, size: 48),
          const SizedBox(height: 12),
          Text(
            '${provider.profileViewCount}',
            style: const TextStyle(
              color: Colors.white,
              fontSize: 48,
              fontWeight: FontWeight.bold,
            ),
          ),
          const Text(
            'Profile Views',
            style: TextStyle(color: Colors.white70, fontSize: 16),
          ),
          const SizedBox(height: 8),
          const Text(
            'in the last 90 days',
            style: TextStyle(color: Colors.white54, fontSize: 12),
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.visibility_off, size: 80, color: Colors.grey[300]),
          const SizedBox(height: 16),
          const Text(
            'No profile views yet',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
          ),
          const SizedBox(height: 8),
          Text(
            'Complete your profile to attract more views!',
            style: TextStyle(color: Colors.grey[600]),
          ),
          const SizedBox(height: 24),
          ElevatedButton(
            onPressed: () => context.push('/edit-profile'),
            child: const Text('Complete Profile'),
          ),
        ],
      ),
    );
  }

  Widget _buildViewerCard(Map<String, dynamic> viewer) {
    final viewedAt = viewer['viewedAt'] as DateTime;

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: ListTile(
        leading: CircleAvatar(
          radius: 24,
          backgroundImage: viewer['viewerImageUrl'] != null
              ? CachedNetworkImageProvider(viewer['viewerImageUrl'])
              : null,
          child: viewer['viewerImageUrl'] == null
              ? Text(
                  (viewer['viewerName'] as String)
                      .substring(0, 1)
                      .toUpperCase(),
                  style: const TextStyle(fontWeight: FontWeight.bold),
                )
              : null,
        ),
        title: Text(
          viewer['viewerName'] ?? 'Alumni',
          style: const TextStyle(fontWeight: FontWeight.w600),
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (viewer['viewerTitle'] != null)
              Text(
                viewer['viewerTitle'],
                style: const TextStyle(fontSize: 12),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            Text(
              'Viewed ${timeago.format(viewedAt)}',
              style: TextStyle(fontSize: 11, color: Colors.grey[500]),
            ),
          ],
        ),
        trailing: OutlinedButton(
          onPressed: () => context.push('/view-profile/${viewer['viewerId']}'),
          style: OutlinedButton.styleFrom(
            padding: const EdgeInsets.symmetric(horizontal: 12),
            visualDensity: VisualDensity.compact,
          ),
          child: const Text('View'),
        ),
        onTap: () => context.push('/view-profile/${viewer['viewerId']}'),
      ),
    );
  }
}
