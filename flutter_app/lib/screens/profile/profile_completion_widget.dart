import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/config/theme.dart';

class ProfileCompletionWidget extends StatelessWidget {
  const ProfileCompletionWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<UserProvider>(
      builder: (context, provider, _) {
        final user = provider.currentUser;
        if (user == null) return const SizedBox.shrink();

        final completion = _calculateCompletion(user);
        if (completion >= 100) return const SizedBox.shrink();

        final missingFields = _getMissingFields(user);

        return Container(
          margin: const EdgeInsets.all(16),
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            gradient: LinearGradient(
              colors: [Colors.blue[50]!, Colors.blue[100]!],
            ),
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: Colors.blue[200]!),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  const Icon(Icons.person_outline, color: AppColors.primary),
                  const SizedBox(width: 8),
                  const Expanded(
                    child: Text(
                      'Complete Your Profile',
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                      ),
                    ),
                  ),
                  Text(
                    '$completion%',
                    style: const TextStyle(
                      fontWeight: FontWeight.bold,
                      color: AppColors.primary,
                      fontSize: 18,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: LinearProgressIndicator(
                  value: completion / 100,
                  minHeight: 8,
                  backgroundColor: Colors.white,
                  valueColor: AlwaysStoppedAnimation<Color>(
                    completion < 50
                        ? Colors.orange
                        : completion < 80
                            ? Colors.blue
                            : Colors.green,
                  ),
                ),
              ),
              const SizedBox(height: 12),
              Text(
                'Add ${missingFields.first} to improve your profile',
                style: TextStyle(color: Colors.grey[700], fontSize: 13),
              ),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: missingFields.take(3).map((field) {
                  return Chip(
                    label: Text(field, style: const TextStyle(fontSize: 11)),
                    backgroundColor: Colors.white,
                    visualDensity: VisualDensity.compact,
                    avatar: const Icon(Icons.add_circle_outline, size: 16),
                  );
                }).toList(),
              ),
              const SizedBox(height: 12),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () => context.push('/edit-profile'),
                  child: const Text('Complete Profile'),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  int _calculateCompletion(dynamic user) {
    int score = 0;
    int total = 10;

    if (user.fullName != null && user.fullName!.isNotEmpty) score++;
    if (user.email != null && user.email!.isNotEmpty) score++;
    if (user.profileImageUrl != null) score++;
    if (user.bio != null && user.bio!.isNotEmpty) score++;
    if (user.major != null && user.major!.isNotEmpty) score++;
    if (user.graduationYear != null && user.graduationYear!.isNotEmpty) score++;
    if (user.currentJob != null && user.currentJob!.isNotEmpty) score++;
    if (user.company != null && user.company!.isNotEmpty) score++;
    if (user.location != null && user.location!.isNotEmpty) score++;
    if (user.skills.isNotEmpty) score++;

    return ((score / total) * 100).round();
  }

  List<String> _getMissingFields(dynamic user) {
    final missing = <String>[];

    if (user.profileImageUrl == null) missing.add('Profile Photo');
    if (user.bio == null || user.bio!.isEmpty) missing.add('Bio');
    if (user.major == null || user.major!.isEmpty) missing.add('Major');
    if (user.graduationYear == null || user.graduationYear!.isEmpty)
      missing.add('Graduation Year');
    if (user.currentJob == null || user.currentJob!.isEmpty)
      missing.add('Current Job');
    if (user.company == null || user.company!.isEmpty) missing.add('Company');
    if (user.location == null || user.location!.isEmpty)
      missing.add('Location');
    if (user.skills.isEmpty) missing.add('Skills');

    return missing.isEmpty ? ['All complete!'] : missing;
  }
}
