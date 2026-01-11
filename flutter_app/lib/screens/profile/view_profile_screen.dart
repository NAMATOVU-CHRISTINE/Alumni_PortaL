import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/providers/chat_provider.dart';
import 'package:alumni_portal/models/user_model.dart';
import 'package:alumni_portal/config/theme.dart';

class ViewProfileScreen extends StatefulWidget {
  final String userId;

  const ViewProfileScreen({super.key, required this.userId});

  @override
  State<ViewProfileScreen> createState() => _ViewProfileScreenState();
}

class _ViewProfileScreenState extends State<ViewProfileScreen> {
  UserModel? _user;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadUser();
  }

  Future<void> _loadUser() async {
    final user = await context.read<UserProvider>().getUserById(widget.userId);
    if (mounted) {
      setState(() {
        _user = user;
        _isLoading = false;
      });
    }
  }

  Future<void> _startChat() async {
    if (_user == null) return;

    final chatProvider = context.read<ChatProvider>();
    final chatId = await chatProvider.createOrGetChat(
      widget.userId,
      _user!.fullName ?? 'User',
    );

    if (chatId != null && mounted) {
      context.push('/chat/$chatId', extra: _user!.fullName ?? 'Chat');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Profile')),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _user == null
          ? const Center(child: Text('User not found'))
          : SingleChildScrollView(
              child: Column(
                children: [
                  _buildProfileHeader(),
                  _buildProfileInfo(),
                  _buildSkillsSection(),
                  const SizedBox(height: 24),
                  _buildActionButtons(),
                  const SizedBox(height: 32),
                ],
              ),
            ),
    );
  }

  Widget _buildProfileHeader() {
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
            radius: 50,
            backgroundColor: Colors.white,
            backgroundImage: _user?.profileImageUrl != null
                ? CachedNetworkImageProvider(_user!.profileImageUrl!)
                : null,
            child: _user?.profileImageUrl == null
                ? Text(
                    _user?.fullName?.substring(0, 1).toUpperCase() ?? 'A',
                    style: const TextStyle(
                      fontSize: 40,
                      color: AppColors.primary,
                    ),
                  )
                : null,
          ),
          const SizedBox(height: 16),
          Text(
            _user?.fullName ?? 'Alumni User',
            style: const TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          if (_user?.username != null)
            Text(
              '@${_user!.username}',
              style: const TextStyle(color: Colors.white70),
            ),
          const SizedBox(height: 8),
          if (_user?.currentJob != null || _user?.company != null)
            Text(
              '${_user?.currentJob ?? ''} ${_user?.company != null ? "at ${_user!.company}" : ""}',
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
              _user?.userType.toUpperCase() ?? 'USER',
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

  Widget _buildProfileInfo() {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (_user?.bio != null && _user!.bio!.isNotEmpty) ...[
            const Text(
              'About',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            Text(
              _user!.bio!,
              style: const TextStyle(color: AppColors.textSecondary),
            ),
            const SizedBox(height: 24),
          ],
          const Text(
            'Details',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),
          if (_user?.major != null)
            _buildInfoRow(Icons.school, 'Major', _user!.major!),
          if (_user?.graduationYear != null)
            _buildInfoRow(
              Icons.calendar_today,
              'Graduation Year',
              _user!.graduationYear!,
            ),
          if (_user?.location != null)
            _buildInfoRow(Icons.location_on, 'Location', _user!.location!),
          if (_user?.workStatus != null)
            _buildInfoRow(Icons.work, 'Work Status', _user!.workStatus!),
          if (_user?.industry != null)
            _buildInfoRow(Icons.business, 'Industry', _user!.industry!),
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

  Widget _buildSkillsSection() {
    if (_user?.skills.isEmpty ?? true) return const SizedBox.shrink();

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
            children: _user!.skills
                .map(
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

  Widget _buildActionButtons() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Row(
        children: [
          Expanded(
            child: ElevatedButton.icon(
              onPressed: _startChat,
              icon: const Icon(Icons.chat),
              label: const Text('Message'),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: OutlinedButton.icon(
              onPressed: () {
                // TODO: Implement mentorship request
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Mentorship request feature coming soon'),
                  ),
                );
              },
              icon: const Icon(Icons.handshake),
              label: const Text('Connect'),
            ),
          ),
        ],
      ),
    );
  }
}
