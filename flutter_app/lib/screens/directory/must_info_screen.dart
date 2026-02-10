import 'package:flutter/material.dart';
import '../../config/theme.dart';
import '../../widgets/must_branding_header.dart';

class MustInfoScreen extends StatelessWidget {
  const MustInfoScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: CustomScrollView(
        slivers: [
          SliverAppBar(
            expandedHeight: 200,
            pinned: true,
            flexibleSpace: FlexibleSpaceBar(
              title: const Text(
                'About MUST',
                style: TextStyle(
                  fontWeight: FontWeight.bold,
                  shadows: [Shadow(blurRadius: 4, color: Colors.black45)],
                ),
              ),
              background: Container(
                decoration: const BoxDecoration(
                  gradient: AppColors.mustGradient,
                ),
                child: Stack(
                  children: [
                    Positioned(
                      right: -50,
                      top: -50,
                      child: Icon(
                        Icons.school,
                        size: 200,
                        color: Colors.white.withValues(alpha: 0.1),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildUniversityOverview(context),
                  const SizedBox(height: 20),
                  _buildMissionVision(context),
                  const SizedBox(height: 20),
                  _buildFaculties(context),
                  const SizedBox(height: 20),
                  _buildCampusInfo(context),
                  const SizedBox(height: 20),
                  _buildAchievements(context),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildUniversityOverview(BuildContext context) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    gradient: AppColors.mustGradient,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Icon(
                    Icons.account_balance,
                    color: Colors.white,
                    size: 28,
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Text(
                    'University Overview',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Text(
              'Mbarara University of Science and Technology (MUST) is a public university in Uganda. '
              'Established in 1989, MUST has grown to become one of the leading institutions of higher learning '
              'in East Africa, known for excellence in science, technology, and health sciences.',
              style: TextStyle(
                fontSize: 14,
                color: AppColors.textSecondary,
                height: 1.5,
              ),
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                _buildInfoChip('Founded: 1989', Icons.calendar_today),
                _buildInfoChip('Location: Mbarara, Uganda', Icons.location_on),
                _buildInfoChip('Type: Public University', Icons.school),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMissionVision(BuildContext context) {
    return Column(
      children: [
        MustInfoCard(
          title: 'Our Mission',
          description:
              'To provide quality and relevant education through training, research, and community service.',
          icon: Icons.flag,
        ),
        const SizedBox(height: 12),
        MustInfoCard(
          title: 'Our Vision',
          description:
              'To be a center of academic and professional excellence in Science and Technology.',
          icon: Icons.visibility,
        ),
      ],
    );
  }

  Widget _buildFaculties(BuildContext context) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Faculties & Schools',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            const SizedBox(height: 16),
            _buildFacultyItem(
              'Faculty of Medicine',
              'Medicine, Nursing, Pharmacy, Medical Laboratory Sciences',
              Icons.local_hospital,
            ),
            _buildFacultyItem(
              'Faculty of Science',
              'Biology, Chemistry, Physics, Mathematics, Computer Science',
              Icons.science,
            ),
            _buildFacultyItem(
              'Faculty of Development Studies',
              'Business, Economics, Social Work, Development Studies',
              Icons.business,
            ),
            _buildFacultyItem(
              'Faculty of Applied Science & Technology',
              'Engineering, Technology, Environmental Sciences',
              Icons.engineering,
            ),
            _buildFacultyItem(
              'Faculty of Interdisciplinary Studies',
              'Education, Arts, Humanities',
              Icons.menu_book,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFacultyItem(String name, String departments, IconData icon) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: AppColors.primary.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Icon(icon, color: AppColors.primary, size: 24),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  name,
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  departments,
                  style: TextStyle(
                    fontSize: 13,
                    color: AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCampusInfo(BuildContext context) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Campus Information',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            const SizedBox(height: 16),
            _buildCampusDetail(
              'Main Campus',
              'Kihumuro Hill, Mbarara City',
              Icons.location_city,
            ),
            _buildCampusDetail(
              'Library',
              'Modern library with extensive collection',
              Icons.local_library,
            ),
            _buildCampusDetail(
              'Research Centers',
              'Multiple research facilities and labs',
              Icons.biotech,
            ),
            _buildCampusDetail(
              'Student Facilities',
              'Hostels, sports complex, cafeterias',
              Icons.sports_soccer,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCampusDetail(String title, String description, IconData icon) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        children: [
          Icon(icon, color: AppColors.primary, size: 24),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  description,
                  style: TextStyle(
                    fontSize: 12,
                    color: AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAchievements(BuildContext context) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          gradient: LinearGradient(
            colors: [
              AppColors.primary.withValues(alpha: 0.05),
              AppColors.accent.withValues(alpha: 0.05),
            ],
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Key Achievements',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            const SizedBox(height: 16),
            _buildAchievementItem(
              'Leading Medical School',
              'One of the top medical schools in East Africa',
              Icons.emoji_events,
            ),
            _buildAchievementItem(
              'Research Excellence',
              'Recognized for groundbreaking research in health sciences',
              Icons.science,
            ),
            _buildAchievementItem(
              'Community Impact',
              'Strong community outreach and service programs',
              Icons.people,
            ),
            _buildAchievementItem(
              'International Partnerships',
              'Collaborations with universities worldwide',
              Icons.public,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAchievementItem(
      String title, String description, IconData icon) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              gradient: AppColors.mustAccentGradient,
              borderRadius: BorderRadius.circular(8),
            ),
            child: Icon(icon, color: Colors.white, size: 20),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Text(
                  description,
                  style: TextStyle(
                    fontSize: 12,
                    color: AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildInfoChip(String label, IconData icon) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: AppColors.primary.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.primary.withValues(alpha: 0.3)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 16, color: AppColors.primary),
          const SizedBox(width: 6),
          Text(
            label,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: AppColors.primary,
            ),
          ),
        ],
      ),
    );
  }
}
