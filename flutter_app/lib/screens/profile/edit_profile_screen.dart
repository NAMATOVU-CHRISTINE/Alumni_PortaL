import 'dart:io';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:image_picker/image_picker.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/widgets/custom_text_field.dart';
import 'package:alumni_portal/widgets/loading_button.dart';

class EditProfileScreen extends StatefulWidget {
  const EditProfileScreen({super.key});

  @override
  State<EditProfileScreen> createState() => _EditProfileScreenState();
}

class _EditProfileScreenState extends State<EditProfileScreen> {
  final _formKey = GlobalKey<FormState>();
  final _fullNameController = TextEditingController();
  final _bioController = TextEditingController();
  final _majorController = TextEditingController();
  final _graduationYearController = TextEditingController();
  final _currentJobController = TextEditingController();
  final _companyController = TextEditingController();
  final _locationController = TextEditingController();
  final _phoneController = TextEditingController();
  final _skillsController = TextEditingController();
  final _linkedinController = TextEditingController();

  File? _selectedImage;
  String? _selectedWorkStatus;
  bool _isSaving = false;

  final List<String> _workStatuses = [
    'Employed',
    'Self-employed',
    'Not working',
    'Looking for a job',
    'Student',
  ];

  @override
  void initState() {
    super.initState();
    _loadUserData();
  }

  void _loadUserData() {
    final user = context.read<UserProvider>().currentUser;
    if (user != null) {
      _fullNameController.text = user.fullName ?? '';
      _bioController.text = user.bio ?? '';
      _majorController.text = user.major ?? '';
      _graduationYearController.text = user.graduationYear ?? '';
      _currentJobController.text = user.currentJob ?? '';
      _companyController.text = user.company ?? '';
      _locationController.text = user.location ?? '';
      _phoneController.text = user.phoneNumber ?? '';
      _skillsController.text = user.skillsAsString;
      _linkedinController.text = user.socialLinks['linkedin'] ?? '';
      // Only set work status if it's in the list
      if (user.workStatus != null && _workStatuses.contains(user.workStatus)) {
        _selectedWorkStatus = user.workStatus;
      }
    }
  }

  @override
  void dispose() {
    _fullNameController.dispose();
    _bioController.dispose();
    _majorController.dispose();
    _graduationYearController.dispose();
    _currentJobController.dispose();
    _companyController.dispose();
    _locationController.dispose();
    _phoneController.dispose();
    _skillsController.dispose();
    _linkedinController.dispose();
    super.dispose();
  }

