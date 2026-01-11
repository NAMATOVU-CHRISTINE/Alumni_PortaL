import 'package:flutter/material.dart';
import 'package:alumni_portal/config/theme.dart';

class CareerTipsScreen extends StatelessWidget {
  const CareerTipsScreen({super.key});

  final List<Map<String, dynamic>> _tips = const [
    {
      'title': 'Build Your Network',
      'content':
          'Networking is key to career success. Connect with alumni, attend events, and maintain relationships.',
      'icon': Icons.handshake,
    },
    {
      'title': 'Keep Learning',
      'content':
          'Stay updated with industry trends. Take courses, read books, and never stop growing.',
      'icon': Icons.menu_book,
    },
    {
      'title': 'Personal Branding',
      'content':
          'Build a strong online presence. Update your LinkedIn, create a portfolio, and showcase your work.',
      'icon': Icons.star,
    },
    {
      'title': 'Seek Mentorship',
      'content':
          'Find mentors who can guide you. Learn from their experiences and avoid common mistakes.',
      'icon': Icons.school,
    },
    {
      'title': 'Set Clear Goals',
      'content':
          'Define your career goals. Create a roadmap and track your progress regularly.',
      'icon': Icons.flag,
    },
    {
      'title': 'Work-Life Balance',
      'content':
          'Maintain a healthy balance. Take breaks, exercise, and spend time with loved ones.',
      'icon': Icons.balance,
    },
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Career Tips')),
      body: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: _tips.length,
        itemBuilder: (context, index) {
          final tip = _tips[index];
          return Card(
            margin: const EdgeInsets.only(bottom: 16),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          color: AppColors.primary.withValues(alpha: 0.1),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Icon(tip['icon'] as IconData,
                            color: AppColors.primary, size: 28),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          tip['title'] as String,
                          style: const TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Text(
                    tip['content'] as String,
                    style: const TextStyle(color: AppColors.textSecondary),
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
