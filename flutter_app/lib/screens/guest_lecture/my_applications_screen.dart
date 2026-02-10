import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/guest_lecture_provider.dart';

class MyApplicationsScreen extends StatelessWidget {
  const MyApplicationsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('My Applications'),
      ),
      body: Consumer<GuestLectureProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.myApplications.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.assignment_outlined,
                      size: 64, color: Colors.grey[400]),
                  const SizedBox(height: 16),
                  const Text('No applications yet'),
                  const SizedBox(height: 8),
                  const Text(
                    'Apply to give a guest lecture',
                    style: TextStyle(color: Colors.grey),
                  ),
                ],
              ),
            );
          }

          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: provider.myApplications.length,
            itemBuilder: (context, index) {
              final application = provider.myApplications[index];
              return Card(
                margin: const EdgeInsets.only(bottom: 12),
                child: ListTile(
                  leading: _buildStatusIcon(application.status),
                  title: Text(application.lectureTitle),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(application.statusDisplay),
                      Text(
                        'Submitted: ${_formatDate(application.submittedAt)}',
                        style: const TextStyle(fontSize: 12),
                      ),
                    ],
                  ),
                  isThreeLine: true,
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () {
                    // Navigate to application details
                  },
                ),
              );
            },
          );
        },
      ),
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
}
