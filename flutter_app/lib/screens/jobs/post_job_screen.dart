import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/widgets/custom_text_field.dart';
import 'package:alumni_portal/widgets/loading_button.dart';

class PostJobScreen extends StatefulWidget {
  const PostJobScreen({super.key});

  @override
  State<PostJobScreen> createState() => _PostJobScreenState();
}

class _PostJobScreenState extends State<PostJobScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _companyController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _requirementsController = TextEditingController();
  final _locationController = TextEditingController();
  final _salaryController = TextEditingController();
  final _applicationUrlController = TextEditingController();
  final _contactEmailController = TextEditingController();

  String _selectedJobType = 'full-time';
  String _selectedExperienceLevel = 'entry';
  bool _isRemote = false;
  bool _isLoading = false;

  @override
  void dispose() {
    _titleController.dispose();
    _companyController.dispose();
    _descriptionController.dispose();
    _requirementsController.dispose();
    _locationController.dispose();
    _salaryController.dispose();
    _applicationUrlController.dispose();
    _contactEmailController.dispose();
    super.dispose();
  }

  Future<void> _postJob() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    try {
      final user = FirebaseAuth.instance.currentUser;
      final userDoc = await FirebaseFirestore.instance
          .collection('users')
          .doc(user?.uid)
          .get();
      final userName = userDoc.data()?['fullName'] ?? 'Anonymous';

      await FirebaseFirestore.instance.collection('jobs').add({
        'title': _titleController.text.trim(),
        'company': _companyController.text.trim(),
        'description': _descriptionController.text.trim(),
        'requirements': _requirementsController.text.trim(),
        'location': _locationController.text.trim(),
        'salary': _salaryController.text.trim(),
        'jobType': _selectedJobType,
        'experienceLevel': _selectedExperienceLevel,
        'isRemote': _isRemote,
        'applicationUrl': _applicationUrlController.text.trim(),
        'contactEmail': _contactEmailController.text.trim(),
        'postedBy': user?.uid,
        'postedByName': userName,
        'postedAt': DateTime.now().millisecondsSinceEpoch,
        'expiresAt': DateTime.now()
            .add(const Duration(days: 90))
            .millisecondsSinceEpoch,
        'isActive': true,
        'viewCount': 0,
        'applicationCount': 0,
      });

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Job posted successfully'),
            backgroundColor: AppColors.success,
          ),
        );
        context.pop();
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Failed to post job'),
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
      appBar: AppBar(title: const Text('Post a Job')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              CustomTextField(
                controller: _titleController,
                label: 'Job Title',
                prefixIcon: Icons.work,
                validator: (v) => v?.isEmpty == true ? 'Required' : null,
              ),
              const SizedBox(height: 16),
              CustomTextField(
                controller: _companyController,
                label: 'Company',
                prefixIcon: Icons.business,
                validator: (v) => v?.isEmpty == true ? 'Required' : null,
              ),
              const SizedBox(height: 16),
              CustomTextField(
                controller: _descriptionController,
                label: 'Description',
                prefixIcon: Icons.description,
                maxLines: 4,
                validator: (v) => v?.isEmpty == true ? 'Required' : null,
              ),
              const SizedBox(height: 16),
              CustomTextField(
                controller: _requirementsController,
                label: 'Requirements',
                prefixIcon: Icons.checklist,
                maxLines: 3,
              ),
              const SizedBox(height: 16),
              CustomTextField(
                controller: _locationController,
                label: 'Location',
                prefixIcon: Icons.location_on,
              ),
              const SizedBox(height: 16),
              CustomTextField(
                controller: _salaryController,
                label: 'Salary Range',
                prefixIcon: Icons.attach_money,
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                initialValue: _selectedJobType,
                decoration: const InputDecoration(
                  labelText: 'Job Type',
                  prefixIcon: Icon(Icons.category),
                ),
                items: ['full-time', 'part-time', 'contract', 'internship']
                    .map((t) => DropdownMenuItem(value: t, child: Text(t)))
                    .toList(),
                onChanged: (v) => setState(() => _selectedJobType = v!),
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                initialValue: _selectedExperienceLevel,
                decoration: const InputDecoration(
                  labelText: 'Experience Level',
                  prefixIcon: Icon(Icons.trending_up),
                ),
                items: ['entry', 'mid', 'senior', 'executive']
                    .map((t) => DropdownMenuItem(value: t, child: Text(t)))
                    .toList(),
                onChanged: (v) => setState(() => _selectedExperienceLevel = v!),
              ),
              const SizedBox(height: 16),
              SwitchListTile(
                title: const Text('Remote Position'),
                value: _isRemote,
                onChanged: (v) => setState(() => _isRemote = v),
              ),
              const SizedBox(height: 16),
              CustomTextField(
                controller: _applicationUrlController,
                label: 'Application URL',
                prefixIcon: Icons.link,
              ),
              const SizedBox(height: 16),
              CustomTextField(
                controller: _contactEmailController,
                label: 'Contact Email',
                prefixIcon: Icons.email,
                keyboardType: TextInputType.emailAddress,
              ),
              const SizedBox(height: 32),
              LoadingButton(
                onPressed: _postJob,
                isLoading: _isLoading,
                text: 'Post Job',
              ),
            ],
          ),
        ),
      ),
    );
  }
}
