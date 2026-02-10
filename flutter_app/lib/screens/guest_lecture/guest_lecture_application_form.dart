import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:image_picker/image_picker.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'dart:io';
import '../../providers/guest_lecture_provider.dart';
import '../../models/guest_lecture_application.dart';

class GuestLectureApplicationForm extends StatefulWidget {
  const GuestLectureApplicationForm({super.key});

  @override
  State<GuestLectureApplicationForm> createState() =>
      _GuestLectureApplicationFormState();
}

class _GuestLectureApplicationFormState
    extends State<GuestLectureApplicationForm> {
  final _formKey = GlobalKey<FormState>();
  final _speakerNameController = TextEditingController();
  final _speakerTitleController = TextEditingController();
  final _organizationController = TextEditingController();
  final _emailController = TextEditingController();
  final _phoneController = TextEditingController();
  final _bioController = TextEditingController();
  final _lectureTitleController = TextEditingController();
  final _lectureAbstractController = TextEditingController();
  final _targetAudienceController = TextEditingController();
  final _departmentController = TextEditingController();

  int _duration = 60;
  String _faculty = 'Computing and Informatics';
  final List<DateTime> _preferredDates = [];
  File? _cvFile;
  File? _presentationFile;
  bool _isSubmitting = false;

  final List<String> _faculties = [
    'Computing and Informatics',
    'Medicine',
    'Science',
    'Development Studies',
    'Applied Science and Technology',
  ];

  @override
  void dispose() {
    _speakerNameController.dispose();
    _speakerTitleController.dispose();
    _organizationController.dispose();
    _emailController.dispose();
    _phoneController.dispose();
    _bioController.dispose();
    _lectureTitleController.dispose();
    _lectureAbstractController.dispose();
    _targetAudienceController.dispose();
    _departmentController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Apply for Guest Lecture'),
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            _buildSectionTitle('Speaker Information'),
            _buildTextField(_speakerNameController, 'Full Name', Icons.person),
            _buildTextField(
                _speakerTitleController, 'Title/Position', Icons.work),
            _buildTextField(
                _organizationController, 'Organization', Icons.business),
            _buildTextField(_emailController, 'Email', Icons.email,
                keyboardType: TextInputType.emailAddress),
            _buildTextField(_phoneController, 'Phone', Icons.phone,
                keyboardType: TextInputType.phone),
            _buildTextField(_bioController, 'Biography', Icons.description,
                maxLines: 4),
            const SizedBox(height: 24),
            _buildSectionTitle('Lecture Details'),
            _buildTextField(
                _lectureTitleController, 'Lecture Title', Icons.title),
            _buildTextField(
                _lectureAbstractController, 'Abstract', Icons.article,
                maxLines: 5),
            _buildDurationSelector(),
            _buildFacultySelector(),
            _buildTextField(_departmentController, 'Department', Icons.school),
            _buildTextField(
                _targetAudienceController, 'Target Audience', Icons.group),
            const SizedBox(height: 24),
            _buildSectionTitle('Preferred Dates'),
            _buildDateSelector(),
            const SizedBox(height: 24),
            _buildSectionTitle('Documents'),
            _buildFileUpload('CV/Resume', _cvFile, () => _pickFile(true)),
            _buildFileUpload('Presentation Outline (Optional)',
                _presentationFile, () => _pickFile(false)),
            const SizedBox(height: 32),
            ElevatedButton(
              onPressed: _isSubmitting ? null : _submitApplication,
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16),
              ),
              child: _isSubmitting
                  ? const CircularProgressIndicator(color: Colors.white)
                  : const Text('Submit Application'),
            ),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: Text(
        title,
        style: const TextStyle(
          fontSize: 18,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }

  Widget _buildTextField(
    TextEditingController controller,
    String label,
    IconData icon, {
    int maxLines = 1,
    TextInputType keyboardType = TextInputType.text,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: TextFormField(
        controller: controller,
        decoration: InputDecoration(
          labelText: label,
          prefixIcon: Icon(icon),
        ),
        maxLines: maxLines,
        keyboardType: keyboardType,
        validator: (value) {
          if (value == null || value.isEmpty) {
            return 'Please enter $label';
          }
          return null;
        },
      ),
    );
  }

  Widget _buildDurationSelector() {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: DropdownButtonFormField<int>(
        initialValue: _duration,
        decoration: const InputDecoration(
          labelText: 'Duration (minutes)',
          prefixIcon: Icon(Icons.timer),
        ),
        items: [30, 45, 60, 90, 120].map((duration) {
          return DropdownMenuItem(
            value: duration,
            child: Text('$duration minutes'),
          );
        }).toList(),
        onChanged: (value) {
          if (value != null) {
            setState(() {
              _duration = value;
            });
          }
        },
      ),
    );
  }

  Widget _buildFacultySelector() {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: DropdownButtonFormField<String>(
        initialValue: _faculty,
        decoration: const InputDecoration(
          labelText: 'Faculty',
          prefixIcon: Icon(Icons.account_balance),
        ),
        items: _faculties.map((faculty) {
          return DropdownMenuItem(
            value: faculty,
            child: Text(faculty),
          );
        }).toList(),
        onChanged: (value) {
          if (value != null) {
            setState(() {
              _faculty = value;
            });
          }
        },
      ),
    );
  }

  Widget _buildDateSelector() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (_preferredDates.isEmpty)
              const Text('No dates selected',
                  style: TextStyle(color: Colors.grey)),
            ..._preferredDates.map((date) => ListTile(
                  leading: const Icon(Icons.calendar_today),
                  title: Text('${date.day}/${date.month}/${date.year}'),
                  trailing: IconButton(
                    icon: const Icon(Icons.delete),
                    onPressed: () {
                      setState(() {
                        _preferredDates.remove(date);
                      });
                    },
                  ),
                )),
            const SizedBox(height: 8),
            OutlinedButton.icon(
              onPressed: _addPreferredDate,
              icon: const Icon(Icons.add),
              label: const Text('Add Preferred Date'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFileUpload(String label, File? file, VoidCallback onTap) {
    return Card(
      child: ListTile(
        leading: const Icon(Icons.attach_file),
        title: Text(label),
        subtitle: file != null
            ? Text(file.path.split('/').last)
            : const Text('No file selected'),
        trailing: ElevatedButton(
          onPressed: onTap,
          child: const Text('Choose File'),
        ),
      ),
    );
  }

  Future<void> _addPreferredDate() async {
    final date = await showDatePicker(
      context: context,
      initialDate: DateTime.now().add(const Duration(days: 7)),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );

    if (date != null) {
      setState(() {
        _preferredDates.add(date);
      });
    }
  }

  Future<void> _pickFile(bool isCv) async {
    // In a real app, use file_picker package for PDF selection
    // For now, using image_picker as placeholder
    final ImagePicker picker = ImagePicker();
    final XFile? file = await picker.pickImage(source: ImageSource.gallery);

    if (file != null) {
      setState(() {
        if (isCv) {
          _cvFile = File(file.path);
        } else {
          _presentationFile = File(file.path);
        }
      });
    }
  }

  Future<void> _submitApplication() async {
    if (!_formKey.currentState!.validate()) return;
    if (_cvFile == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please upload your CV')),
      );
      return;
    }
    if (_preferredDates.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
            content: Text('Please select at least one preferred date')),
      );
      return;
    }

    setState(() {
      _isSubmitting = true;
    });

    try {
      // Upload files to Firebase Storage
      final cvUrl = await _uploadFile(_cvFile!, 'cv');
      final presentationUrl = _presentationFile != null
          ? await _uploadFile(_presentationFile!, 'presentation')
          : null;

      final application = GuestLectureApplication(
        id: '',
        applicantId: FirebaseAuth.instance.currentUser!.uid,
        speakerName: _speakerNameController.text,
        speakerTitle: _speakerTitleController.text,
        organization: _organizationController.text,
        email: _emailController.text,
        phone: _phoneController.text,
        bio: _bioController.text,
        lectureTitle: _lectureTitleController.text,
        lectureAbstract: _lectureAbstractController.text,
        duration: _duration,
        preferredDates: _preferredDates,
        targetAudience: _targetAudienceController.text,
        faculty: _faculty,
        department: _departmentController.text,
        cvUrl: cvUrl,
        presentationOutlineUrl: presentationUrl,
        status: ApplicationStatus.pending,
        submittedAt: DateTime.now(),
      );

      if (!mounted) return;

      final success = await context
          .read<GuestLectureProvider>()
          .submitApplication(application);

      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Application submitted successfully!')),
        );
        Navigator.pop(context);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: $e')),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isSubmitting = false;
        });
      }
    }
  }

  Future<String> _uploadFile(File file, String type) async {
    final userId = FirebaseAuth.instance.currentUser!.uid;
    final timestamp = DateTime.now().millisecondsSinceEpoch;
    final ref = FirebaseStorage.instance
        .ref()
        .child('guest_lectures/$userId/${type}_$timestamp');

    await ref.putFile(file);
    return await ref.getDownloadURL();
  }
}
