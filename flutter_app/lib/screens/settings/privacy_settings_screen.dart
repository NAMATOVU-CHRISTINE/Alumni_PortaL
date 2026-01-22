import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/config/theme.dart';

class PrivacySettingsScreen extends StatefulWidget {
  const PrivacySettingsScreen({super.key});

  @override
  State<PrivacySettingsScreen> createState() => _PrivacySettingsScreenState();
}

class _PrivacySettingsScreenState extends State<PrivacySettingsScreen> {
  bool _showEmail = false;
  bool _showPhone = false;
  bool _showLocation = true;
  bool _showCurrentJob = true;
  bool _allowMentorRequests = true;
  bool _showInDirectory = true;

  @override
  void initState() {
    super.initState();
    _loadSettings();
  }

  void _loadSettings() {
    final user = context.read<UserProvider>().currentUser;
    if (user != null) {
      setState(() {
        _showEmail = user.privacySettings['showEmail'] ?? false;
        _showPhone = user.privacySettings['showPhone'] ?? false;
        _showLocation = user.privacySettings['showLocation'] ?? true;
        _showCurrentJob = user.privacySettings['showCurrentJob'] ?? true;
        _allowMentorRequests =
            user.privacySettings['allowMentorRequests'] ?? true;
        _showInDirectory = user.privacySettings['showInDirectory'] ?? true;
      });
    }
  }

  Future<void> _saveSettings() async {
    final success = await context.read<UserProvider>().updatePrivacySettings({
      'showEmail': _showEmail,
      'showPhone': _showPhone,
      'showLocation': _showLocation,
      'showCurrentJob': _showCurrentJob,
      'allowMentorRequests': _allowMentorRequests,
      'showInDirectory': _showInDirectory,
    });
    if (success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Settings saved'),
          backgroundColor: AppColors.success,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Privacy Settings'),
        actions: [
          TextButton(onPressed: _saveSettings, child: const Text('Save')),
        ],
      ),
      body: ListView(
        children: [
          _buildSection('Profile Visibility', [
            SwitchListTile(
              title: const Text('Show Email'),
              subtitle: const Text('Allow others to see your email'),
              value: _showEmail,
              onChanged: (v) => setState(() => _showEmail = v),
            ),
            SwitchListTile(
              title: const Text('Show Phone'),
              subtitle: const Text('Allow others to see your phone'),
              value: _showPhone,
              onChanged: (v) => setState(() => _showPhone = v),
            ),
            SwitchListTile(
              title: const Text('Show Location'),
              subtitle: const Text('Display your location on profile'),
              value: _showLocation,
              onChanged: (v) => setState(() => _showLocation = v),
            ),
            SwitchListTile(
              title: const Text('Show Current Job'),
              subtitle: const Text('Display your job information'),
              value: _showCurrentJob,
              onChanged: (v) => setState(() => _showCurrentJob = v),
            ),
          ]),
          _buildSection('Directory & Mentorship', [
            SwitchListTile(
              title: const Text('Show in Directory'),
              subtitle: const Text('Appear in university network'),
              value: _showInDirectory,
              onChanged: (v) => setState(() => _showInDirectory = v),
            ),
            SwitchListTile(
              title: const Text('Allow Mentor Requests'),
              subtitle: const Text('Receive mentorship requests'),
              value: _allowMentorRequests,
              onChanged: (v) => setState(() => _allowMentorRequests = v),
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
}
