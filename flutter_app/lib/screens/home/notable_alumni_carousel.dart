import 'dart:async';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:alumni_portal/config/theme.dart';

class NotableAlumniCarousel extends StatefulWidget {
  const NotableAlumniCarousel({super.key});

  @override
  State<NotableAlumniCarousel> createState() => _NotableAlumniCarouselState();
}

class _NotableAlumniCarouselState extends State<NotableAlumniCarousel> {
  final PageController _pageController = PageController();
  int _currentPage = 0;
  Timer? _autoScrollTimer;
  List<Map<String, dynamic>> _notableAlumni = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadNotableAlumni();
  }

  Future<void> _loadNotableAlumni() async {
    try {
      // Try to get featured/notable alumni from Firestore
      final snapshot = await FirebaseFirestore.instance
          .collection('notable_alumni')
          .where('isActive', isEqualTo: true)
          .orderBy('priority', descending: true)
          .limit(5)
          .get();

      if (snapshot.docs.isNotEmpty) {
        _notableAlumni = await Future.wait(
          snapshot.docs.map((doc) async {
            final data = doc.data();
            // Get user details
            final userDoc = await FirebaseFirestore.instance
                .collection('users')
                .doc(data['userId'])
                .get();

            if (userDoc.exists) {
              final userData = userDoc.data()!;
              return {
                'userId': data['userId'],
                'fullName': userData['fullName'] ?? 'Notable Alumni',
                'profileImageUrl': userData['profileImageUrl'],
                'currentJob': userData['currentJob'] ?? data['position'],
                'company': userData['company'] ?? data['company'],
                'graduationYear': userData['graduationYear'] ?? data['graduationYear'],
                'achievement': data['achievement'] ?? 'Distinguished Alumni',
                'quote': data['quote'],
              };
            }
            return null;
          }).toList(),
        );
        _notableAlumni.removeWhere((element) => element == null);
      }

      // Fallback: Get alumni with notable achievements
      if (_notableAlumni.isEmpty) {
        final usersSnapshot = await FirebaseFirestore.instance
            .collection('users')
            .where('isAlumni', isEqualTo: true)
            .where('isNotable', isEqualTo: true)
            .limit(5)
            .get();

        if (usersSnapshot.docs.isNotEmpty) {
          _notableAlumni = usersSnapshot.docs.map((doc) {
            final data = doc.data();
            return {
              'userId': doc.id,
              'fullName': data['fullName'] ?? 'Notable Alumni',
              'profileImageUrl': data['profileImageUrl'],
              'currentJob': data['currentJob'],
              'company': data['company'],
              'graduationYear': data['graduationYear'],
              'achievement': data['achievement'] ?? 'Distinguished Alumni',
              'quote': data['quote'],
            };
          }).toList();
        }
      }

      // If still empty, create sample data for demonstration
      if (_notableAlumni.isEmpty) {
        _notableAlumni = _getSampleAlumni();
      }

      if (mounted) {
        setState(() => _isLoading = false);
        _startAutoScroll();
      }
    } catch (e) {
      print('Error loading notable alumni: $e');
      // Use sample data on error
      _notableAlumni = _getSampleAlumni();
      if (mounted) {
        setState(() => _isLoading = false);
        _startAutoScroll();
      }
    }
  }

  List<Map<String, dynamic>> _getSampleAlumni() {
    return [
      {
        'userId': 'sample1',
        'fullName': 'Dr. Sarah Nakato',
        'profileImageUrl': null,
        'currentJob': 'Chief Medical Officer',
        'company': 'Mulago Hospital',
        'graduationYear': '2012',
        'achievement': 'Leading healthcare innovation in Uganda',
        'quote': 'MUST gave me the foundation to make a difference',
      },
      {
        'userId': 'sample2',
        'fullName': 'Eng. David Okello',
        'profileImageUrl': null,
        'currentJob': 'Software Engineering Manager',
        'company': 'Google',
        'graduationYear': '2015',
        'achievement': 'Building technology solutions for Africa',
        'quote': 'Excellence is a journey, not a destination',
      },
      {
        'userId': 'sample3',
        'fullName': 'Prof. Grace Namugga',
        'profileImageUrl': null,
        'currentJob': 'Research Scientist',
        'company': 'WHO',
        'graduationYear': '2010',
        'achievement': 'Pioneering research in tropical diseases',
        'quote': 'Science has the power to transform lives',
      },
    ];
  }

  void _startAutoScroll() {
    _autoScrollTimer?.cancel();
    _autoScrollTimer = Timer.periodic(const Duration(seconds: 5), (timer) {
      if (_notableAlumni.isEmpty || !mounted) return;

      final nextPage = (_currentPage + 1) % _notableAlumni.length;
      _pageController.animateToPage(
        nextPage,
        duration: const Duration(milliseconds: 400),
        curve: Curves.easeInOut,
      );
    });
  }

  @override
  void dispose() {
    _autoScrollTimer?.cancel();
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return Container(
        height: 200,
        margin: const EdgeInsets.symmetric(horizontal: 20),
        child: const Center(child: CircularProgressIndicator()),
      );
    }

    if (_notableAlumni.isEmpty) {
      return const SizedBox.shrink();
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: Row(
            children: [
              const Icon(Icons.auto_awesome, color: Colors.amber, size: 20),
              const SizedBox(width: 8),
              Text(
                'Notable Alumni',
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        SizedBox(
          height: 200,
          child: PageView.builder(
            controller: _pageController,
            onPageChanged: (index) {
              setState(() => _currentPage = index);
            },
            itemCount: _notableAlumni.length,
            itemBuilder: (context, index) {
              return _buildAlumniCard(_notableAlumni[index]);
            },
          ),
        ),
        const SizedBox(height: 12),
        // Page indicators
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: List.generate(
            _notableAlumni.length,
            (index) => AnimatedContainer(
              duration: const Duration(milliseconds: 300),
              margin: const EdgeInsets.symmetric(horizontal: 4),
              width: _currentPage == index ? 24 : 8,
              height: 8,
              decoration: BoxDecoration(
                color: _currentPage == index
                    ? AppColors.primary
                    : AppColors.primary.withValues(alpha: 0.3),
                borderRadius: BorderRadius.circular(4),
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildAlumniCard(Map<String, dynamic> alumni) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 20),
      child: Card(
        elevation: 4,
        clipBehavior: Clip.antiAlias,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
        child: InkWell(
          onTap: () {
            if (alumni['userId'] != null && !alumni['userId'].toString().startsWith('sample')) {
              context.push('/view-profile/${alumni['userId']}');
            }
          },
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
                  // Profile image with star badge
                  Stack(
                    children: [
                      Container(
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          border: Border.all(color: Colors.white, width: 3),
                          boxShadow: [
                            BoxShadow(
                              color: Colors.black.withValues(alpha: 0.2),
                              blurRadius: 8,
                              offset: const Offset(0, 4),
                            ),
                          ],
                        ),
                        child: CircleAvatar(
                          radius: 40,
                          backgroundColor: Colors.white24,
                          backgroundImage: alumni['profileImageUrl'] != null
                              ? CachedNetworkImageProvider(
                                  alumni['profileImageUrl'])
                              : null,
                          child: alumni['profileImageUrl'] == null
                              ? Text(
                                  (alumni['fullName'] as String)
                                      .substring(0, 1)
                                      .toUpperCase(),
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 32,
                                    fontWeight: FontWeight.bold,
                                  ),
                                )
                              : null,
                        ),
                      ),
                      Positioned(
                        bottom: 0,
                        right: 0,
                        child: Container(
                          padding: const EdgeInsets.all(6),
                          decoration: BoxDecoration(
                            color: Colors.amber,
                            shape: BoxShape.circle,
                            border: Border.all(color: Colors.white, width: 2),
                          ),
                          child: const Icon(
                            Icons.star,
                            color: Colors.white,
                            size: 16,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(width: 16),
                  // Info
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          alumni['fullName'] ?? '',
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        if (alumni['currentJob'] != null) ...[
                          const SizedBox(height: 4),
                          Text(
                            '${alumni['currentJob']}${alumni['company'] != null ? ' at ${alumni['company']}' : ''}',
                            style: const TextStyle(
                              color: Colors.white70,
                              fontSize: 13,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                        if (alumni['graduationYear'] != null) ...[
                          const SizedBox(height: 2),
                          Text(
                            'Class of ${alumni['graduationYear']}',
                            style: const TextStyle(
                              color: Colors.white60,
                              fontSize: 12,
                            ),
                          ),
                        ],
                        const SizedBox(height: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 10,
                            vertical: 6,
                          ),
                          decoration: BoxDecoration(
                            color: Colors.white.withValues(alpha: 0.2),
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(
                              color: Colors.white.withValues(alpha: 0.3),
                            ),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              const Icon(
                                Icons.emoji_events,
                                color: Colors.amber,
                                size: 14,
                              ),
                              const SizedBox(width: 6),
                              Flexible(
                                child: Text(
                                  alumni['achievement'] ?? '',
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 11,
                                    fontWeight: FontWeight.w500,
                                  ),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            ],
                          ),
                        ),
                        if (alumni['quote'] != null) ...[
                          const SizedBox(height: 8),
                          Text(
                            '"${alumni['quote']}"',
                            style: TextStyle(
                              color: Colors.white.withValues(alpha: 0.9),
                              fontSize: 11,
                              fontStyle: FontStyle.italic,
                            ),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ],
                    ),
                  ),
                  const SizedBox(width: 8),
                  const Icon(
                    Icons.chevron_right,
                    color: Colors.white54,
                    size: 28,
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
