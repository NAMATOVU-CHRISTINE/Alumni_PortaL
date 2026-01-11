import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/models/user_model.dart';
import 'package:alumni_portal/config/theme.dart';

class MentorSearchScreen extends StatefulWidget {
  const MentorSearchScreen({super.key});

  @override
  State<MentorSearchScreen> createState() => _MentorSearchScreenState();
}

class _MentorSearchScreenState extends State<MentorSearchScreen> {
  List<UserModel> _mentors = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadMentors();
  }

  Future<void> _loadMentors() async {
    final mentors = await context.read<UserProvider>().searchMentors();
    if (mounted) {
      setState(() {
        _mentors = mentors;
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Find a Mentor')),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _mentors.isEmpty
          ? const Center(child: Text('No mentors available'))
          : ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: _mentors.length,
              itemBuilder: (context, index) {
                final mentor = _mentors[index];
                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  child: ListTile(
                    leading: CircleAvatar(
                      backgroundImage: mentor.profileImageUrl != null
                          ? CachedNetworkImageProvider(mentor.profileImageUrl!)
                          : null,
                      child: mentor.profileImageUrl == null
                          ? Text(mentor.fullName?.substring(0, 1) ?? 'M')
                          : null,
                    ),
                    title: Text(mentor.fullName ?? 'Mentor'),
                    subtitle: Text(
                      '${mentor.currentJob ?? ''} ${mentor.company != null ? "at ${mentor.company}" : ""}',
                    ),
                    trailing: ElevatedButton(
                      onPressed: () =>
                          context.push('/view-profile/${mentor.userId}'),
                      child: const Text('View'),
                    ),
                  ),
                );
              },
            ),
    );
  }
}
