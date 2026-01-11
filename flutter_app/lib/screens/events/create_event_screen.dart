import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/widgets/custom_text_field.dart';
import 'package:alumni_portal/widgets/loading_button.dart';

class CreateEventScreen extends StatefulWidget {
  const CreateEventScreen({super.key});

  @override
  State<CreateEventScreen> createState() => _CreateEventScreenState();
}

class _CreateEventScreenState extends State<CreateEventScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _locationController = TextEditingController();
  DateTime _selectedDate = DateTime.now().add(const Duration(days: 7));
  bool _isOnline = false;
  bool _isLoading = false;

  Future<void> _createEvent() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _isLoading = true);
    try {
      final user = FirebaseAuth.instance.currentUser;
      final userDoc = await FirebaseFirestore.instance
          .collection('users')
          .doc(user?.uid)
          .get();
      await FirebaseFirestore.instance.collection('events').add({
        'title': _titleController.text.trim(),
        'description': _descriptionController.text.trim(),
        'location': _locationController.text.trim(),
        'dateTime': _selectedDate.millisecondsSinceEpoch,
        'isOnline': _isOnline,
        'organizerName': userDoc.data()?['fullName'] ?? 'Anonymous',
        'maxParticipants': 50,
        'currentParticipants': 0,
        'createdAt': DateTime.now().millisecondsSinceEpoch,
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Event created'),
            backgroundColor: AppColors.success,
          ),
        );
        context.pop();
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Failed to create event'),
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
      appBar: AppBar(title: const Text('Create Event')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              CustomTextField(
                controller: _titleController,
                label: 'Event Title',
                prefixIcon: Icons.event,
                validator: (v) => v?.isEmpty == true ? 'Required' : null,
              ),
              const SizedBox(height: 16),
              CustomTextField(
                controller: _descriptionController,
                label: 'Description',
                prefixIcon: Icons.description,
                maxLines: 4,
              ),
              const SizedBox(height: 16),
              CustomTextField(
                controller: _locationController,
                label: 'Location',
                prefixIcon: Icons.location_on,
              ),
              const SizedBox(height: 16),
              ListTile(
                leading: const Icon(Icons.calendar_today),
                title: const Text('Date & Time'),
                subtitle: Text(
                  '${_selectedDate.day}/${_selectedDate.month}/${_selectedDate.year}',
                ),
                onTap: () async {
                  final date = await showDatePicker(
                    context: context,
                    initialDate: _selectedDate,
                    firstDate: DateTime.now(),
                    lastDate: DateTime.now().add(const Duration(days: 365)),
                  );
                  if (date != null) setState(() => _selectedDate = date);
                },
              ),
              SwitchListTile(
                title: const Text('Online Event'),
                value: _isOnline,
                onChanged: (v) => setState(() => _isOnline = v),
              ),
              const SizedBox(height: 32),
              LoadingButton(
                onPressed: _createEvent,
                isLoading: _isLoading,
                text: 'Create Event',
              ),
            ],
          ),
        ),
      ),
    );
  }
}
