import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:alumni_portal/providers/auth_provider.dart';
import 'package:alumni_portal/config/theme.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        children: [
          _buildSection('Account', [
            _buildTile(
              context,
              Icons.person,
              'Edit Profile',
              () => context.push('/edit-profile'),
            ),
            _buildTile(
              context,
              Icons.lock,
              'Privacy Settings',
              () => context.push('/privacy-settings'),
            ),
            _buildTile(
              context,
              Icons.notifications,
              'Notification Settings',
              () => context.push('/notification-settings'),
            ),
          ]),
          _buildSection('Support', [
            _buildTile(context, Icons.help, 'Help & FAQ', () {}),
            _buildTile(context, Icons.feedback, 'Send Feedback', () {}),
            _buildTile(
              context,
              Icons.info,
              'About',
              () => _showAboutDialog(context),
            ),
          ]),
          _buildSection('Account Actions', [
            ListTile(
              leading: const Icon(Icons.logout, color: AppColors.error),
              title: const Text(
                'Logout',
                style: TextStyle(color: AppColors.error),
              ),
              onTap: () => _handleLogout(context),
            ),
          ]),
        ],
      ),
    );
  }

  Widget _buildSection(String title, List<Widget> children) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 24, 16, 8),
          child: Text(
            title,
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.bold,
              color: AppColors.textSecondary,
            ),
          ),
        ),
        ...children,
      ],
    );
  }

  Widget _buildTile(
    BuildContext context,
    IconData icon,
    String title,
    VoidCallback onTap,
  ) {
    return ListTile(
      leading: Icon(icon),
      title: Text(title),
      trailing: const Icon(Icons.chevron_right),
      onTap: onTap,
    );
  }

  void _showAboutDialog(BuildContext context) {
    showAboutDialog(
      context: context,
      applicationName: 'Alumni Portal',
      applicationVersion: '1.1.0',
      applicationIcon: const Icon(
        Icons.school,
        size: 48,
        color: AppColors.primary,
      ),
      children: [
        const Text(
          'Connect with fellow alumni, find mentors, and grow your career.',
        ),
      ],
    );
  }

  void _handleLogout(BuildContext context) {
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
              context.go('/login');
            },
            child: const Text('Logout'),
          ),
        ],
      ),
    );
  }
}
