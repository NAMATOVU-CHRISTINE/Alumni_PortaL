import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:alumni_portal/config/theme.dart';

class SuccessStoriesWidget extends StatefulWidget {
  const SuccessStoriesWidget({super.key});

  @override
  State<SuccessStoriesWidget> createState() => _SuccessStoriesWidgetState();
}

class _SuccessStoriesWidgetState extends State<SuccessStoriesWidget> {
  List<Map<String, dynamic>> _stories = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadStories();
  }

  Future<void> _loadStories() async {
    try {
      // Try to load from Firestore, fallback to sample data
      final snapshot = await FirebaseFirestore.instance
          .collection('success_stories')
          .orderBy('createdAt', descending: true)
          .limit(5)
          .get();

      if (snapshot.docs.isNotEmpty) {
        _stories =
            snapshot.docs.map((doc) => {...doc.data(), 'id': doc.id}).toList();
      } else {
        // Sample stories for demo
        _stories = [
          {
            'id': '1',
            'title': 'From Graduate to CEO',
            'name': 'Sarah Namukasa',
            'year': '2015',
            'story': 'Started my own tech company after graduation...',
            'achievement': 'Founded TechUganda Ltd',
            'imageUrl': null,
          },
          {
            'id': '2',
            'title': 'Medical Breakthrough',
            'name': 'Dr. John Mukasa',
            'year': '2012',
            'story': 'Leading research in tropical diseases...',
            'achievement': 'Published 20+ research papers',
            'imageUrl': null,
          },
          {
            'id': '3',
            'title': 'Global Impact',
            'name': 'Grace Atuhaire',
            'year': '2018',
            'story': 'Working with UN on sustainable development...',
            'achievement': 'UN Youth Ambassador',
            'imageUrl': null,
          },
        ];
      }

      if (mounted) setState(() => _isLoading = false);
    } catch (e) {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const SizedBox(
        height: 200,
        child: Center(child: CircularProgressIndicator()),
      );
    }

    if (_stories.isEmpty) return const SizedBox.shrink();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Padding(
          padding: EdgeInsets.symmetric(horizontal: 16),
          child: Row(
            children: [
              Icon(Icons.star, color: Colors.amber, size: 24),
              SizedBox(width: 8),
              Text(
                'Success Stories',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        SizedBox(
          height: 200,
          child: PageView.builder(
            controller: PageController(viewportFraction: 0.9),
            itemCount: _stories.length,
            itemBuilder: (context, index) => _buildStoryCard(_stories[index]),
          ),
        ),
      ],
    );
  }

  Widget _buildStoryCard(Map<String, dynamic> story) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 4),
      child: Card(
        clipBehavior: Clip.antiAlias,
        child: Container(
          decoration: BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [
                AppColors.primary,
                AppColors.primaryDark,
              ],
            ),
          ),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    CircleAvatar(
                      radius: 24,
                      backgroundColor: Colors.white24,
                      backgroundImage: story['imageUrl'] != null
                          ? CachedNetworkImageProvider(story['imageUrl'])
                          : null,
                      child: story['imageUrl'] == null
                          ? Text(
                              (story['name'] as String).substring(0, 1),
                              style: const TextStyle(
                                  color: Colors.white, fontSize: 20),
                            )
                          : null,
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            story['name'] ?? '',
                            style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 16,
                            ),
                          ),
                          Text(
                            'Class of ${story['year']}',
                            style:
                                TextStyle(color: Colors.white70, fontSize: 12),
                          ),
                        ],
                      ),
                    ),
                    const Icon(Icons.emoji_events,
                        color: Colors.amber, size: 28),
                  ],
                ),
                const SizedBox(height: 12),
                Text(
                  story['title'] ?? '',
                  style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.w600,
                    fontSize: 18,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  story['achievement'] ?? '',
                  style: TextStyle(color: Colors.white70, fontSize: 13),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
                const Spacer(),
                Align(
                  alignment: Alignment.centerRight,
                  child: TextButton(
                    onPressed: () {},
                    style: TextButton.styleFrom(
                      foregroundColor: Colors.white,
                      backgroundColor: Colors.white24,
                    ),
                    child: const Text('Read More'),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