  Future<void> _pickImage() async {
    try {
      final picker = ImagePicker();
      final pickedFile = await picker.pickImage(
        source: ImageSource.gallery,
        maxWidth: 1024,
        maxHeight: 1024,
        imageQuality: 85,
      );
      
      if (pickedFile != null) {
        final file = File(pickedFile.path);
        
        // Check if file exists
        if (await file.exists()) {
          print('Image selected: ${pickedFile.path}');
          print('File size: ${await file.length()} bytes');
          setState(() => _selectedImage = file);
        } else {
          print('Error: Selected file does not exist');
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('Failed to load selected image'),
                backgroundColor: Colors.red,
              ),
            );
          }
        }
      } else {
        print('No image selected');
      }
    } catch (e) {
      print('Error picking image: $e');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error selecting image: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  Future<void> _saveProfile() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isSaving = true);

    try {
      final userProvider = context.read<UserProvider>();

      // Upload image if selected
      if (_selectedImage != null) {
        final imageUrl = await userProvider.uploadProfileImage(_selectedImage!);
        if (imageUrl == null && mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(userProvider.error ?? 'Failed to upload profile picture'),
              backgroundColor: Colors.red,
            ),
          );
          setState(() => _isSaving = false);
          return;
        }
      }

      // Parse skills
      final skills = _skillsController.text
          .split(',')
          .map((s) => s.trim())
          .where((s) => s.isNotEmpty)
          .toList();

      // Update profile
      final success = await userProvider.updateProfile({
        'fullName': _fullNameController.text.trim(),
        'bio': _bioController.text.trim(),
        'major': _majorController.text.trim(),
        'graduationYear': _graduationYearController.text.trim(),
        'currentJob': _currentJobController.text.trim(),
        'company': _companyController.text.trim(),
        'location': _locationController.text.trim(),
        'phoneNumber': _phoneController.text.trim(),
        'skills': skills,
        'workStatus': _selectedWorkStatus,
        'socialLinks': {'linkedin': _linkedinController.text.trim()},
      });

      if (success && mounted) {
        // Reload user to ensure profile picture is updated
        await userProvider.loadCurrentUser();
        
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Profile updated successfully'),
              backgroundColor: AppColors.success,
            ),
          );
          context.pop();
        }
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Failed to update profile'),
          backgroundColor: AppColors.error,
        ),
      );
    } finally {
      if (mounted) setState(() => _isSaving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Edit Profile')),
      body: Consumer<UserProvider>(
        builder: (context, provider, _) {
          final user = provider.currentUser;

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Form(
              key: _formKey,
              child: Column(
                children: [
                  // Profile Image
                  GestureDetector(
                    onTap: _isSaving ? null : _pickImage,
                    child: Stack(
                      children: [
                        CircleAvatar(
                          radius: 50,
                          backgroundColor: AppColors.primary.withAlpha(25),
                          backgroundImage: _selectedImage != null
                              ? FileImage(_selectedImage!) as ImageProvider
                              : (user?.profileImageUrl != null
                                  ? CachedNetworkImageProvider(
                                      user!.profileImageUrl!,
                                    ) as ImageProvider
                                  : null),
                          child: _selectedImage == null &&
                                  user?.profileImageUrl == null
                              ? const Icon(
                                  Icons.person,
                                  size: 50,
                                  color: AppColors.primary,
                                )
                              : null,
                        ),
                        if (_selectedImage != null)
                          Positioned(
                            top: 0,
                            right: 0,
                            child: Container(
                              padding: const EdgeInsets.all(4),
                              decoration: const BoxDecoration(
                                color: Colors.green,
                                shape: BoxShape.circle,
                              ),
                              child: const Icon(
                                Icons.check,
                                color: Colors.white,
                                size: 16,
                              ),
                            ),
                          ),
                        Positioned(
                          bottom: 0,
                          right: 0,
                          child: Container(
                            padding: const EdgeInsets.all(4),
                            decoration: const BoxDecoration(
                              color: AppColors.primary,
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(
                              Icons.camera_alt,
                              size: 20,
                              color: Colors.white,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),

                  CustomTextField(
                    controller: _fullNameController,
                    label: 'Full Name',
                    prefixIcon: Icons.person_outline,
                    validator: (v) => v?.isEmpty == true ? 'Required' : null,
                  ),
                  const SizedBox(height: 16),

                  CustomTextField(
                    controller: _bioController,
                    label: 'Bio',
                    prefixIcon: Icons.info_outline,
                    maxLines: 3,
                  ),
                  const SizedBox(height: 16),

                  CustomTextField(
                    controller: _majorController,
                    label: 'Major/Field of Study',
                    prefixIcon: Icons.school_outlined,
                  ),
                  const SizedBox(height: 16),

                  CustomTextField(
                    controller: _graduationYearController,
                    label: 'Graduation Year',
                    prefixIcon: Icons.calendar_today_outlined,
                    keyboardType: TextInputType.number,
                  ),
                  const SizedBox(height: 16),

                  DropdownButtonFormField<String>(
                    initialValue: _selectedWorkStatus,
                    decoration: InputDecoration(
                      labelText: 'Work Status',
                      prefixIcon: const Icon(Icons.work_outline),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(8),
                      ),
                    ),
                    items: _workStatuses
                        .map(
                          (status) => DropdownMenuItem(
                            value: status,
                            child: Text(status),
                          ),
                        )
                        .toList(),
                    onChanged: (value) =>
                        setState(() => _selectedWorkStatus = value),
                  ),
                  const SizedBox(height: 16),

                  CustomTextField(
                    controller: _currentJobController,
                    label: 'Current Job Title',
                    prefixIcon: Icons.badge_outlined,
                  ),
                  const SizedBox(height: 16),

                  CustomTextField(
                    controller: _companyController,
                    label: 'Company',
                    prefixIcon: Icons.business_outlined,
                  ),
                  const SizedBox(height: 16),

                  CustomTextField(
                    controller: _locationController,
                    label: 'Location',
                    prefixIcon: Icons.location_on_outlined,
                  ),
                  const SizedBox(height: 16),

                  CustomTextField(
                    controller: _phoneController,
                    label: 'Phone Number',
                    prefixIcon: Icons.phone_outlined,
                    keyboardType: TextInputType.phone,
                  ),
                  const SizedBox(height: 16),

                  CustomTextField(
                    controller: _skillsController,
                    label: 'Skills (comma separated)',
                    prefixIcon: Icons.psychology_outlined,
                    hint: 'e.g., Python, Leadership, Marketing',
                  ),
                  const SizedBox(height: 16),

                  CustomTextField(
                    controller: _linkedinController,
                    label: 'LinkedIn URL',
                    prefixIcon: Icons.link,
                  ),
                  const SizedBox(height: 32),

                  LoadingButton(
                    onPressed: _saveProfile,
                    isLoading: _isSaving,
                    text: 'Save Changes',
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
