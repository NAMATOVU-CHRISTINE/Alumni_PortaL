import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/models/user_model.dart';

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
                    return _buildMentorCard(mentor);
                  },
                ),
    );
  }

  Widget _buildMentorCard(UserModel mentor) {
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header with profile image and basic info
            Row(
              children: [
                CircleAvatar(
                  radius: 30,
                  backgroundImage: mentor.profileImageUrl != null
                      ? CachedNetworkImageProvider(mentor.profileImageUrl!)
                      : null,
                  child: mentor.profileImageUrl == null
                      ? Text(
                          mentor.fullName?.substring(0, 1).toUpperCase() ?? 'M',
                          style: const TextStyle(
                              fontSize: 24, fontWeight: FontWeight.bold),
                        )
                      : null,
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        mentor.fullName ?? 'Mentor',
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                      ),
                      const SizedBox(height: 4),
                      if (mentor.currentJob != null)
                        Text(
                          mentor.currentJob!,
                          style:
                              Theme.of(context).textTheme.titleMedium?.copyWith(
                                    color: Theme.of(context).primaryColor,
                                    fontWeight: FontWeight.w600,
                                  ),
                        ),
                      if (mentor.company != null)
                        Text(
                          mentor.company!,
                          style:
                              Theme.of(context).textTheme.bodyMedium?.copyWith(
                                    color: Theme.of(context)
                                        .textTheme
                                        .bodySmall
                                        ?.color,
                                  ),
                        ),
                    ],
                  ),
                ),
              ],
            ),

            const SizedBox(height: 16),

            // Bio/About section
            if (mentor.bio != null && mentor.bio!.isNotEmpty) ...[
              Text(
                'About',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
              ),
              const SizedBox(height: 8),
              Text(
                mentor.bio!,
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      height: 1.4,
                    ),
                maxLines: 4,
                overflow: TextOverflow.ellipsis,
              ),
              const SizedBox(height: 16),
            ],

            // Skills/Expertise
            if (mentor.skills != null && mentor.skills!.isNotEmpty) ...[
              Text(
                'Expertise',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
              ),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                runSpacing: 4,
                children: mentor.skills!
                    .take(6)
                    .map((skill) => Chip(
                          label: Text(
                            skill,
                            style: const TextStyle(fontSize: 12),
                          ),
                          backgroundColor: Theme.of(context)
                              .primaryColor
                              .withValues(alpha: 0.1),
                          side: BorderSide(
                              color: Theme.of(context)
                                  .primaryColor
                                  .withValues(alpha: 0.3)),
                        ))
                    .toList(),
              ),
              const SizedBox(height: 16),
            ],

            // Education and Experience
            Row(
              children: [
                if (mentor.graduationYear != null) ...[
                  Icon(Icons.school,
                      size: 16,
                      color: Theme.of(context).textTheme.bodySmall?.color),
                  const SizedBox(width: 4),
                  Text(
                    'Class of ${mentor.graduationYear}',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                  const SizedBox(width: 16),
                ],
                if (mentor.major != null) ...[
                  Icon(Icons.book,
                      size: 16,
                      color: Theme.of(context).textTheme.bodySmall?.color),
                  const SizedBox(width: 4),
                  Flexible(
                    child: Text(
                      mentor.major!,
                      style: Theme.of(context).textTheme.bodySmall,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ],
            ),

            const SizedBox(height: 16),

            // Action buttons
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () =>
                        context.push('/view-profile/${mentor.userId}'),
                    icon: const Icon(Icons.person, size: 18),
                    label: const Text('View Profile'),
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () => _connectWithMentor(mentor),
                    icon: const Icon(Icons.message, size: 18),
                    label: const Text('Connect'),
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _connectWithMentor(UserModel mentor) {
    // Navigate to chat or send connection request
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Connection request sent to ${mentor.fullName}!'),
        backgroundColor: Theme.of(context).primaryColor,
      ),
    );
  }
}
