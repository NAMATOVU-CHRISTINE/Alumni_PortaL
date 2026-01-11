import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/widgets/custom_text_field.dart';
import 'package:alumni_portal/widgets/loading_button.dart';

class CreateGroupScreen extends StatefulWidget {
  const CreateGroupScreen({super.key});

  @override
  State<CreateGroupScreen> createState() => _CreateGroupScreenState();
}

class _CreateGroupScreenState extends State<CreateGroupScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _descriptionController = TextEditingController();
  String _selectedType = 'interest';
  bool _isPrivate = false;
  bool _isLoading = false;

  Future<void> _createGroup() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _isLoading = true);
    try {
      final user = FirebaseAuth.instance.currentUser;
      final userDoc = await FirebaseFirestore.instance
          .collection('users')
          .doc(user?.uid)
          .get();
      final userName = userDoc.data()?['fullName'] ?? 'Anonymous';
      await FirebaseFirestore.instance.collection('groups').add({
        'groupName': _nameController.text.trim(),
        'description': _descriptionController.text.trim(),
        'groupType': _selectedType,
        'isPrivate': _isPrivate,
        'creatorId': user?.uid,
        'creatorName': userName,
        'memberIds': [user?.uid],
        'adminIds': [user?.uid],
        'memberCount': 1,
        'createdAt': DateTime.now().millisecondsSinceEpoch,
        'lastActivityAt': DateTime.now().millisecondsSinceEpoch,
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Group created'),
            backgroundColor: AppColors.success,
          ),
        );
        context.pop();
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Failed to create group'),
          backgroundColor: AppColors.error,
        ),
      );
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Create Group')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              CustomTextField(
                controller: _nameController,
                label: 'Group Name',
                prefixIcon: Icons.group,
                validator: (v) => v?.isEmpty == true ? 'Required' : null,
              ),
              const SizedBox(height: 16),
              CustomTextField(
                controller: _descriptionController,
                label: 'Description',
                prefixIcon: Icons.description,
                maxLines: 3,
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                initialValue: _selectedType,
                decoration: const InputDecoration(
                  labelText: 'Group Type',
                  prefixIcon: Icon(Icons.category),
                ),
                items: ['class', 'department', 'interest', 'location']
                    .map((t) => DropdownMenuItem(value: t, child: Text(t)))
                    .toList(),
                onChanged: (v) => setState(() => _selectedType = v!),
              ),
              const SizedBox(height: 16),
              SwitchListTile(
                title: const Text('Private Group'),
                value: _isPrivate,
                onChanged: (v) => setState(() => _isPrivate = v),
              ),
              const SizedBox(height: 32),
              LoadingButton(
                onPressed: _createGroup,
                isLoading: _isLoading,
                text: 'Create Group',
              ),
            ],
          ),
        ),
      ),
    );
  }
}
