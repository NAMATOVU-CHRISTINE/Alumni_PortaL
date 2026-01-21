import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/models/group_model.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/services/group_initialization_service.dart';

class AlumniGroupsScreen extends StatefulWidget {
  const AlumniGroupsScreen({super.key});

  @override
  State<AlumniGroupsScreen> createState() => _AlumniGroupsScreenState();
}

class _AlumniGroupsScreenState extends State<AlumniGroupsScreen> {
  bool _isAdmin = false;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _checkAdminStatus();
  }

  Future<void> _checkAdminStatus() async {
    final userId = FirebaseAuth.instance.currentUser?.uid;
    if (userId != null) {
      final isAdmin = await GroupInitializationService.isUserAdmin(userId);
      setState(() {
        _isAdmin = isAdmin;
        _isLoading = false;
      });
    } else {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Alumni Groups'),
        actions: [
          if (_isAdmin)
            IconButton(
              icon: const Icon(Icons.admin_panel_settings),
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Admin mode enabled')),
                );
              },
            ),
        ],
      ),
      floatingActionButton: _isAdmin
          ? FloatingActionButton(
              onPressed: () => context.push('/create-group'),
              child: const Icon(Icons.add),
            )
          : null,
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
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(
                    Icons.groups_outlined,
                    size: 64,
                    color: AppColors.textSecondary,
                  ),
                  const SizedBox(height: 16),
                  const Text(
                    'No groups found',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'Groups will appear here once created',
                    style: TextStyle(color: AppColors.textSecondary),
                  ),
                  if (_isAdmin) ...[
                    const SizedBox(height: 24),
                    ElevatedButton.icon(
                      onPressed: () async {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text('Initializing class groups...'),
                          ),
                        );
                        await GroupInitializationService
                            .initializeClassGroups();
                        if (mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('Class groups initialized!'),
                              backgroundColor: AppColors.success,
                            ),
                          );
                        }
                      },
                      icon: const Icon(Icons.auto_awesome),
                      label: const Text('Initialize Class Groups'),
                    ),
                  ],
                ],
              ),
            );
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
                    backgroundColor: AppColors.primary.withValues(alpha: 0.1),
                    child: Text(
                      group.groupTypeIcon,
                      style: const TextStyle(fontSize: 24),
                    ),
                  ),
                  title: Row(
                    children: [
                      Expanded(
                        child: Text(
                          group.groupName ?? 'Group',
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                      ),
                      if (group.isOfficial)
                        const Icon(
                          Icons.verified,
                          size: 16,
                          color: AppColors.primary,
                        ),
                    ],
                  ),
                  subtitle: Text(
                    '${group.memberCount} members • ${group.groupType ?? "General"}',
                  ),
                  trailing:
                      group.isPrivate ? const Icon(Icons.lock, size: 16) : null,
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
