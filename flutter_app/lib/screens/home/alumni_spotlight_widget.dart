import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:alumni_portal/config/theme.dart';

class AlumniSpotlightWidget extends StatefulWidget {
  const AlumniSpotlightWidget({super.key});

  @override
  State<AlumniSpotlightWidget> createState() => _AlumniSpotlightWidgetState();
}

class _AlumniSpotlightWidgetState extends State<AlumniSpotlightWidget> {
  Map<String, dynamic>? _spotlightAlumni;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadSpotlight();
  }

  Future<void> _loadSpotlight() async {
    try {
      // Try to get featured alumni from Firestore
      final snapshot = await FirebaseFirestore.instance
          .collection('spotlight')
          .where('isActive', isEqualTo: true)
          .orderBy('featuredAt', descending: true)
          .limit(1)
          .get();

      if (snapshot.docs.isNotEmpty) {
        final data = snapshot.docs.first.data();
        // Get user details
        final userDoc = await FirebaseFirestore.instance
            .collection('users')
            .doc(data['userId'])
            .get();

        if (userDoc.exists) {
          final userData = userDoc.data()!;
          _spotlightAlumni = {
            'userId': data['userId'],
            'fullName': userData['fullName'] ?? 'Featured Alumni',
            'profileImageUrl': userData['profileImageUrl'],
            'currentJob': userData['currentJob'],
            'company': userData['company'],
            'graduationYear': userData['graduationYear'],
            'achievement':
                data['achievement'] ?? 'Making a difference in the community',
            'quote': data['quote'],
          };
        }
      } else {
        // Fallback: Get a random active alumni with complete profile
        final usersSnapshot = await FirebaseFirestore.instance
            .collection('users')
            .where('isAlumni', isEqualTo: true)
            .limit(10)
            .get();

        if (usersSnapshot.docs.isNotEmpty) {
          final randomUser = usersSnapshot.docs.first;
          final userData = randomUser.data();
          _spotlightAlumni = {
            'userId': randomUser.id,
            'fullName': userData['fullName'] ?? 'Featured Alumni',
            'profileImageUrl': userData['profileImageUrl'],
            'currentJob': userData['currentJob'],
            'company': userData['company'],
            'graduationYear': userData['graduationYear'],
            'achievement': 'Active community member',
            'quote': null,
          };
        }
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
          height: 200, child: Center(child: CircularProgressIndicator()));
    }

    if (_spotlightAlumni == null) return const SizedBox.shrink();

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Row(
            children: [
              Icon(Icons.auto_awesome, color: Colors.amber, size: 20),
              SizedBox(width: 8),
              Text(
                'Alumni Spotlight',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Card(
            clipBehavior: Clip.antiAlias,
            child: InkWell(
              onTap: () =>
                  context.push('/view-profile/${_spotlightAlumni!['userId']}'),
              child: Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [
                      AppColors.primary.withValues(alpha: 0.9),
                      AppColors.primaryDark,
                    ],
                  ),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Row(
                    children: [
                      // Profile image with badge
                      Stack(
                        children: [
                          CircleAvatar(
                            radius: 40,
                            backgroundColor: Colors.white24,
                            backgroundImage:
                                _spotlightAlumni!['profileImageUrl'] != null
                                    ? CachedNetworkImageProvider(
                                        _spotlightAlumni!['profileImageUrl'])
                                    : null,
                            child: _spotlightAlumni!['profileImageUrl'] == null
                                ? Text(
                                    (_spotlightAlumni!['fullName'] as String)
                                        .substring(0, 1),
                                    style: const TextStyle(
                                      color: Colors.white,
                                      fontSize: 32,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  )
                                : null,
                          ),
                          Positioned(
                            bottom: 0,
                            right: 0,
                            child: Container(
                              padding: const EdgeInsets.all(4),
                              decoration: const BoxDecoration(
                                color: Colors.amber,
                                shape: BoxShape.circle,
                              ),
                              child: const Icon(Icons.star,
                                  color: Colors.white, size: 16),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(width: 16),
                      // Info
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              _spotlightAlumni!['fullName'] ?? '',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 18,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            if (_spotlightAlumni!['currentJob'] != null) ...[
                              const SizedBox(height: 4),
                              Text(
                                '${_spotlightAlumni!['currentJob']}${_spotlightAlumni!['company'] != null ? ' at ${_spotlightAlumni!['company']}' : ''}',
                                style: const TextStyle(
                                    color: Colors.white70, fontSize: 13),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ],
                            if (_spotlightAlumni!['graduationYear'] !=
                                null) ...[
                              const SizedBox(height: 2),
                              Text(
                                'Class of ${_spotlightAlumni!['graduationYear']}',
                                style: const TextStyle(
                                    color: Colors.white60, fontSize: 12),
                              ),
                            ],
                            const SizedBox(height: 8),
                            Container(
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 8, vertical: 4),
                              decoration: BoxDecoration(
                                color: Colors.white24,
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: Text(
                                _spotlightAlumni!['achievement'] ?? '',
                                style: const TextStyle(
                                    color: Colors.white, fontSize: 11),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                          ],
                        ),
                      ),
                      const Icon(Icons.chevron_right, color: Colors.white54),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
