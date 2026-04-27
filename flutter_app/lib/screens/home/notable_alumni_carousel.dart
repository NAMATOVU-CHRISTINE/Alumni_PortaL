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
        final alumniList = <Map<String, dynamic>>[];
        
        for (var doc in snapshot.docs) {
          final data = doc.data();
          // Get user details
          final userDoc = await FirebaseFirestore.instance
              .collection('users')
              .doc(data['userId'])
              .get();

          if (userDoc.exists) {
            final userData = userDoc.data()!;
            alumniList.add({
              'userId': data['userId'],
              'fullName': userData['fullName'] ?? 'Notable Alumni',
              'profileImageUrl': userData['profileImageUrl'],
              'currentJob': userData['currentJob'] ?? data['position'],
              'company': userData['company'] ?? data['company'],
              'graduationYear': userData['graduationYear'] ?? data['graduationYear'],
              'achievement': data['achievement'] ?? 'Distinguished Alumni',
              'quote': data['quote'],
            });
          }
        }
        
        _notableAlumni = alumniList;
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
        'fullName': 'Dr. Monica Musenero',
        'profileImageUrl': 'https://newvision-media.s3.amazonaws.com/cms/d9322ce7-fe13-4796-a546-be425b172239.jpg',
        'currentJob': 'Senior Presidential Advisor on Epidemics',
        'company': 'Office of the President, Uganda',
        'graduationYear': '1995',
        'achievement': 'Leading Uganda\'s COVID-19 response & vaccine development',
        'quote': 'Science and innovation are key to solving Africa\'s challenges',
        'linkedin': 'https://www.linkedin.com/in/monica-musenero',
        'twitter': 'https://twitter.com/MonicaMusenero',
      },
      {
        'userId': 'sample2',
        'fullName': 'Prof. Celestino Obua',
        'profileImageUrl': 'https://softpower.ug/wp-content/uploads/2025/01/Gh-AHK1WsAAauCt.jpg',
        'currentJob': 'Vice Chancellor',
        'company': 'Mbarara University of Science & Technology',
        'graduationYear': '1998',
        'achievement': 'Leading academic excellence and research innovation',
        'quote': 'Education transforms lives and builds nations',
        'linkedin': 'https://www.linkedin.com/in/celestino-obua',
      },
      {
        'userId': 'sample3',
        'fullName': 'Dr. Jackson Orem',
        'profileImageUrl': 'https://uci.or.ug/wp-content/uploads/2025/08/Orem.jpeg',
        'currentJob': 'Executive Director',
        'company': 'Uganda Cancer Institute',
        'graduationYear': '2000',
        'achievement': 'Pioneering cancer research and treatment in East Africa',
        'quote': 'Every patient deserves access to quality cancer care',
        'linkedin': 'https://www.linkedin.com/in/jackson-orem',
        'twitter': 'https://twitter.com/JacksonOrem',
      },
      {
        'userId': 'sample4',
        'fullName': 'Dr. Sarah Kiguli',
        'profileImageUrl': 'https://som.mak.ac.ug/wp-content/uploads/2022/04/Professor-Sarah-Kiguli.jpg',
        'currentJob': 'Principal',
        'company': 'Makerere University College of Health Sciences',
        'graduationYear': '1997',
        'achievement': 'Advancing medical education and pediatric care',
        'quote': 'Investing in health education is investing in our future',
        'linkedin': 'https://www.linkedin.com/in/sarah-kiguli',
      },
      {
        'userId': 'sample5',
        'fullName': 'Eng. Dr. Silver Mugisha',
        'profileImageUrl': 'https://cioviews.com/wp-content/uploads/2023/08/1.-Silver-Mugisha.jpg',
        'currentJob': 'Managing Director',
        'company': 'National Water and Sewerage Corporation',
        'graduationYear': '2002',
        'achievement': 'Transforming Uganda\'s water and sanitation infrastructure',
        'quote': 'Clean water is a fundamental human right',
        'linkedin': 'https://www.linkedin.com/in/silver-mugisha',
        'twitter': 'https://twitter.com/SilverMugisha',
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

  String _getInitials(String name) {
    final parts = name.split(' ');
    if (parts.length >= 2) {
      return '${parts[0][0]}${parts[1][0]}'.toUpperCase();
    }
    return name.substring(0, 1).toUpperCase();
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
              const Icon(Icons.workspace_premium, color: Colors.amber, size: 20),
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
          height: 220,
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
            } else {
              // Show full info dialog for sample data
              _showAlumniDetailsDialog(alumni);
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
                  // Profile image with badge
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
                          backgroundColor: Colors.white,
                          backgroundImage: alumni['profileImageUrl'] != null
                              ? CachedNetworkImageProvider(
                                  alumni['profileImageUrl'])
                              : null,
                          child: alumni['profileImageUrl'] == null
                              ? Text(
                                  _getInitials(alumni['fullName'] ?? 'NA'),
                                  style: TextStyle(
                                    color: AppColors.primary,
                                    fontSize: 28,
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
                            Icons.verified,
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
                            fontSize: 17,
                            fontWeight: FontWeight.bold,
                            height: 1.2,
                          ),
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                        if (alumni['currentJob'] != null) ...[
                          const SizedBox(height: 6),
                          Row(
                            children: [
                              const Icon(
                                Icons.work_outline,
                                color: Colors.white70,
                                size: 14,
                              ),
                              const SizedBox(width: 4),
                              Expanded(
                                child: Text(
                                  alumni['currentJob'],
                                  style: const TextStyle(
                                    color: Colors.white70,
                                    fontSize: 13,
                                  ),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            ],
                          ),
                        ],
                        if (alumni['company'] != null) ...[
                          const SizedBox(height: 4),
                          Row(
                            children: [
                              const Icon(
                                Icons.business,
                                color: Colors.white70,
                                size: 14,
                              ),
                              const SizedBox(width: 4),
                              Expanded(
                                child: Text(
                                  alumni['company'],
                                  style: const TextStyle(
                                    color: Colors.white70,
                                    fontSize: 13,
                                  ),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            ],
                          ),
                        ],
                        if (alumni['graduationYear'] != null) ...[
                          const SizedBox(height: 4),
                          Row(
                            children: [
                              const Icon(
                                Icons.school,
                                color: Colors.white60,
                                size: 14,
                              ),
                              const SizedBox(width: 4),
                              Text(
                                'Class of ${alumni['graduationYear']}',
                                style: const TextStyle(
                                  color: Colors.white60,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ],
                        const SizedBox(height: 10),
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
                                  maxLines: 2,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            ],
                          ),
                        ),
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

  void _showAlumniDetailsDialog(Map<String, dynamic> alumni) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        contentPadding: EdgeInsets.zero,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
        content: Container(
          width: MediaQuery.of(context).size.width * 0.9,
          decoration: BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [
                AppColors.primary.withValues(alpha: 0.9),
                AppColors.primaryDark,
              ],
            ),
            borderRadius: BorderRadius.circular(16),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Header with close button
              Padding(
                padding: const EdgeInsets.all(16),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Notable Alumni',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close, color: Colors.white),
                      onPressed: () => Navigator.pop(context),
                    ),
                  ],
                ),
              ),
              // Profile section
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Column(
                  children: [
                    Stack(
                      children: [
                        CircleAvatar(
                          radius: 50,
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
                                    fontSize: 40,
                                    fontWeight: FontWeight.bold,
                                  ),
                                )
                              : null,
                        ),
                        Positioned(
                          bottom: 0,
                          right: 0,
                          child: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: Colors.amber,
                              shape: BoxShape.circle,
                              border: Border.all(color: Colors.white, width: 2),
                            ),
                            child: const Icon(
                              Icons.verified,
                              color: Colors.white,
                              size: 20,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    Text(
                      alumni['fullName'] ?? '',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 22,
                        fontWeight: FontWeight.bold,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    if (alumni['currentJob'] != null) ...[
                      const SizedBox(height: 8),
                      Text(
                        alumni['currentJob'],
                        style: const TextStyle(
                          color: Colors.white70,
                          fontSize: 16,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ],
                    if (alumni['company'] != null) ...[
                      const SizedBox(height: 4),
                      Text(
                        alumni['company'],
                        style: const TextStyle(
                          color: Colors.white70,
                          fontSize: 14,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ],
                    if (alumni['graduationYear'] != null) ...[
                      const SizedBox(height: 8),
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 12,
                          vertical: 6,
                        ),
                        decoration: BoxDecoration(
                          color: Colors.white.withValues(alpha: 0.2),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Text(
                          'Class of ${alumni['graduationYear']}',
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 13,
                          ),
                        ),
                      ),
                    ],
                    const SizedBox(height: 16),
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: Colors.white.withValues(alpha: 0.2),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Row(
                        children: [
                          const Icon(
                            Icons.emoji_events,
                            color: Colors.amber,
                            size: 20,
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              alumni['achievement'] ?? '',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 14,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                    if (alumni['quote'] != null) ...[
                      const SizedBox(height: 16),
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: Colors.white.withValues(alpha: 0.1),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Icon(
                              Icons.format_quote,
                              color: Colors.white60,
                              size: 20,
                            ),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Text(
                                alumni['quote'],
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 13,
                                  fontStyle: FontStyle.italic,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                    const SizedBox(height: 20),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
