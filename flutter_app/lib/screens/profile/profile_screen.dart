import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/providers/auth_provider.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/config/theme.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<UserProvider>().loadCurrentUser();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Profile'),
        actions: [
          IconButton(
            icon: const Icon(Icons.edit),
            onPressed: () => context.push('/edit-profile'),
          ),
        ],
      ),
      body: Consumer<UserProvider>(
        builder: (context, provider, _) {
          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          final user = provider.currentUser;
          if (user == null) {
            return const Center(child: Text('Failed to load profile'));
          }

          return SingleChildScrollView(
            child: Column(
              children: [
                _buildProfileHeader(user),
                _buildProfileInfo(user),
                _buildSkillsSection(user),
                _buildSocialLinks(user),
                const SizedBox(height: 24),
                _buildLogoutButton(),
                const SizedBox(height: 32),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildProfileHeader(user) {
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
          CircleAvatar(
            key: ValueKey(user.profileImageUrl ?? 'no-image'),
            radius: 50,
            backgroundColor: Colors.white,
            backgroundImage: user.profileImageUrl != null
                ? CachedNetworkImageProvider(user.profileImageUrl!)
                : null,
            child: user.profileImageUrl == null
                ? Text(
                    user.fullName?.substring(0, 1).toUpperCase() ?? 'A',
                    style: const TextStyle(
                      fontSize: 40,
                      color: AppColors.primary,
                    ),
                  )
                : null,
          ),
          const SizedBox(height: 16),
          Text(
            user.fullName ?? 'Alumni User',
            style: const TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          if (user.username != null)
            Text(
              '@${user.username}',
              style: const TextStyle(color: Colors.white70),
            ),
          const SizedBox(height: 8),
          if (user.currentJob != null || user.company != null)
            Text(
              '${user.currentJob ?? ''} ${user.company != null ? "at ${user.company}" : ""}',
              style: const TextStyle(color: Colors.white70),
              textAlign: TextAlign.center,
            ),
          const SizedBox(height: 8),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.2),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Text(
              user.userType.toUpperCase(),
              style: const TextStyle(
                color: Colors.white,
                fontSize: 12,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProfileInfo(user) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'About',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),
          if (user.bio != null && user.bio!.isNotEmpty)
            Text(
              user.bio!,
              style: const TextStyle(color: AppColors.textSecondary),
            )
          else
            const Text(
              'No bio added yet',
              style: TextStyle(
                color: AppColors.textSecondary,
                fontStyle: FontStyle.italic,
              ),
            ),
          const SizedBox(height: 24),
          const Text(
            'Details',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),
          _buildInfoRow(Icons.email, 'Email', user.email ?? 'Not provided'),
          _buildInfoRow(Icons.school, 'Major', user.major ?? 'Not provided'),
          _buildInfoRow(
            Icons.calendar_today,
            'Graduation Year',
            user.graduationYear ?? 'Not provided',
          ),
          _buildInfoRow(
            Icons.location_on,
            'Location',
            user.location ?? 'Not provided',
          ),
          _buildInfoRow(
            Icons.badge,
            user.idLabel,
            user.displayId ?? 'Not provided',
          ),
          if (user.workStatus != null)
            _buildInfoRow(Icons.work, 'Work Status', user.workStatus!),
          // Show subscription info only for current user (private profile)
          Consumer<AuthProvider>(
            builder: (context, authProvider, _) {
              final currentUserId = authProvider.user?.uid;
              final isOwnProfile = currentUserId == user.uid;
              
              if (isOwnProfile) {
                return Column(
                  children: [
                    const SizedBox(height: 16),
                    const Divider(),
                    const SizedBox(height: 16),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Membership',
                          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                        ),
                        TextButton(
                          onPressed: () => context.push('/annual-subscriptions'),
                          child: const Text('Manage'),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    _buildSubscriptionCard(),
                  ],
                );
              }
              return const SizedBox.shrink();
            },
          ),
        ],
      ),
    );
  }

  Widget _buildInfoRow(IconData icon, String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          Icon(icon, size: 20, color: AppColors.textSecondary),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: const TextStyle(
                    fontSize: 12,
                    color: AppColors.textSecondary,
                  ),
                ),
                Text(
                  value,
                  style: const TextStyle(fontWeight: FontWeight.w500),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSkillsSection(user) {
    if (user.skills.isEmpty) return const SizedBox.shrink();

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Skills',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: user.skills
                .map<Widget>(
                  (skill) => Chip(
                    label: Text(skill),
                    backgroundColor: AppColors.primary.withOpacity(0.1),
                  ),
                )
                .toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildSocialLinks(user) {
    final socialLinks = user.socialLinks;
    if (socialLinks.isEmpty) return const SizedBox.shrink();

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Social Links',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),
          if (socialLinks['linkedin'] != null)
            _buildSocialLink(Icons.link, 'LinkedIn', socialLinks['linkedin']),
          if (socialLinks['twitter'] != null)
            _buildSocialLink(Icons.link, 'Twitter', socialLinks['twitter']),
          if (socialLinks['github'] != null)
            _buildSocialLink(Icons.link, 'GitHub', socialLinks['github']),
        ],
      ),
    );
  }

  Widget _buildSocialLink(IconData icon, String platform, String url) {
    return ListTile(
      leading: Icon(icon, color: AppColors.primary),
      title: Text(platform),
      subtitle: Text(url, style: const TextStyle(fontSize: 12)),
      contentPadding: EdgeInsets.zero,
    );
  }

  Widget _buildLogoutButton() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: OutlinedButton.icon(
        onPressed: _handleLogout,
        icon: const Icon(Icons.logout, color: AppColors.error),
        label: const Text('Logout', style: TextStyle(color: AppColors.error)),
        style: OutlinedButton.styleFrom(
          side: const BorderSide(color: AppColors.error),
          padding: const EdgeInsets.symmetric(vertical: 12),
          minimumSize: const Size(double.infinity, 48),
        ),
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

  Widget _buildSubscriptionCard() {
    // TODO: Get actual subscription status from user data or Firebase
    final hasActiveSubscription = true; // Example - replace with actual data
    final subscriptionType = 'Basic Alumni'; // Example - replace with actual data
    final expiryDate = '2024-12-31'; // Example - replace with actual data

    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
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
                    color: hasActiveSubscription 
                        ? AppColors.primary.withValues(alpha: 0.1)
                        : Colors.grey.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Icon(
                    hasActiveSubscription ? Icons.card_membership : Icons.card_membership_outlined,
                    color: hasActiveSubscription ? AppColors.primary : Colors.grey,
                    size: 24,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        hasActiveSubscription ? subscriptionType : 'No Active Subscription',
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                        ),
                      ),
                      Text(
                        hasActiveSubscription 
                            ? 'Expires: $expiryDate'
                            : 'Subscribe to unlock premium features',
                        style: TextStyle(
                          color: Colors.grey[600],
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: hasActiveSubscription ? Colors.green : Colors.orange,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    hasActiveSubscription ? 'ACTIVE' : 'INACTIVE',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 10,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
            if (hasActiveSubscription) ...[
              const SizedBox(height: 12),
              const Divider(),
              const SizedBox(height: 8),
              Row(
                children: [
                  Icon(Icons.check_circle, color: AppColors.primary, size: 16),
                  const SizedBox(width: 8),
                  const Expanded(
                    child: Text(
                      'Premium features unlocked',
                      style: TextStyle(fontSize: 12),
                    ),
                  ),
                ],
              ),
            ],
          ],
        ),
      ),
    );
  }
}
