import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/guest_lecture_provider.dart';
import '../../config/theme.dart';
import 'guest_lecture_application_form.dart';
import 'my_applications_screen.dart';
import 'scheduled_lectures_screen.dart';

class EnhancedGuestLectureScreen extends StatefulWidget {
  const EnhancedGuestLectureScreen({super.key});

  @override
  State<EnhancedGuestLectureScreen> createState() =>
      _EnhancedGuestLectureScreenState();
}

class _EnhancedGuestLectureScreenState
    extends State<EnhancedGuestLectureScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        context.read<GuestLectureProvider>().fetchScheduledLectures();
        context.read<GuestLectureProvider>().fetchMyApplications();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: CustomScrollView(
        slivers: [
          _buildAppBar(),
          SliverToBoxAdapter(child: _buildHeroSection()),
          SliverToBoxAdapter(child: _buildStatisticsCards()),
          SliverToBoxAdapter(child: _buildQuickActions()),
          SliverToBoxAdapter(child: _buildUpcomingLectures()),
          SliverToBoxAdapter(child: _buildMyApplicationsSection()),
          const SliverToBoxAdapter(child: SizedBox(height: 80)),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _navigateToApplicationForm(),
        icon: const Icon(Icons.mic),
        label: const Text('Apply to Speak'),
        backgroundColor: AppColors.primary,
      ),
    );
  }

  Widget _buildAppBar() {
    return SliverAppBar(
      expandedHeight: 200,
      pinned: true,
      flexibleSpace: FlexibleSpaceBar(
        title: const Text(
          'Guest Lectures',
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
                right: -40,
                top: -40,
                child: Icon(
                  Icons.campaign,
                  size: 180,
                  color: Colors.white.withValues(alpha: 0.1),
                ),
              ),
              Positioned(
                left: -20,
                bottom: -20,
                child: Icon(
                  Icons.lightbulb,
                  size: 120,
                  color: AppColors.accent.withValues(alpha: 0.2),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeroSection() {
    return Container(
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.primary.withValues(alpha: 0.1),
            AppColors.accent.withValues(alpha: 0.1),
          ],
        ),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.primary.withValues(alpha: 0.3)),
      ),
      child: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: AppColors.primary.withValues(alpha: 0.1),
              shape: BoxShape.circle,
            ),
            child: Icon(
              Icons.school,
              size: 48,
              color: AppColors.primary,
            ),
          ),
          const SizedBox(height: 16),
          Text(
            'Share Your Expertise',
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: AppColors.primary,
                ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 8),
          Text(
            'Inspire the next generation of MUST students with your knowledge and experience',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: AppColors.textSecondary,
                ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildStatisticsCards() {
    return Consumer<GuestLectureProvider>(
      builder: (context, provider, child) {
        final totalLectures = provider.scheduledLectures.length;
        final myApplications = provider.myApplications.length;
        final approvedApps = provider.myApplications
            .where((app) => app.status.toString().contains('approved'))
            .length;

        return Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Row(
            children: [
              Expanded(
                child: _buildStatCard(
                  icon: Icons.event,
                  count: totalLectures.toString(),
                  label: 'Scheduled',
                  color: AppColors.primary,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _buildStatCard(
                  icon: Icons.assignment,
                  count: myApplications.toString(),
                  label: 'My Apps',
                  color: AppColors.secondary,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _buildStatCard(
                  icon: Icons.check_circle,
                  count: approvedApps.toString(),
                  label: 'Approved',
                  color: AppColors.success,
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildStatCard({
    required IconData icon,
    required String count,
    required String label,
    required Color color,
  }) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          gradient: LinearGradient(
            colors: [
              color.withValues(alpha: 0.1),
              color.withValues(alpha: 0.05),
            ],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: Column(
          children: [
            Icon(icon, color: color, size: 32),
            const SizedBox(height: 8),
            Text(
              count,
              style: TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
                color: color,
              ),
            ),
            Text(
              label,
              style: TextStyle(
                fontSize: 12,
                color: AppColors.textSecondary,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildQuickActions() {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Quick Actions',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: _buildActionCard(
                  icon: Icons.add_circle,
                  title: 'Apply Now',
                  subtitle: 'Submit application',
                  gradient: AppColors.mustGradient,
                  onTap: _navigateToApplicationForm,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _buildActionCard(
                  icon: Icons.calendar_today,
                  title: 'View Schedule',
                  subtitle: 'Upcoming lectures',
                  gradient: AppColors.mustAccentGradient,
                  onTap: _navigateToScheduledLectures,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: _buildActionCard(
                  icon: Icons.track_changes,
                  title: 'Track Status',
                  subtitle: 'My applications',
                  gradient: const LinearGradient(
                    colors: [AppColors.secondary, AppColors.success],
                  ),
                  onTap: _navigateToMyApplications,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _buildActionCard(
                  icon: Icons.info_outline,
                  title: 'Guidelines',
                  subtitle: 'Application tips',
                  gradient: LinearGradient(
                    colors: [
                      AppColors.warning,
                      AppColors.warning.withValues(alpha: 0.7),
                    ],
                  ),
                  onTap: _showGuidelines,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildActionCard({
    required IconData icon,
    required String title,
    required String subtitle,
    required Gradient gradient,
    required VoidCallback onTap,
  }) {
    return Card(
      elevation: 3,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            gradient: gradient,
            borderRadius: BorderRadius.circular(16),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Icon(icon, color: Colors.white, size: 32),
              const SizedBox(height: 12),
              Text(
                title,
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                subtitle,
                style: TextStyle(
                  fontSize: 12,
                  color: Colors.white.withValues(alpha: 0.9),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildUpcomingLectures() {
    return Consumer<GuestLectureProvider>(
      builder: (context, provider, child) {
        if (provider.scheduledLectures.isEmpty) {
          return const SizedBox.shrink();
        }

        return Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Upcoming Lectures',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                  TextButton.icon(
                    onPressed: _navigateToScheduledLectures,
                    icon: const Icon(Icons.arrow_forward, size: 16),
                    label: const Text('View All'),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              ...provider.scheduledLectures.take(3).map((lecture) {
                return _buildLectureCard(lecture);
              }),
            ],
          ),
        );
      },
    );
  }

  Widget _buildLectureCard(dynamic lecture) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: InkWell(
        onTap: () {
          // Navigate to lecture details
        },
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  gradient: AppColors.mustGradient,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(
                  Icons.event,
                  color: Colors.white,
                  size: 28,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      lecture.lectureTitle,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      lecture.speakerName,
                      style: TextStyle(
                        fontSize: 14,
                        color: AppColors.primary,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Row(
                      children: [
                        Icon(
                          Icons.calendar_today,
                          size: 14,
                          color: AppColors.textSecondary,
                        ),
                        const SizedBox(width: 4),
                        Text(
                          _formatDate(lecture.scheduledDate!),
                          style: TextStyle(
                            fontSize: 12,
                            color: AppColors.textSecondary,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              Icon(
                Icons.arrow_forward_ios,
                color: AppColors.primary,
                size: 16,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildMyApplicationsSection() {
    return Consumer<GuestLectureProvider>(
      builder: (context, provider, child) {
        if (provider.myApplications.isEmpty) {
          return _buildEmptyApplicationsState();
        }

        return Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'My Applications',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                  TextButton.icon(
                    onPressed: _navigateToMyApplications,
                    icon: const Icon(Icons.arrow_forward, size: 16),
                    label: const Text('View All'),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              ...provider.myApplications.take(3).map((application) {
                return _buildApplicationCard(application);
              }),
            ],
          ),
        );
      },
    );
  }

  Widget _buildApplicationCard(dynamic application) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: InkWell(
        onTap: () {
          // Navigate to application details
        },
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              _buildStatusIcon(application.status),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      application.lectureTitle,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 4),
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 8,
                        vertical: 4,
                      ),
                      decoration: BoxDecoration(
                        color: _getStatusColor(application.status)
                            .withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        application.statusDisplay,
                        style: TextStyle(
                          fontSize: 12,
                          color: _getStatusColor(application.status),
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              Icon(
                Icons.arrow_forward_ios,
                color: AppColors.primary,
                size: 16,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStatusIcon(status) {
    IconData icon;
    Color color;

    switch (status.toString()) {
      case 'ApplicationStatus.approved':
        icon = Icons.check_circle;
        color = AppColors.success;
        break;
      case 'ApplicationStatus.rejected':
        icon = Icons.cancel;
        color = AppColors.error;
        break;
      case 'ApplicationStatus.scheduled':
        icon = Icons.event_available;
        color = Colors.blue;
        break;
      default:
        icon = Icons.pending;
        color = AppColors.warning;
    }

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Icon(icon, color: color, size: 24),
    );
  }

  Color _getStatusColor(status) {
    switch (status.toString()) {
      case 'ApplicationStatus.approved':
        return AppColors.success;
      case 'ApplicationStatus.rejected':
        return AppColors.error;
      case 'ApplicationStatus.scheduled':
        return Colors.blue;
      default:
        return AppColors.warning;
    }
  }

  Widget _buildEmptyApplicationsState() {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Card(
        elevation: 2,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        child: Container(
          padding: const EdgeInsets.all(32),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(20),
            gradient: LinearGradient(
              colors: [
                AppColors.primary.withValues(alpha: 0.05),
                AppColors.accent.withValues(alpha: 0.05),
              ],
            ),
          ),
          child: Column(
            children: [
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: AppColors.primary.withValues(alpha: 0.1),
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  Icons.lightbulb_outline,
                  size: 64,
                  color: AppColors.primary,
                ),
              ),
              const SizedBox(height: 20),
              Text(
                'Share Your Knowledge',
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
              ),
              const SizedBox(height: 8),
              Text(
                'Apply to give a guest lecture and inspire the next generation of MUST professionals.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: AppColors.textSecondary,
                  fontSize: 14,
                ),
              ),
              const SizedBox(height: 20),
              ElevatedButton.icon(
                onPressed: _navigateToApplicationForm,
                icon: const Icon(Icons.add),
                label: const Text('Apply Now'),
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 32,
                    vertical: 16,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _formatDate(DateTime date) {
    final months = [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec'
    ];
    return '${date.day} ${months[date.month - 1]} ${date.year}';
  }

  void _navigateToApplicationForm() {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => const GuestLectureApplicationForm(),
      ),
    );
  }

  void _navigateToScheduledLectures() {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => const ScheduledLecturesScreen(),
      ),
    );
  }

  void _navigateToMyApplications() {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => const MyApplicationsScreen(),
      ),
    );
  }

  void _showGuidelines() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => DraggableScrollableSheet(
        initialChildSize: 0.7,
        maxChildSize: 0.9,
        minChildSize: 0.5,
        expand: false,
        builder: (context, scrollController) => Container(
          padding: const EdgeInsets.all(24),
          child: ListView(
            controller: scrollController,
            children: [
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: Colors.grey[300],
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 20),
              Text(
                'Application Guidelines',
                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
              ),
              const SizedBox(height: 20),
              _buildGuidelineItem(
                icon: Icons.check_circle,
                title: 'Expertise Required',
                description:
                    'Demonstrate relevant expertise in your field with at least 3 years of experience.',
              ),
              _buildGuidelineItem(
                icon: Icons.description,
                title: 'Complete Application',
                description:
                    'Fill all required fields including CV, topic outline, and presentation summary.',
              ),
              _buildGuidelineItem(
                icon: Icons.schedule,
                title: 'Flexible Timing',
                description:
                    'Propose multiple dates for your lecture to increase approval chances.',
              ),
              _buildGuidelineItem(
                icon: Icons.people,
                title: 'Student-Focused',
                description:
                    'Ensure your topic is relevant and valuable to current MUST students.',
              ),
              _buildGuidelineItem(
                icon: Icons.timer,
                title: 'Review Process',
                description:
                    'Applications are reviewed within 5-7 business days by the academic committee.',
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildGuidelineItem({
    required IconData icon,
    required String title,
    required String description,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 20),
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
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  description,
                  style: TextStyle(
                    fontSize: 14,
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
}
