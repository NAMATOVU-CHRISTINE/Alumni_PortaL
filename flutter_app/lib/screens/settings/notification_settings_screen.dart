import 'package:flutter/material.dart';
import 'package:alumni_portal/config/theme.dart';

class NotificationSettingsScreen extends StatefulWidget {
  const NotificationSettingsScreen({super.key});

  @override
  State<NotificationSettingsScreen> createState() =>
      _NotificationSettingsScreenState();
}

class _NotificationSettingsScreenState
    extends State<NotificationSettingsScreen> {
  bool _pushEnabled = true;
  bool _emailEnabled = true;
  bool _messageNotifications = true;
  bool _eventNotifications = true;
  bool _jobNotifications = true;
  bool _mentorshipNotifications = true;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Notification Settings')),
      body: ListView(
        children: [
          _buildSection('General', [
            SwitchListTile(
              title: const Text('Push Notifications'),
              subtitle: const Text('Receive push notifications'),
              value: _pushEnabled,
              onChanged: (v) => setState(() => _pushEnabled = v),
            ),
            SwitchListTile(
              title: const Text('Email Notifications'),
              subtitle: const Text('Receive email updates'),
              value: _emailEnabled,
              onChanged: (v) => setState(() => _emailEnabled = v),
            ),
          ]),
          _buildSection('Notification Types', [
            SwitchListTile(
              title: const Text('Messages'),
              subtitle: const Text('New chat messages'),
              value: _messageNotifications,
              onChanged: (v) => setState(() => _messageNotifications = v),
            ),
            SwitchListTile(
              title: const Text('Events'),
              subtitle: const Text('Event reminders and updates'),
              value: _eventNotifications,
              onChanged: (v) => setState(() => _eventNotifications = v),
            ),
            SwitchListTile(
              title: const Text('Jobs'),
              subtitle: const Text('New job postings'),
              value: _jobNotifications,
              onChanged: (v) => setState(() => _jobNotifications = v),
            ),
            SwitchListTile(
              title: const Text('Mentorship'),
              subtitle: const Text('Mentorship requests and updates'),
              value: _mentorshipNotifications,
              onChanged: (v) => setState(() => _mentorshipNotifications = v),
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
