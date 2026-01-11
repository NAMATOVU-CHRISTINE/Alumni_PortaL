import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:alumni_portal/models/event_model.dart';
import 'package:alumni_portal/config/theme.dart';

class EventsScreen extends StatelessWidget {
  const EventsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Events')),
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.push('/create-event'),
        child: const Icon(Icons.add),
      ),
      body: StreamBuilder<QuerySnapshot>(
        stream: FirebaseFirestore.instance
            .collection('events')
            .orderBy('dateTime', descending: false)
            .snapshots(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (!snapshot.hasData || snapshot.data!.docs.isEmpty) {
            return const Center(child: Text('No events found'));
          }

          final events = snapshot.data!.docs
              .map((doc) => EventModel.fromFirestore(doc))
              .toList();
          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: events.length,
            itemBuilder: (context, index) {
              final event = events[index];
              return Card(
                margin: const EdgeInsets.only(bottom: 12),
                child: ListTile(
                  leading: Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: AppColors.primary.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      event.category?.icon ?? '📅',
                      style: const TextStyle(fontSize: 24),
                    ),
                  ),
                  title: Text(
                    event.title ?? 'Event',
                    style: const TextStyle(fontWeight: FontWeight.bold),
                  ),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        event.formattedDateTime,
                        style: const TextStyle(fontSize: 12),
                      ),
                      if (event.location != null)
                        Text(
                          event.location!,
                          style: const TextStyle(
                            fontSize: 12,
                            color: AppColors.textSecondary,
                          ),
                        ),
                    ],
                  ),
                  trailing: event.isOnline
                      ? const Chip(
                          label: Text('Online', style: TextStyle(fontSize: 10)),
                          backgroundColor: AppColors.success,
                        )
                      : null,
                  onTap: () => context.push('/event-details/${event.id}'),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
