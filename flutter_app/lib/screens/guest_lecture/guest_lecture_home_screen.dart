import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/guest_lecture_provider.dart';
import 'guest_lecture_application_form.dart';
import 'my_applications_screen.dart';
import 'scheduled_lectures_screen.dart';

class GuestLectureHomeScreen extends StatefulWidget {
  const GuestLectureHomeScreen({super.key});

  @override
  State<GuestLectureHomeScreen> createState() => _GuestLectureHomeScreenState();
}

class _GuestLectureHomeScreenState extends State<GuestLectureHomeScreen> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() {
      context.read<GuestLectureProvider>().fetchScheduledLectures();
      context.read<GuestLectureProvider>().fetchMyApplications();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Guest Lectures'),
        elevation: 0,
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildHeader(context),
            _buildQuickActions(context),
            _buildUpcomingLectures(context),
            _buildMyApplicationsPreview(context),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _navigateToApplicationForm(context),
        icon: const Icon(Icons.add),
        label: const Text('Apply to Speak'),
      ),
    );
  }

  Widget _buildHeader(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            Theme.of(context).primaryColor,
            Theme.of(context).primaryColor.withOpacity(0.8),
          ],
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Guest Lecture Program',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Share your expertise with MUST students',
            style: TextStyle(
              fontSize: 14,
              color: Colors.white.withOpacity(0.9),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildQuickActions(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Expanded(
            child: _buildActionCard(
              context,
              icon: Icons.calendar_today,
              title: 'Scheduled',
              subtitle: 'View lectures',
              onTap: () => _navigateToScheduledLectures(context),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: _buildActionCard(
              context,
              icon: Icons.assignment,
              title: 'My Applications',
              subtitle: 'Track status',
              onTap: () => _navigateToMyApplications(context),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildActionCard(
    BuildContext context, {
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return Card(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              Icon(icon, size: 32, color: Theme.of(context).primaryColor),
              const SizedBox(height: 8),
              Text(
                title,
                style: const TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.bold,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 4),
              Text(
                subtitle,
                style: const TextStyle(
                  fontSize: 12,
                  color: Colors.grey,
                ),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildUpcomingLectures(BuildContext context) {
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
                  const Text(
                    'Upcoming Lectures',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  TextButton(
                    onPressed: () => _navigateToScheduledLectures(context),
                    child: const Text('View All'),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              ...provider.scheduledLectures.take(3).map((lecture) {
                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  child: ListTile(
                    leading: CircleAvatar(
                      backgroundColor: Theme.of(context).primaryColor,
                      child: const Icon(Icons.event, color: Colors.white),
                    ),
                    title: Text(
                      lecture.lectureTitle,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    subtitle: Text(
                      '${lecture.speakerName} • ${_formatDate(lecture.scheduledDate!)}',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () {
                      // Navigate to lecture details
                    },
                  ),
                );
              }),
            ],
          ),
        );
      },
    );
  }

  Widget _buildMyApplicationsPreview(BuildContext context) {
    return Consumer<GuestLectureProvider>(
      builder: (context, provider, child) {
        if (provider.myApplications.isEmpty) {
          return Padding(
            padding: const EdgeInsets.all(16),
            child: Card(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                  children: [
                    Icon(
                      Icons.lightbulb_outline,
                      size: 64,
                      color: Theme.of(context).primaryColor.withOpacity(0.5),
                    ),
                    const SizedBox(height: 16),
                    const Text(
                      'Share Your Knowledge',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      'Apply to give a guest lecture and inspire the next generation of professionals.',
                      textAlign: TextAlign.center,
                      style: TextStyle(color: Colors.grey),
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: () => _navigateToApplicationForm(context),
                      child: const Text('Apply Now'),
                    ),
                  ],
                ),
              ),
            ),
          );
        }

        return Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'My Applications',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  TextButton(
                    onPressed: () => _navigateToMyApplications(context),
                    child: const Text('View All'),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              ...provider.myApplications.take(2).map((application) {
                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  child: ListTile(
                    leading: _buildStatusIcon(application.status),
                    title: Text(
                      application.lectureTitle,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    subtitle: Text(application.statusDisplay),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () {
                      // Navigate to application details
                    },
                  ),
                );
              }),
            ],
          ),
        );
      },
    );
  }

  Widget _buildStatusIcon(status) {
    IconData icon;
    Color color;

    switch (status.toString()) {
      case 'ApplicationStatus.approved':
        icon = Icons.check_circle;
        color = Colors.green;
        break;
      case 'ApplicationStatus.rejected':
        icon = Icons.cancel;
        color = Colors.red;
        break;
      case 'ApplicationStatus.scheduled':
        icon = Icons.event_available;
        color = Colors.blue;
        break;
      default:
        icon = Icons.pending;
        color = Colors.orange;
    }

    return CircleAvatar(
      backgroundColor: color.withOpacity(0.1),
      child: Icon(icon, color: color, size: 20),
    );
  }

  String _formatDate(DateTime date) {
    return '${date.day}/${date.month}/${date.year}';
  }

  void _navigateToApplicationForm(BuildContext context) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => const GuestLectureApplicationForm(),
      ),
    );
  }

  void _navigateToScheduledLectures(BuildContext context) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => const ScheduledLecturesScreen(),
      ),
    );
  }

  void _navigateToMyApplications(BuildContext context) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => const MyApplicationsScreen(),
      ),
    );
  }
}
