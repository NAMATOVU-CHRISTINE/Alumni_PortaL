import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/models/news_model.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/widgets/custom_app_bar.dart';

class NewsFeedScreen extends StatefulWidget {
  const NewsFeedScreen({super.key});

  @override
  State<NewsFeedScreen> createState() => _NewsFeedScreenState();
}

class _NewsFeedScreenState extends State<NewsFeedScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final TextEditingController _searchController = TextEditingController();
  bool _isSearching = false;
  String _searchQuery = '';

  final List<String> _categories = [
    'All',
    'University',
    'Alumni',
    'Events',
    'Research',
    'Sports',
    'Technology',
  ];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: _categories.length, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  String _categorizeNews(NewsModel article) {
    // Use the article's category if it exists
    if (article.category != null) {
      final categoryName = article.category!.displayName;
      // Map the category to our tab categories
      if (_categories.contains(categoryName)) {
        return categoryName;
      }
    }

    // Fallback to keyword-based categorization with priority order
    final text =
        '${article.title ?? ''} ${article.content ?? ''} ${article.summary ?? ''}'
            .toLowerCase();

    // Check in priority order - most specific first
    if (text.contains('alumni') ||
        text.contains('graduate') ||
        text.contains('reunion') ||
        text.contains('former student')) {
      return 'Alumni';
    }

    if (text.contains('sport') ||
        text.contains('football') ||
        text.contains('basketball') ||
        text.contains('soccer') ||
        text.contains('athletics') ||
        text.contains('game') ||
        text.contains('match') ||
        text.contains('tournament') ||
        text.contains('championship') ||
        text.contains('team') ||
        text.contains('player')) {
      return 'Sports';
    }

    if (text.contains('technology') ||
        text.contains('tech') ||
        text.contains('software') ||
        text.contains('computer') ||
        text.contains('digital') ||
        text.contains('innovation') ||
        text.contains('ai') ||
        text.contains('artificial intelligence') ||
        text.contains('programming') ||
        text.contains('coding')) {
      return 'Technology';
    }

    if (text.contains('research') ||
        text.contains('study') ||
        text.contains('science') ||
        text.contains('discovery') ||
        text.contains('experiment') ||
        text.contains('laboratory') ||
        text.contains('publication') ||
        text.contains('journal')) {
      return 'Research';
    }

    if (text.contains('event') ||
        text.contains('ceremony') ||
        text.contains('celebration') ||
        text.contains('conference') ||
        text.contains('workshop') ||
        text.contains('seminar') ||
        text.contains('meeting') ||
        text.contains('gathering')) {
      return 'Events';
    }

    return 'University';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: AppColors.primary,
        foregroundColor: Colors.white,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () {
            if (_isSearching) {
              setState(() {
                _isSearching = false;
                _searchQuery = '';
                _searchController.clear();
              });
            } else if (context.canPop()) {
              context.pop();
            } else {
              context.go('/home');
            }
          },
          tooltip: _isSearching ? 'Close Search' : 'Back',
        ),
        title: _isSearching
            ? Container(
                height: 40,
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(20),
                ),
                child: TextField(
                  controller: _searchController,
                  autofocus: true,
                  style: const TextStyle(color: Colors.black),
                  decoration: InputDecoration(
                    hintText: 'Search news...',
                    hintStyle: TextStyle(color: Colors.grey[600]),
                    border: InputBorder.none,
                    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                    suffixIcon: _searchQuery.isNotEmpty
                        ? IconButton(
                            icon: Icon(Icons.clear, color: Colors.grey[600], size: 20),
                            onPressed: () {
                              setState(() {
                                _searchController.clear();
                                _searchQuery = '';
                              });
                            },
                          )
                        : null,
                  ),
                  onChanged: (value) {
                    setState(() {
                      _searchQuery = value.toLowerCase();
                    });
                  },
                ),
              )
            : const Text('News'),
        actions: [
          if (!_isSearching)
            IconButton(
              icon: const Icon(Icons.search),
              onPressed: () {
                setState(() {
                  _isSearching = true;
                });
              },
            ),
        ],
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(51),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                color: Colors.white,
                child: TabBar(
                  controller: _tabController,
                  isScrollable: true,
                  indicatorColor: AppColors.primary,
                  indicatorWeight: 3,
                  indicatorSize: TabBarIndicatorSize.label,
                  labelColor: Colors.black,
                  unselectedLabelColor: Colors.grey[600],
                  labelStyle: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 0.3,
                  ),
                  unselectedLabelStyle: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.normal,
                  ),
                  tabs: _categories.map((category) => Tab(
                    text: category,
                    height: 48,
                  )).toList(),
                ),
              ),
              Container(
                height: 3,
                child: Row(
                  children: [
                    Expanded(child: Container(color: AppColors.primary)),
                    Expanded(child: Container(color: AppColors.accent)),
                    Expanded(child: Container(color: AppColors.secondary)),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
      body: StreamBuilder<QuerySnapshot>(
        stream: FirebaseFirestore.instance
            .collection('news')
            .orderBy('publishedAt', descending: true)
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
                    Icons.article_outlined,
                    size: 64,
                    color: Colors.grey[400],
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'No news available',
                    style: TextStyle(
                      color: Colors.grey[600],
                      fontSize: 16,
                    ),
                  ),
                ],
              ),
            );
          }

          final news = snapshot.data!.docs
              .map((doc) => NewsModel.fromFirestore(doc))
              .toList();

          // Remove duplicates by ID first
          final seenIds = <String>{};
          final uniqueById = news.where((article) {
            if (article.id == null) return false;
            if (seenIds.contains(article.id!)) return false;
            seenIds.add(article.id!);
            return true;
          }).toList();

          // Remove duplicates by title (keep most recent)
          final seenTitles = <String>{};
          final uniqueNewsList = uniqueById.where((article) {
            final title = article.title?.trim().toLowerCase();
            if (title == null || title.isEmpty) return false;
            if (seenTitles.contains(title)) {
              print(
                  'Filtering duplicate: "${article.title}" (ID: ${article.id})');
              return false;
            }
            seenTitles.add(title);
            return true;
          }).toList();

          print(
              'Total docs: ${snapshot.data!.docs.length}, After dedup: ${uniqueNewsList.length}');

          return TabBarView(
            controller: _tabController,
            children: _categories.map((category) {
              if (category == 'All') {
                return _buildAllTabView(uniqueNewsList);
              } else {
                return _buildFilteredTabView(uniqueNewsList, category);
              }
            }).toList(),
          );
        },
      ),
    );
  }

  Widget _buildAllTabView(List<NewsModel> articles) {
    // Filter articles based on search query
    final filteredArticles = _searchQuery.isEmpty
        ? articles
        : articles.where((article) {
            final title = article.title?.toLowerCase() ?? '';
            final summary = article.summary?.toLowerCase() ?? '';
            final content = article.content?.toLowerCase() ?? '';
            return title.contains(_searchQuery) ||
                summary.contains(_searchQuery) ||
                content.contains(_searchQuery);
          }).toList();

    if (filteredArticles.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.search_off,
              size: 64,
              color: Colors.grey[400],
            ),
            const SizedBox(height: 16),
            Text(
              _searchQuery.isEmpty ? 'No news available' : 'No results found for "$_searchQuery"',
              style: TextStyle(
                color: Colors.grey[600],
                fontSize: 16,
              ),
            ),
          ],
        ),
      );
    }

    final List<Widget> allWidgets = [];
    final Map<String, List<NewsModel>> groupedNews = {};

    print('Building All tab with ${filteredArticles.length} articles');

    for (var article in filteredArticles) {
      final category = _categorizeNews(article);
      if (!groupedNews.containsKey(category)) {
        groupedNews[category] = [];
      }
      groupedNews[category]!.add(article);
    }

    print('Grouped news: ${groupedNews.map((k, v) => MapEntry(k, v.length))}');

    // Check for duplicate titles within each category
    for (var category in groupedNews.keys) {
      final categoryArticles = groupedNews[category]!;
      final titleCounts = <String, int>{};

      for (var article in categoryArticles) {
        final title = article.title ?? 'Untitled';
        titleCounts[title] = (titleCounts[title] ?? 0) + 1;
      }

      final duplicates = titleCounts.entries.where((e) => e.value > 1).toList();
      if (duplicates.isNotEmpty) {
        print('⚠️ DUPLICATES in $category:');
        for (var dup in duplicates) {
          print('  "${dup.key}" appears ${dup.value} times');
          // Show the IDs of duplicate articles
          final dupArticles =
              categoryArticles.where((a) => a.title == dup.key).toList();
          print('  IDs: ${dupArticles.map((a) => a.id).join(", ")}');
        }
      }
    }

    // Sort categories to match tab order
    final sortedCategories = _categories
        .skip(1)
        .where((cat) => groupedNews.containsKey(cat))
        .toList();

    print('Sorted categories: $sortedCategories');

    for (var category in sortedCategories) {
      final categoryArticles = groupedNews[category]!;

      print('Adding ${categoryArticles.length} articles for $category');

      allWidgets.add(
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 12),
          child: Row(
            children: [
              Container(
                width: 4,
                height: 24,
                decoration: BoxDecoration(
                  color: _getCategoryColor(category.toLowerCase()),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              const SizedBox(width: 8),
              Text(
                category,
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: _getCategoryColor(category.toLowerCase()),
                ),
              ),
              const SizedBox(width: 8),
              Text(
                '(${categoryArticles.length})',
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.grey[600],
                ),
              ),
            ],
          ),
        ),
      );

      for (var article in categoryArticles) {
        allWidgets.add(_buildNewsCard(article));
      }

      allWidgets.add(const SizedBox(height: 8));
    }

    print('Total widgets in All tab: ${allWidgets.length}');

    if (allWidgets.isEmpty) {
      return const Center(child: Text('No news available'));
    }

    return ListView(
      padding: const EdgeInsets.all(16),
      children: allWidgets,
    );
  }

  Widget _buildFilteredTabView(List<NewsModel> articles, String category) {
    // Filter by category first
    final categoryFiltered = articles
        .where((article) => _categorizeNews(article) == category)
        .toList();

    // Then filter by search query
    final filteredNews = _searchQuery.isEmpty
        ? categoryFiltered
        : categoryFiltered.where((article) {
            final title = article.title?.toLowerCase() ?? '';
            final summary = article.summary?.toLowerCase() ?? '';
            final content = article.content?.toLowerCase() ?? '';
            return title.contains(_searchQuery) ||
                summary.contains(_searchQuery) ||
                content.contains(_searchQuery);
          }).toList();

    if (filteredNews.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              _searchQuery.isEmpty ? Icons.article_outlined : Icons.search_off,
              size: 64,
              color: Colors.grey[400],
            ),
            const SizedBox(height: 16),
            Text(
              _searchQuery.isEmpty
                  ? 'No $category news available'
                  : 'No results found for "$_searchQuery" in $category',
              style: TextStyle(
                color: Colors.grey[600],
                fontSize: 16,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: filteredNews.length,
      itemBuilder: (context, index) {
        return _buildNewsCard(filteredNews[index]);
      },
    );
  }

  Widget _buildNewsCard(NewsModel article) {
    return Card(
      key: ValueKey(article.id),
      margin: const EdgeInsets.only(bottom: 16),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: () => context.push('/article/${article.id}'),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (article.imageUrl != null && article.imageUrl!.isNotEmpty)
              CachedNetworkImage(
                imageUrl: article.imageUrl!,
                height: 180,
                width: double.infinity,
                fit: BoxFit.cover,
                errorWidget: (context, url, error) => Container(
                  height: 180,
                  width: double.infinity,
                  color: Colors.grey[300],
                  child: const Icon(
                    Icons.image_not_supported,
                    size: 50,
                    color: Colors.grey,
                  ),
                ),
                placeholder: (context, url) => Container(
                  height: 180,
                  width: double.infinity,
                  color: Colors.grey[200],
                  child: const Center(
                    child: CircularProgressIndicator(),
                  ),
                ),
              ),
            Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      if (article.category != null)
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 10,
                            vertical: 6,
                          ),
                          decoration: BoxDecoration(
                            color: _getCategoryColor(article.category!.name),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Text(
                            article.category!.displayName.toUpperCase(),
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 10,
                              fontWeight: FontWeight.bold,
                              letterSpacing: 0.5,
                            ),
                          ),
                        ),
                      const Spacer(),
                      Icon(
                        Icons.visibility,
                        size: 14,
                        color: Colors.grey[600],
                      ),
                      const SizedBox(width: 4),
                      Text(
                        '${article.viewCount}',
                        style: TextStyle(
                          fontSize: 12,
                          color: Colors.grey[600],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(
                    article.title ?? 'News',
                    style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),
                  if (article.summary != null)
                    Text(
                      article.summary!,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        color: AppColors.textSecondary,
                      ),
                    ),
                  const SizedBox(height: 8),
                  Text(
                    article.timeAgo,
                    style: const TextStyle(
                      fontSize: 12,
                      color: AppColors.textSecondary,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Color _getCategoryColor(String category) {
    switch (category.toLowerCase()) {
      case 'university':
        return Colors.blue;
      case 'alumni':
        return AppColors.primary;
      case 'events':
        return Colors.purple;
      case 'research':
        return Colors.green;
      case 'sports':
        return Colors.orange;
      case 'technology':
        return Colors.teal;
      default:
        return Colors.grey;
    }
  }
}
