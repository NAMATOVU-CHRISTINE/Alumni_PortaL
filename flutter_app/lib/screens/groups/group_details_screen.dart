import 'package:flutter/material.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/models/group_model.dart';
import 'package:alumni_portal/config/theme.dart';

class GroupDetailsScreen extends StatelessWidget {
  final String groupId;
  const GroupDetailsScreen({super.key, required this.groupId});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Group')),
      body: FutureBuilder<DocumentSnapshot>(
        future: FirebaseFirestore.instance
            .collection('groups')
            .doc(groupId)
            .get(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (!snapshot.hasData || !snapshot.data!.exists) {
            return const Center(child: Text('Group not found'));
          }
          final group = GroupModel.fromFirestore(snapshot.data!);
          final currentUserId = FirebaseAuth.instance.currentUser?.uid ?? '';
          final isMember = group.isMember(currentUserId);
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Center(
                  child: CircleAvatar(
                    radius: 50,
                    backgroundColor: AppColors.primary.withOpacity(0.1),
                    child: Text(
                      group.groupTypeIcon,
                      style: const TextStyle(fontSize: 48),
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                Center(
                  child: Text(
                    group.groupName ?? 'Group',
                    style: const TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                Center(
                  child: Text(
                    '${group.memberCount} members',
                    style: const TextStyle(color: AppColors.textSecondary),
                  ),
                ),
                const SizedBox(height: 24),
                if (group.description != null) ...[
                  const Text(
                    'About',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  Text(group.description!),
                ],
                const SizedBox(height: 24),
                SizedBox(
                  width: double.infinity,
                  child: isMember
                      ? OutlinedButton(
                          onPressed: () {},
                          child: const Text('Leave Group'),
                        )
                      : ElevatedButton(
                          onPressed: () {},
                          child: const Text('Join Group'),
                        ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
