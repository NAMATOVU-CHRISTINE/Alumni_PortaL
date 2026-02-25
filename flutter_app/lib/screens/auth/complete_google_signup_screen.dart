import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:alumni_portal/providers/auth_provider.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/widgets/custom_text_field.dart';
import 'package:alumni_portal/widgets/loading_button.dart';

class CompleteGoogleSignupScreen extends StatefulWidget {
  final String googleEmail;

  const CompleteGoogleSignupScreen({super.key, required this.googleEmail});

  @override
  State<CompleteGoogleSignupScreen> createState() =>
      _CompleteGoogleSignupScreenState();
}

class _CompleteGoogleSignupScreenState
    extends State<CompleteGoogleSignupScreen> {
  final _formKey = GlobalKey<FormState>();
  final _fullNameController = TextEditingController();
  final _usernameController = TextEditingController();
  final _studentIdController = TextEditingController();

  String _selectedUserType = 'Student';
  final List<String> _userTypes = ['Student', 'Alumni', 'Staff'];

  @override
  void dispose() {
    _fullNameController.dispose();
    _usernameController.dispose();
    _studentIdController.dispose();
    super.dispose();
  }

  String get _idLabel {
    switch (_selectedUserType) {
      case 'Alumni':
        return 'Alumni ID';
      case 'Staff':
        return 'Staff ID';
      default:
        return 'Student ID';
    }
  }

  Future<void> _handleComplete() async {
    if (!_formKey.currentState!.validate()) return;

    final authProvider = context.read<AuthProvider>();
    final success = await authProvider.completeGoogleSignup(
      fullName: _fullNameController.text.trim(),
      username: _usernameController.text.trim(),
      studentId: _studentIdController.text.trim(),
      userType: _selectedUserType,
    );

    if (!mounted) return;

    if (success) {
      context.go('/home');
    } else if (authProvider.error != null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(authProvider.error!),
          backgroundColor: AppColors.error,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Decorative lines at top
              Container(
                height: 4,
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    colors: [AppColors.accent, AppColors.secondary],
                  ),
                ),
              ),
              const SizedBox(height: 2),
              Container(
                height: 2,
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    colors: [AppColors.accent, AppColors.secondary],
                  ),
                ),
              ),
              
              Padding(
                padding: const EdgeInsets.all(24),
                child: Form(
                  key: _formKey,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      const SizedBox(height: 20),
                      
                      // Complete Profile heading in lime green
                      const Center(
                        child: Text(
                          'Complete Profile',
                          style: TextStyle(
                            fontSize: 28,
                            fontWeight: FontWeight.bold,
                            color: AppColors.primary,
                          ),
                        ),
                      ),
                      const SizedBox(height: 8),
                      const Center(
                        child: Text(
                          'Complete your profile to get started',
                          style: TextStyle(color: AppColors.textSecondary),
                        ),
                      ),
                      const SizedBox(height: 24),

                      // Show Google email
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: AppColors.primary.withOpacity(0.1),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Row(
                          children: [
                            const Icon(Icons.email, color: AppColors.primary),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text(
                                    'Signed in as',
                                    style: TextStyle(
                                      fontSize: 12,
                                      color: AppColors.textSecondary,
                                    ),
                                  ),
                                  Text(
                                    widget.googleEmail,
                                    style: const TextStyle(
                                      fontWeight: FontWeight.w500,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 24),

                      // Full Name
                      CustomTextField(
                        controller: _fullNameController,
                        label: 'Full Name',
                        hint: 'Enter your full name',
                        prefixIcon: Icons.person_outline,
                        validator: (value) {
                          if (value == null || value.isEmpty) {
                            return 'Full name is required';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 16),

                      // Username
                      CustomTextField(
                        controller: _usernameController,
                        label: 'Username',
                        hint: 'Choose a username',
                        prefixIcon: Icons.alternate_email,
                        validator: (value) {
                          if (value == null || value.isEmpty) {
                            return 'Username is required';
                          }
                          if (value.length < 3) {
                            return 'Username must be at least 3 characters';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 16),

                      // User Type Dropdown
                      DropdownButtonFormField<String>(
                        initialValue: _selectedUserType,
                        decoration: InputDecoration(
                          labelText: 'User Type',
                          prefixIcon: const Icon(Icons.badge_outlined),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(8),
                          ),
                        ),
                        items: _userTypes
                            .map(
                              (type) =>
                                  DropdownMenuItem(value: type, child: Text(type)),
                            )
                            .toList(),
                        onChanged: (value) {
                          if (value != null) {
                            setState(() => _selectedUserType = value);
                          }
                        },
                      ),
                      const SizedBox(height: 16),

                      // Student/Alumni/Staff ID
                      CustomTextField(
                        controller: _studentIdController,
                        label: _idLabel,
                        hint: 'Enter your $_idLabel',
                        prefixIcon: Icons.credit_card_outlined,
                        validator: (value) {
                          if (value == null || value.isEmpty) {
                            return '$_idLabel is required';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 32),

                      // Complete Button
                      Consumer<AuthProvider>(
                        builder: (context, auth, _) => LoadingButton(
                          onPressed: _handleComplete,
                          isLoading: auth.isLoading,
                          text: 'Complete Setup',
                        ),
                      ),
                      const SizedBox(height: 16),

                      // Sign out option
                      TextButton(
                        onPressed: () async {
                          await context.read<AuthProvider>().signOut();
                          if (mounted) context.go('/login');
                        },
                        child: const Text('Use a different account'),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
