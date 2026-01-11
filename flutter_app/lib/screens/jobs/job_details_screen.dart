import 'package:flutter/material.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:alumni_portal/models/job_model.dart';
import 'package:alumni_portal/config/theme.dart';

class JobDetailsScreen extends StatelessWidget {
  final String jobId;

  const JobDetailsScreen({super.key, required this.jobId});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Job Details')),
      body: FutureBuilder<DocumentSnapshot>(
        future: FirebaseFirestore.instance.collection('jobs').doc(jobId).get(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (!snapshot.hasData || !snapshot.data!.exists) {
            return const Center(child: Text('Job not found'));
          }

          final job = JobModel.fromFirestore(snapshot.data!);
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  job.title ?? 'Job Title',
                  style: const TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  job.company ?? 'Company',
                  style: const TextStyle(
                    fontSize: 18,
                    color: AppColors.primary,
                  ),
                ),
                const SizedBox(height: 16),
                _buildInfoChips(job),
                const SizedBox(height: 24),
                const Text(
                  'Description',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                Text(job.description ?? 'No description provided'),
                if (job.requirements != null) ...[
                  const SizedBox(height: 24),
                  const Text(
                    'Requirements',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  Text(job.requirements!),
                ],
                const SizedBox(height: 32),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: () => _applyForJob(context, job),
                    child: const Text('Apply Now'),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildInfoChips(JobModel job) {
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: [
        if (job.location != null)
          _buildChip(Icons.location_on, job.formattedLocation),
        if (job.jobType != null) _buildChip(Icons.work, job.jobType!),
        if (job.salary != null)
          _buildChip(Icons.attach_money, job.formattedSalary),
        if (job.experienceLevel != null)
          _buildChip(Icons.trending_up, job.experienceLevel!),
      ],
    );
  }

  Widget _buildChip(IconData icon, String label) {
    return Chip(
      avatar: Icon(icon, size: 16),
      label: Text(label, style: const TextStyle(fontSize: 12)),
      backgroundColor: AppColors.primary.withOpacity(0.1),
    );
  }

  void _applyForJob(BuildContext context, JobModel job) async {
    if (job.applicationUrl != null) {
      final uri = Uri.parse(job.applicationUrl!);
      if (await canLaunchUrl(uri)) {
        await launchUrl(uri, mode: LaunchMode.externalApplication);
      }
    } else if (job.contactEmail != null) {
      final uri = Uri.parse(
        'mailto:${job.contactEmail}?subject=Application for ${job.title}',
      );
      if (await canLaunchUrl(uri)) {
        await launchUrl(uri);
      }
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('No application method available')),
      );
    }
  }
}
