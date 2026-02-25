import 'package:flutter/material.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:go_router/go_router.dart';
import 'package:alumni_portal/config/theme.dart';

class StoryViewersScreen extends StatelessWidget {
  final String storyId;
  final List<String> viewerIds;

  const StoryViewersScreen({
    super.key,
    required this.storyId,
    required this.viewerIds,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('${viewerIds.length} Views'),
        backgroundColor: AppColors.primary,
        foregroundColor: Colors.white,
      ),
      body: viewerIds.isEmpty
          ? const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.visibility_off, size: 64, color: Colors.grey),
                  SizedBox(height: 16),
                  Text(
                    'No views yet',
                    style: TextStyle(fontSize: 18, color: Colors.grey),
                  ),
                ],
              ),
            )
          : ListView.builder(
              itemCount: viewerIds.length,
              itemBuilder: (context, index) {
                final viewerId = viewerIds[index];
                return FutureBuilder<DocumentSnapshot>(
                  future: FirebaseFirestore.instance
                      .collection('users')
                      .doc(viewerId)
                      .get(),
                  builder: (context, snapshot) {
                    if (!snapshot.hasData) {
                      return const ListTile(
                        leading: CircleAvatar(child: Icon(Icons.person)),
                        title: Text('Loading...'),
                      );
                    }

                    final userData =
                        snapshot.data!.data() as Map<String, dynamic>?;
                    if (userData == null) {
                      return const SizedBox.shrink();
                    }

                    final name = userData['fullName'] ?? 'Unknown';
                    final imageUrl = userData['profileImageUrl'] as String?;
                    final lastSeen = userData['lastSeen'] as int?;

                    return ListTile(
                      leading: CircleAvatar(
                        radius: 24,
                        backgroundImage: imageUrl != null
                            ? CachedNetworkImageProvider(imageUrl)
                            : null,
                        child: imageUrl == null
                            ? Text(
                                name.substring(0, 1).toUpperCase(),
                                style: const TextStyle(
                                  fontSize: 20,
                                  fontWeight: FontWeight.bold,
                                ),
                              )
                            : null,
                      ),
                      title: Text(
                        name,
                        style: const TextStyle(fontWeight: FontWeight.w600),
                      ),
                      subtitle: lastSeen != null
                          ? Text(
                              'Active ${timeago.format(DateTime.fromMillisecondsSinceEpoch(lastSeen))}',
                              style: const TextStyle(fontSize: 12),
                            )
                          : null,
                      onTap: () {
                        context.push('/view-profile/$viewerId');
                      },
                    );
                  },
                );
              },
            ),
    );
  }
}
