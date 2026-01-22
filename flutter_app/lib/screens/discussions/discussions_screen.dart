import 'dart:async';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/config/theme.dart';

class DiscussionsScreen extends StatefulWidget {
  const DiscussionsScreen({super.key});

  @override
  State<DiscussionsScreen> createState() => _DiscussionsScreenState();
}

class _DiscussionsScreenState extends State<DiscussionsScreen>
    with TickerProviderStateMixin {
  late TabController _tabController;
  final _searchController = TextEditingController();
  bool _showWelcomeHeader = true;
  Timer? _welcomeTimer;

  @override
  void initState() {
    super.initState();
    _tabController =
        TabController(length: 5, vsync: this); // Changed to 5 for Q&A tab
    _startWelcomeTimer();
  }

  void _startWelcomeTimer() {
    _welcomeTimer = Timer(const Duration(seconds: 30), () {
      if (mounted) {
        setState(() {
          _showWelcomeHeader = false;
        });
      }
    });
  }

  @override
  void dispose() {
    _tabController.dispose();
    _searchController.dispose();
    _welcomeTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Community Discussions'),
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () => _showSearchDialog(),
          ),
        ],
      ),
      body: Column(
        children: [
          // Header Section (conditional)
          if (_showWelcomeHeader)
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: Theme.of(context).primaryColor.withValues(alpha: 0.1),
                border: Border(
                  bottom: BorderSide(
                    color: Theme.of(context).dividerColor,
                    width: 0.5,
                  ),
                ),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          'Welcome to MUST Alumni Discussions',
                          style:
                              Theme.of(context).textTheme.titleLarge?.copyWith(
                                    fontWeight: FontWeight.w600,
                                    fontSize: 16,
                                    color: Theme.of(context).primaryColor,
                                  ),
                        ),
                      ),
                      IconButton(
                        icon: const Icon(Icons.close, size: 20),
                        onPressed: () {
                          setState(() {
                            _showWelcomeHeader = false;
                          });
                          _welcomeTimer?.cancel();
                        },
                        tooltip: 'Close',
                      ),
                    ],
                  ),
                  const SizedBox(height: 6),
                  Text(
                    'Connect, share knowledge, and engage with fellow alumni. Ask questions, share experiences, and build meaningful connections.',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          fontSize: 13,
                          height: 1.4,
                          color: Theme.of(context).textTheme.bodySmall?.color,
                        ),
                  ),
                  const SizedBox(height: 12),
                  StreamBuilder<QuerySnapshot>(
                    stream: FirebaseFirestore.instance
                        .collection('discussions')
                        .snapshots(),
                    builder: (context, discussionsSnapshot) {
                      return StreamBuilder<QuerySnapshot>(
                        stream: FirebaseFirestore.instance
                            .collection('users')
                            .where('lastActive',
                                isGreaterThan: DateTime.now()
                                    .subtract(const Duration(minutes: 5))
                                    .millisecondsSinceEpoch)
                            .snapshots(),
                        builder: (context, activeUsersSnapshot) {
                          return StreamBuilder<QuerySnapshot>(
                            stream: FirebaseFirestore.instance
                                .collection('users')
                                .where('lastActive',
                                    isGreaterThan: DateTime.now()
                                        .subtract(const Duration(hours: 24))
                                        .millisecondsSinceEpoch)
                                .snapshots(),
                            builder: (context, onlineUsersSnapshot) {
                              final activeDiscussions =
                                  discussionsSnapshot.hasData
                                      ? discussionsSnapshot.data!.docs.length
                                      : 0;
                              final activeUsers = activeUsersSnapshot.hasData
                                  ? activeUsersSnapshot.data!.docs.length
                                  : 24; // Default fallback
                              final onlineUsers = onlineUsersSnapshot.hasData
                                  ? onlineUsersSnapshot.data!.docs.length
                                  : 156; // Default fallback

                              return Row(
                                children: [
                                  _buildStatChip('Active', '$activeUsers'),
                                  const SizedBox(width: 12),
                                  _buildStatChip('Online', '$onlineUsers'),
                                  const SizedBox(width: 12),
                                  _buildStatChip(
                                      'Discussions', '$activeDiscussions'),
                                ],
                              );
                            },
                          );
                        },
                      );
                    },
                  ),
                ],
              ),
            ),

          // Tab Bar
          Container(
            color: Theme.of(context).scaffoldBackgroundColor,
            child: TabBar(
              controller: _tabController,
              isScrollable: true,
              labelColor: Theme.of(context).primaryColor,
              unselectedLabelColor:
                  Theme.of(context).textTheme.bodySmall?.color,
              indicatorColor: Theme.of(context).primaryColor,
              tabs: const [
                Tab(text: 'All'),
                Tab(text: 'Career'),
                Tab(text: 'Tech'),
                Tab(text: 'Life'),
                Tab(text: 'Q&A'),
              ],
            ),
          ),

          // Content
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: [
                _buildAllDiscussions(),
                _buildCategoryDiscussions('Career'),
                _buildCategoryDiscussions('Tech'),
                _buildCategoryDiscussions('Life'),
                _buildQASection(),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatChip(String label, String value) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: Theme.of(context).cardColor,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: Theme.of(context).dividerColor,
          width: 0.5,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            value,
            style: Theme.of(context).textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: Theme.of(context).primaryColor,
                ),
          ),
          const SizedBox(width: 4),
          Text(
            label,
            style: Theme.of(context).textTheme.bodySmall,
          ),
        ],
      ),
    );
  }

  Widget _buildAllDiscussions() {
    return StreamBuilder<QuerySnapshot>(
      stream: FirebaseFirestore.instance
          .collection('discussions')
          .orderBy('createdAt', descending: true)
          .snapshots(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(child: CircularProgressIndicator());
        }

        if (!snapshot.hasData || snapshot.data!.docs.isEmpty) {
          return _buildEmptyState();
        }

        final discussions = snapshot.data!.docs;
        return RefreshIndicator(
          onRefresh: () async {
            // Refresh is handled by the stream
          },
          child: ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: discussions.length,
            itemBuilder: (context, index) {
              final discussion =
                  discussions[index].data() as Map<String, dynamic>;
              discussion['id'] = discussions[index].id;
              return _buildDiscussionCard(discussion);
            },
          ),
        );
      },
    );
  }

  Widget _buildCategoryDiscussions(String category) {
    return StreamBuilder<QuerySnapshot>(
      stream: FirebaseFirestore.instance
          .collection('discussions')
          .where('category', isEqualTo: category)
          .orderBy('createdAt', descending: true)
          .snapshots(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(child: CircularProgressIndicator());
        }

        if (!snapshot.hasData || snapshot.data!.docs.isEmpty) {
          return _buildEmptyState(category: category);
        }

        final discussions = snapshot.data!.docs;
        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: discussions.length,
          itemBuilder: (context, index) {
            final discussion =
                discussions[index].data() as Map<String, dynamic>;
            discussion['id'] = discussions[index].id;
            return _buildDiscussionCard(discussion);
          },
        );
      },
    );
  }

  Widget _buildEmptyState({String? category}) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.forum_outlined,
            size: 80,
            color: Theme.of(context).textTheme.bodySmall?.color,
          ),
          const SizedBox(height: 16),
          Text(
            category != null
                ? 'No $category discussions yet'
                : 'No discussions yet',
            style: Theme.of(context).textTheme.titleLarge,
          ),
          const SizedBox(height: 8),
          Text(
            'Be the first to start a discussion!',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: Theme.of(context).textTheme.bodySmall?.color,
                ),
          ),
          const SizedBox(height: 24),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton.icon(
                onPressed: () => _createNewDiscussion(),
                icon: const Icon(Icons.add),
                label: const Text('Start Discussion'),
              ),
              const SizedBox(width: 12),
              OutlinedButton.icon(
                onPressed: () => _createNewQuestion(),
                icon: const Icon(Icons.help_outline),
                label: const Text('Ask Question'),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildDiscussionCard(Map<String, dynamic> discussion) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: InkWell(
        onTap: () => _openDiscussion(discussion),
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header
              Row(
                children: [
                  CircleAvatar(
                    radius: 20,
                    backgroundColor: AppColors.primary.withValues(alpha: 0.1),
                    backgroundImage: discussion['authorImage'] != null
                        ? CachedNetworkImageProvider(discussion['authorImage'])
                        : null,
                    child: discussion['authorImage'] == null
                        ? Text(
                            discussion['author'].substring(0, 1).toUpperCase(),
                            style: TextStyle(
                              color: AppColors.primary,
                              fontWeight: FontWeight.bold,
                            ),
                          )
                        : null,
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          discussion['author'] ?? 'Anonymous',
                          style:
                              Theme.of(context).textTheme.bodyMedium?.copyWith(
                                    fontWeight: FontWeight.w600,
                                    fontSize: 14,
                                  ),
                        ),
                        Text(
                          discussion['timeAgo'] ?? 'Recently',
                          style:
                              Theme.of(context).textTheme.bodySmall?.copyWith(
                                    fontSize: 12,
                                  ),
                        ),
                      ],
                    ),
                  ),
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: _getCategoryColor(discussion['category'])
                          .withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      discussion['category'],
                      style: TextStyle(
                        fontSize: 10,
                        fontWeight: FontWeight.w500,
                        color: _getCategoryColor(discussion['category']),
                      ),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 12),

              // Title
              Text(
                discussion['title'] ?? 'Discussion',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      height: 1.3,
                    ),
              ),

              const SizedBox(height: 6),

              // Content preview
              Text(
                discussion['content'] ?? '',
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      fontSize: 13,
                      height: 1.4,
                    ),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),

              const SizedBox(height: 12),

              // Tags
              if (discussion['tags'] != null)
                Wrap(
                  spacing: 6,
                  children: (discussion['tags'] as List<dynamic>)
                      .map((tag) => Container(
                            padding: const EdgeInsets.symmetric(
                                horizontal: 8, vertical: 2),
                            decoration: BoxDecoration(
                              color: Colors.grey[200],
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Text(
                              '#${tag.toString()}',
                              style: const TextStyle(fontSize: 10),
                            ),
                          ))
                      .toList(),
                ),

              const SizedBox(height: 12),

              // Stats
              Row(
                children: [
                  Icon(Icons.thumb_up_outlined,
                      size: 14,
                      color: Theme.of(context).textTheme.bodySmall?.color),
                  const SizedBox(width: 4),
                  Text('${discussion['likes'] ?? 0}',
                      style: Theme.of(context)
                          .textTheme
                          .bodySmall
                          ?.copyWith(fontSize: 12)),
                  const SizedBox(width: 16),
                  Icon(Icons.comment_outlined,
                      size: 14,
                      color: Theme.of(context).textTheme.bodySmall?.color),
                  const SizedBox(width: 4),
                  Text('${discussion['replies'] ?? 0}',
                      style: Theme.of(context)
                          .textTheme
                          .bodySmall
                          ?.copyWith(fontSize: 12)),
                  const SizedBox(width: 16),
                  Icon(Icons.visibility_outlined,
                      size: 14,
                      color: Theme.of(context).textTheme.bodySmall?.color),
                  const SizedBox(width: 4),
                  Text('${discussion['views'] ?? 0}',
                      style: Theme.of(context)
                          .textTheme
                          .bodySmall
                          ?.copyWith(fontSize: 12)),
                  const Spacer(),
                  if (discussion['isHot'] == true)
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(
                        color: Colors.orange,
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: const Text(
                        'HOT',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 9,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Color _getCategoryColor(String category) {
    switch (category.toLowerCase()) {
      case 'career':
        return Colors.blue;
      case 'tech':
        return Colors.green;
      case 'life':
        return Colors.purple;
      default:
        return AppColors.primary;
    }
  }

  void _showSearchDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Search Discussions'),
        content: TextField(
          controller: _searchController,
          decoration: const InputDecoration(
            hintText: 'Enter keywords...',
            prefixIcon: Icon(Icons.search),
          ),
          autofocus: true,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              _performSearch(_searchController.text);
            },
            child: const Text('Search'),
          ),
        ],
      ),
    );
  }

  void _performSearch(String query) {
    if (query.trim().isEmpty) return;

    // Navigate to search results or filter current discussions
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Search Results for "$query"'),
        content: SizedBox(
          width: double.maxFinite,
          height: 300,
          child: StreamBuilder<QuerySnapshot>(
            stream: FirebaseFirestore.instance
                .collection('discussions')
                .where('title', isGreaterThanOrEqualTo: query)
                .where('title', isLessThanOrEqualTo: '$query\uf8ff')
                .snapshots(),
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return const Center(child: CircularProgressIndicator());
              }

              if (!snapshot.hasData || snapshot.data!.docs.isEmpty) {
                return const Center(
                  child: Text('No discussions found matching your search.'),
                );
              }

              final discussions = snapshot.data!.docs;
              return ListView.builder(
                itemCount: discussions.length,
                itemBuilder: (context, index) {
                  final discussion =
                      discussions[index].data() as Map<String, dynamic>;
                  discussion['id'] = discussions[index].id;
                  return ListTile(
                    title: Text(
                      discussion['title'] ?? 'Discussion',
                      style: const TextStyle(fontSize: 14),
                    ),
                    subtitle: Text(
                      discussion['content'] ?? '',
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(fontSize: 12),
                    ),
                    onTap: () {
                      Navigator.pop(context);
                      _openDiscussion(discussion);
                    },
                  );
                },
              );
            },
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }

  void _createNewDiscussion() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (context) => Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
        ),
        child: _buildCreateDiscussionForm(),
      ),
    );
  }

  Widget _buildCreateDiscussionForm() {
    final titleController = TextEditingController();
    final contentController = TextEditingController();
    String selectedCategory = 'Career';

    return StatefulBuilder(
      builder: (context, setState) => Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Start New Discussion',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 24),

            // Category selection
            DropdownButtonFormField<String>(
              initialValue: selectedCategory,
              decoration: const InputDecoration(
                labelText: 'Category',
                prefixIcon: Icon(Icons.category),
              ),
              items: ['Career', 'Tech', 'Life', 'General']
                  .map((category) => DropdownMenuItem(
                        value: category,
                        child: Text(category),
                      ))
                  .toList(),
              onChanged: (value) => setState(() => selectedCategory = value!),
            ),

            const SizedBox(height: 16),

            // Title
            TextField(
              controller: titleController,
              decoration: const InputDecoration(
                labelText: 'Discussion Title',
                prefixIcon: Icon(Icons.title),
              ),
            ),

            const SizedBox(height: 16),

            // Content
            TextField(
              controller: contentController,
              decoration: const InputDecoration(
                labelText: 'What would you like to discuss?',
                prefixIcon: Icon(Icons.message),
              ),
              maxLines: 4,
            ),

            const SizedBox(height: 24),

            // Actions
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text('Cancel'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () {
                      Navigator.pop(context);
                      _submitDiscussion(
                        titleController.text,
                        contentController.text,
                        selectedCategory,
                      );
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primary,
                    ),
                    child: const Text('Post',
                        style: TextStyle(color: Colors.white)),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _submitDiscussion(String title, String content, String category) async {
    if (title.isEmpty || content.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please fill in all fields')),
      );
      return;
    }

    try {
      final user = FirebaseAuth.instance.currentUser;
      if (user == null) return;

      // Get user data
      final userDoc = await FirebaseFirestore.instance
          .collection('users')
          .doc(user.uid)
          .get();
      final userData = userDoc.data() ?? {};

      // Create discussion document
      await FirebaseFirestore.instance.collection('discussions').add({
        'title': title,
        'content': content,
        'category': category,
        'authorId': user.uid,
        'author': userData['fullName'] ?? 'Anonymous',
        'authorImage': userData['profileImageUrl'],
        'createdAt': FieldValue.serverTimestamp(),
        'likes': 0,
        'replies': 0,
        'views': 0,
        'isHot': false,
        'tags': _extractTags(content),
      });

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Discussion "$title" posted successfully!'),
            backgroundColor: Theme.of(context).primaryColor,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Failed to post discussion. Please try again.'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  List<String> _extractTags(String content) {
    // Simple tag extraction from content
    final words = content.toLowerCase().split(' ');
    final commonTags = [
      'career',
      'tech',
      'startup',
      'remote',
      'programming',
      'life',
      'work'
    ];
    return words.where((word) => commonTags.contains(word)).take(3).toList();
  }

  void _openDiscussion(Map<String, dynamic> discussion) {
    context.push('/discussion/${discussion['id']}');
  }

  Widget _buildQASection() {
    return Column(
      children: [
        // Add Question Button
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(16),
          child: ElevatedButton.icon(
            onPressed: () => _createNewQuestion(),
            icon: const Icon(Icons.help_outline),
            label: const Text('Ask a Question'),
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primary,
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(vertical: 12),
            ),
          ),
        ),
        // Q&A List from Firestore
        Expanded(
          child: StreamBuilder<QuerySnapshot>(
            stream: FirebaseFirestore.instance
                .collection('questions')
                .orderBy('createdAt', descending: true)
                .snapshots(),
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return const Center(child: CircularProgressIndicator());
              }

              if (!snapshot.hasData || snapshot.data!.docs.isEmpty) {
                return Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.help_outline,
                        size: 80,
                        color: Theme.of(context).textTheme.bodySmall?.color,
                      ),
                      const SizedBox(height: 16),
                      Text(
                        'No questions yet',
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'Be the first to ask a question!',
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                              color:
                                  Theme.of(context).textTheme.bodySmall?.color,
                            ),
                      ),
                    ],
                  ),
                );
              }

              final questions = snapshot.data!.docs;
              return ListView.builder(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                itemCount: questions.length,
                itemBuilder: (context, index) {
                  final qa = questions[index].data() as Map<String, dynamic>;
                  qa['id'] = questions[index].id;
                  return _buildQACard(qa);
                },
              );
            },
          ),
        ),
      ],
    );
  }

  Widget _buildQACard(Map<String, dynamic> qa) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: InkWell(
        onTap: () => _openQA(qa),
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Question
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: AppColors.primary.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Icon(
                      Icons.help_outline,
                      color: AppColors.primary,
                      size: 20,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          qa['question'],
                          style: const TextStyle(
                            fontSize: 14,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Row(
                          children: [
                            Text(
                              'by ${qa['author']}',
                              style: TextStyle(
                                color: Colors.grey[600],
                                fontSize: 12,
                              ),
                            ),
                            const SizedBox(width: 8),
                            Text(
                              qa['timeAgo'],
                              style: TextStyle(
                                color: Colors.grey[600],
                                fontSize: 12,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  if (qa['hasAcceptedAnswer'] == true)
                    Container(
                      padding: const EdgeInsets.all(4),
                      decoration: const BoxDecoration(
                        color: Colors.green,
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.check,
                        color: Colors.white,
                        size: 16,
                      ),
                    ),
                ],
              ),

              const SizedBox(height: 12),

              // Tags
              Wrap(
                spacing: 6,
                children: (qa['tags'] as List<String>)
                    .map((tag) => Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 8, vertical: 2),
                          decoration: BoxDecoration(
                            color: Colors.grey[200],
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            '#$tag',
                            style: const TextStyle(fontSize: 10),
                          ),
                        ))
                    .toList(),
              ),

              const SizedBox(height: 12),

              // Stats
              Row(
                children: [
                  Icon(Icons.thumb_up_outlined,
                      size: 16, color: Colors.grey[600]),
                  const SizedBox(width: 4),
                  Text('${qa['upvotes']}',
                      style: TextStyle(color: Colors.grey[600], fontSize: 12)),
                  const SizedBox(width: 16),
                  Icon(Icons.comment_outlined,
                      size: 16, color: Colors.grey[600]),
                  const SizedBox(width: 4),
                  Text('${qa['answers']} answers',
                      style: TextStyle(color: Colors.grey[600], fontSize: 12)),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _createNewQuestion() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (context) => Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
        ),
        child: _buildCreateQuestionForm(),
      ),
    );
  }

  Widget _buildCreateQuestionForm() {
    final questionController = TextEditingController();
    final detailsController = TextEditingController();
    String selectedCategory = 'Career';

    return StatefulBuilder(
      builder: (context, setState) => Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Ask a Question',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 24),

            // Category selection
            DropdownButtonFormField<String>(
              initialValue: selectedCategory,
              decoration: const InputDecoration(
                labelText: 'Category',
                prefixIcon: Icon(Icons.category),
              ),
              items: ['Career', 'Tech', 'Life', 'General']
                  .map((category) => DropdownMenuItem(
                        value: category,
                        child: Text(category),
                      ))
                  .toList(),
              onChanged: (value) => setState(() => selectedCategory = value!),
            ),

            const SizedBox(height: 16),

            // Question
            TextField(
              controller: questionController,
              decoration: const InputDecoration(
                labelText: 'Your Question',
                prefixIcon: Icon(Icons.help_outline),
              ),
            ),

            const SizedBox(height: 16),

            // Details
            TextField(
              controller: detailsController,
              decoration: const InputDecoration(
                labelText: 'Additional details (optional)',
                prefixIcon: Icon(Icons.description),
              ),
              maxLines: 3,
            ),

            const SizedBox(height: 24),

            // Actions
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text('Cancel'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () {
                      Navigator.pop(context);
                      _submitQuestion(
                        questionController.text,
                        detailsController.text,
                        selectedCategory,
                      );
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primary,
                    ),
                    child: const Text('Ask Question',
                        style: TextStyle(color: Colors.white)),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _submitQuestion(String question, String details, String category) async {
    if (question.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter your question')),
      );
      return;
    }

    try {
      final user = FirebaseAuth.instance.currentUser;
      if (user == null) return;

      // Get user data
      final userDoc = await FirebaseFirestore.instance
          .collection('users')
          .doc(user.uid)
          .get();
      final userData = userDoc.data() ?? {};

      // Create question document
      await FirebaseFirestore.instance.collection('questions').add({
        'question': question,
        'details': details,
        'category': category,
        'authorId': user.uid,
        'author': userData['fullName'] ?? 'Anonymous',
        'authorImage': userData['profileImageUrl'],
        'createdAt': FieldValue.serverTimestamp(),
        'upvotes': 0,
        'answers': 0,
        'hasAcceptedAnswer': false,
        'tags': _extractTags('$question $details'),
      });

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: const Text('Question posted successfully!'),
            backgroundColor: Theme.of(context).primaryColor,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Failed to post question. Please try again.'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  void _openQA(Map<String, dynamic> qa) {
    context.push('/qa/${qa['question']}');
  }
}
