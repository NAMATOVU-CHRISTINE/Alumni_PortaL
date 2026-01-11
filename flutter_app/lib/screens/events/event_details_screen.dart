import 'package:flutter/material.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:alumni_portal/models/event_model.dart';
import 'package:alumni_portal/config/theme.dart';

class EventDetailsScreen extends StatelessWidget {
  final String eventId;
  const EventDetailsScreen({super.key, required this.eventId});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Event Details')),
      body: FutureBuilder<DocumentSnapshot>(
        future: FirebaseFirestore.instance
            .collection('events')
            .doc(eventId)
            .get(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (!snapshot.hasData || !snapshot.data!.exists) {
            return const Center(child: Text('Event not found'));
          }
          final event = EventModel.fromFirestore(snapshot.data!);
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  event.title ?? 'Event',
                  style: const TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 16),
                _buildInfoRow(Icons.calendar_today, event.formattedDateTime),
                if (event.location != null)
                  _buildInfoRow(Icons.location_on, event.location!),
                if (event.organizerName != null)
                  _buildInfoRow(
                    Icons.person,
                    'Organized by ${event.organizerName}',
                  ),
                _buildInfoRow(
                  Icons.people,
                  '${event.currentParticipants}/${event.maxParticipants} participants',
                ),
                const SizedBox(height: 24),
                const Text(
                  'Description',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                Text(event.description ?? 'No description'),
                const SizedBox(height: 32),
                if (event.isAvailableForRegistration)
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: () {},
                      child: const Text('Register'),
                    ),
                  ),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildInfoRow(IconData icon, String text) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          Icon(icon, size: 20, color: AppColors.textSecondary),
          const SizedBox(width: 12),
          Expanded(child: Text(text)),
        ],
      ),
    );
  }
}
