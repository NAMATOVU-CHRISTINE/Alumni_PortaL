import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:alumni_portal/models/group_model.dart';
import 'package:alumni_portal/config/theme.dart';

class AlumniGroupsScreen extends StatelessWidget {
  const AlumniGroupsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Alumni Groups')),
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.push('/create-group'),
        child: const Icon(Icons.add),
      ),
      body: StreamBuilder<QuerySnapshot>(
        stream: FirebaseFirestore.instance
            .collection('groups')
            .orderBy('memberCount', descending: true)
            .snapshots(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (!snapshot.hasData || snapshot.data!.docs.isEmpty) {
            return const Center(child: Text('No groups found'));
          }
          final groups = snapshot.data!.docs
              .map((doc) => GroupModel.fromFirestore(doc))
              .toList();
          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: groups.length,
            itemBuilder: (context, index) {
              final group = groups[index];
              return Card(
                margin: const EdgeInsets.only(bottom: 12),
                child: ListTile(
                  leading: CircleAvatar(
                    backgroundColor: AppColors.primary.withOpacity(0.1),
                    child: Text(
                      group.groupTypeIcon,
                      style: const TextStyle(fontSize: 24),
                    ),
                  ),
                  title: Text(
                    group.groupName ?? 'Group',
                    style: const TextStyle(fontWeight: FontWeight.bold),
                  ),
                  subtitle: Text(
                    '${group.memberCount} members • ${group.groupType ?? "General"}',
                  ),
                  trailing: group.isPrivate
                      ? const Icon(Icons.lock, size: 16)
                      : null,
                  onTap: () => context.push('/group/${group.groupId}'),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
