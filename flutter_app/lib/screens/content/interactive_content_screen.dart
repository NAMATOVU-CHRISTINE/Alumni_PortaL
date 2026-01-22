import 'package:flutter/material.dart';
import 'package:alumni_portal/config/theme.dart';

class InteractiveContentScreen extends StatefulWidget {
  const InteractiveContentScreen({super.key});

  @override
  State<InteractiveContentScreen> createState() =>
      _InteractiveContentScreenState();
}

class _InteractiveContentScreenState extends State<InteractiveContentScreen>
    with TickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      body: Column(
        children: [
          // Header Section - Above tabs
          Container(
            width: double.infinity,
            padding: const EdgeInsets.fromLTRB(16, 48, 16, 16),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [AppColors.primary, AppColors.primaryDark],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    IconButton(
                      icon: const Icon(Icons.arrow_back,
                          color: Colors.white, size: 22),
                      onPressed: () => Navigator.pop(context),
                      padding: EdgeInsets.zero,
                      constraints: const BoxConstraints(),
                    ),
                    const SizedBox(width: 12),
                    Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: Colors.white.withValues(alpha: 0.2),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.play_circle_outline,
                        color: Colors.white,
                        size: 24,
                      ),
                    ),
                    const SizedBox(width: 12),
                    const Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Interactive Content',
                            style: TextStyle(
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                              color: Colors.white,
                            ),
                          ),
                          Text(
                            'Engage with live events, webinars & more',
                            style: TextStyle(
                              color: Colors.white70,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          // Tabs
          Container(
            color: Colors.white,
            child: TabBar(
              controller: _tabController,
              labelColor: AppColors.primary,
              unselectedLabelColor: Colors.grey,
              indicatorColor: AppColors.primary,
              isScrollable: true,
              tabs: const [
                Tab(text: 'Live Events'),
                Tab(text: 'Webinars'),
                Tab(text: 'Podcasts'),
                Tab(text: 'Success Stories'),
              ],
            ),
          ),

          // Tab Content
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: [
                _buildLiveEventsTab(),
                _buildWebinarsTab(),
                _buildPodcastsTab(),
                _buildSuccessStoriesTab(),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLiveEventsTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Live Now Section
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [Colors.red, Colors.red.shade700],
              ),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(6),
                  decoration: const BoxDecoration(
                    color: Colors.white,
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.live_tv, color: Colors.red, size: 20),
                ),
                const SizedBox(width: 10),
                const Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        'LIVE NOW',
                        style: TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                          fontSize: 13,
                        ),
                      ),
                      Text(
                        'Alumni Career Panel Discussion',
                        style: TextStyle(color: Colors.white70, fontSize: 12),
                      ),
                    ],
                  ),
                ),
                ElevatedButton(
                  onPressed: () => _joinLiveEvent(),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.white,
                    foregroundColor: Colors.red,
                    padding:
                        const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    textStyle: const TextStyle(fontSize: 13),
                  ),
                  child: const Text('Join'),
                ),
              ],
            ),
          ),

          const SizedBox(height: 20),

          // Upcoming Events
          const Text(
            'Upcoming Events',
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),

          ...List.generate(
              3,
              (index) => _buildEventCard({
                    'title': [
                      'Tech Talk: AI in Healthcare',
                      'Entrepreneurship Workshop',
                      'Alumni Networking Night'
                    ][index],
                    'speaker': [
                      'Dr. Sarah Nakato',
                      'John Mukasa',
                      'Multiple Speakers'
                    ][index],
                    'date': [
                      'Today, 7:00 PM',
                      'Tomorrow, 2:00 PM',
                      'Friday, 6:00 PM'
                    ][index],
                    'attendees': [156, 89, 234][index],
                    'isLive': index == 0,
                  })),
        ],
      ),
    );
  }

  Widget _buildWebinarsTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Featured Webinar
          Card(
            elevation: 3,
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  height: 140,
                  decoration: BoxDecoration(
                    borderRadius:
                        const BorderRadius.vertical(top: Radius.circular(12)),
                    gradient: LinearGradient(
                      colors: [AppColors.primary, AppColors.primaryDark],
                    ),
                  ),
                  child: const Center(
                    child: Icon(Icons.play_circle_fill,
                        size: 48, color: Colors.white),
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 8, vertical: 3),
                        decoration: BoxDecoration(
                          color: Colors.orange,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const Text(
                          'FEATURED',
                          style: TextStyle(
                            color: Colors.white,
                            fontSize: 9,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                      const SizedBox(height: 6),
                      const Text(
                        'Building Successful Startups in Uganda',
                        style: TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 6),
                      const Text(
                        'Learn from successful entrepreneurs about building and scaling startups in the Ugandan market.',
                        style: TextStyle(fontSize: 13),
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                      const SizedBox(height: 10),
                      Row(
                        children: [
                          const Icon(Icons.person,
                              size: 14, color: Colors.grey),
                          const SizedBox(width: 4),
                          const Text('Grace Namuli',
                              style: TextStyle(fontSize: 11)),
                          const SizedBox(width: 12),
                          const Icon(Icons.access_time,
                              size: 14, color: Colors.grey),
                          const SizedBox(width: 4),
                          const Text('45 min', style: TextStyle(fontSize: 11)),
                        ],
                      ),
                      const SizedBox(height: 10),
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton(
                          onPressed: () => _watchWebinar('featured'),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: AppColors.primary,
                          ),
                          child: const Text('Watch Now',
                              style: TextStyle(color: Colors.white)),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(height: 24),

          // Categories
          const Text(
            'Browse by Category',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          SizedBox(
            height: 100,
            child: ListView(
              scrollDirection: Axis.horizontal,
              children: [
                _buildCategoryChip('Technology', Icons.computer, Colors.blue),
                _buildCategoryChip('Business', Icons.business, Colors.green),
                _buildCategoryChip('Career', Icons.work, Colors.orange),
                _buildCategoryChip('Leadership', Icons.star, Colors.purple),
                _buildCategoryChip('Innovation', Icons.lightbulb, Colors.amber),
              ],
            ),
          ),

          const SizedBox(height: 24),

          // Recent Webinars
          const Text(
            'Recent Webinars',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          ...List.generate(
              4,
              (index) => _buildWebinarCard({
                    'title': [
                      'Digital Marketing Strategies',
                      'Financial Planning for Young Professionals',
                      'Remote Work Best Practices',
                      'Data Science Career Path'
                    ][index],
                    'speaker': [
                      'Mary Nakirya',
                      'Peter Okello',
                      'Jane Akello',
                      'David Ssali'
                    ][index],
                    'duration': ['30 min', '45 min', '25 min', '60 min'][index],
                    'views': [1234, 856, 2341, 567][index],
                    'rating': [4.8, 4.6, 4.9, 4.7][index],
                  })),
        ],
      ),
    );
  }

  Widget _buildPodcastsTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Now Playing
          Card(
            elevation: 2,
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Container(
                    width: 60,
                    height: 60,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(8),
                      gradient: LinearGradient(
                        colors: [AppColors.primary, AppColors.primaryDark],
                      ),
                    ),
                    child: const Icon(Icons.podcasts,
                        color: Colors.white, size: 30),
                  ),
                  const SizedBox(width: 12),
                  const Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          'Alumni Success Stories',
                          style: TextStyle(fontWeight: FontWeight.bold),
                        ),
                        Text(
                          'Episode 12: From Student to CEO',
                          style: TextStyle(fontSize: 12, color: Colors.grey),
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    onPressed: () => _playPodcast(),
                    icon: const Icon(Icons.play_arrow, size: 32),
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 24),

          // Podcast Series
          const Text(
            'Podcast Series',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          ...List.generate(
              3,
              (index) => _buildPodcastSeriesCard({
                    'title': [
                      'Alumni Success Stories',
                      'Tech Talks with MUST Grads',
                      'Career Guidance Sessions'
                    ][index],
                    'description': [
                      'Inspiring stories from successful alumni',
                      'Technical discussions and insights',
                      'Career advice and mentorship'
                    ][index],
                    'episodes': [24, 18, 31][index],
                    'subscribers': [1234, 856, 2341][index],
                  })),

          const SizedBox(height: 24),

          // Latest Episodes
          const Text(
            'Latest Episodes',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          ...List.generate(
              5,
              (index) => _buildPodcastEpisodeCard({
                    'title': [
                      'Building a Tech Startup in Uganda',
                      'The Future of Remote Work',
                      'Women in STEM Leadership',
                      'Sustainable Business Practices',
                      'Digital Transformation in Africa'
                    ][index],
                    'series': [
                      'Alumni Success Stories',
                      'Tech Talks',
                      'Leadership Series',
                      'Business Insights',
                      'Tech Talks'
                    ][index],
                    'duration': [
                      '32 min',
                      '28 min',
                      '41 min',
                      '35 min',
                      '29 min'
                    ][index],
                    'date': [
                      '2 days ago',
                      '5 days ago',
                      '1 week ago',
                      '1 week ago',
                      '2 weeks ago'
                    ][index],
                  })),
        ],
      ),
    );
  }

  Widget _buildSuccessStoriesTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Featured Story
          Card(
            elevation: 3,
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  height: 200,
                  decoration: BoxDecoration(
                    borderRadius:
                        const BorderRadius.vertical(top: Radius.circular(12)),
                    gradient: LinearGradient(
                      colors: [Colors.purple, Colors.purple.shade700],
                    ),
                  ),
                  child: const Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.star, size: 48, color: Colors.white),
                        SizedBox(height: 8),
                        Text(
                          'Featured Success Story',
                          style: TextStyle(
                            color: Colors.white,
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'From MUST Graduate to Silicon Valley Executive',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'Sarah Nakato shares her journey from studying Computer Science at MUST to becoming a VP of Engineering at a major tech company.',
                      ),
                      const SizedBox(height: 12),
                      Row(
                        children: [
                          CircleAvatar(
                            radius: 16,
                            backgroundColor:
                                AppColors.primary.withValues(alpha: 0.1),
                            child: const Text('SN',
                                style: TextStyle(fontSize: 12)),
                          ),
                          const SizedBox(width: 8),
                          const Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Text(
                                  'Sarah Nakato',
                                  style: TextStyle(
                                      fontWeight: FontWeight.w600,
                                      fontSize: 12),
                                ),
                                Text(
                                  'Class of 2015 • Computer Science',
                                  style: TextStyle(
                                      fontSize: 10, color: Colors.grey),
                                ),
                              ],
                            ),
                          ),
                          ElevatedButton(
                            onPressed: () => _readStory('featured'),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: AppColors.primary,
                            ),
                            child: const Text('Read Story',
                                style: TextStyle(color: Colors.white)),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(height: 24),

          // Story Categories
          const Text(
            'Browse Stories',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          GridView.count(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            crossAxisCount: 2,
            childAspectRatio: 1.5,
            crossAxisSpacing: 12,
            mainAxisSpacing: 12,
            children: [
              _buildStoryCategoryCard(
                  'Entrepreneurship', Icons.business, Colors.green, 23),
              _buildStoryCategoryCard(
                  'Technology', Icons.computer, Colors.blue, 18),
              _buildStoryCategoryCard(
                  'Healthcare', Icons.local_hospital, Colors.red, 12),
              _buildStoryCategoryCard(
                  'Education', Icons.school, Colors.orange, 15),
            ],
          ),

          const SizedBox(height: 24),

          // Recent Stories
          const Text(
            'Recent Success Stories',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          ...List.generate(
              4,
              (index) => _buildSuccessStoryCard({
                    'title': [
                      'Building Uganda\'s First EdTech Unicorn',
                      'Leading Digital Transformation in Banking',
                      'From Engineer to Award-Winning Doctor',
                      'Creating Sustainable Agriculture Solutions'
                    ][index],
                    'author': [
                      'John Mukasa',
                      'Grace Namuli',
                      'Dr. Peter Okello',
                      'Mary Nakirya'
                    ][index],
                    'class': [
                      'Class of 2018',
                      'Class of 2016',
                      'Class of 2014',
                      'Class of 2017'
                    ][index],
                    'field': [
                      'Computer Science',
                      'Business Administration',
                      'Biomedical Engineering',
                      'Agricultural Engineering'
                    ][index],
                    'readTime': [
                      '5 min read',
                      '7 min read',
                      '6 min read',
                      '4 min read'
                    ][index],
                  })),
        ],
      ),
    );
  }

  Widget _buildEventCard(Map<String, dynamic> event) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: event['isLive']
                        ? Colors.red.withValues(alpha: 0.1)
                        : AppColors.primary.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Icon(
                    event['isLive'] ? Icons.live_tv : Icons.event,
                    color: event['isLive'] ? Colors.red : AppColors.primary,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        event['title'],
                        style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text(
                        'by ${event['speaker']}',
                        style: TextStyle(color: Colors.grey[600]),
                      ),
                    ],
                  ),
                ),
                if (event['isLive'])
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: Colors.red,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Text(
                      'LIVE',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Icon(Icons.access_time, size: 16, color: Colors.grey[600]),
                const SizedBox(width: 4),
                Text(event['date'], style: TextStyle(color: Colors.grey[600])),
                const SizedBox(width: 16),
                Icon(Icons.people, size: 16, color: Colors.grey[600]),
                const SizedBox(width: 4),
                Text('${event['attendees']} attending',
                    style: TextStyle(color: Colors.grey[600])),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () => _viewEventDetails(event),
                    child: const Text('Details'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () => _joinEvent(event),
                    style: ElevatedButton.styleFrom(
                      backgroundColor:
                          event['isLive'] ? Colors.red : AppColors.primary,
                    ),
                    child: Text(
                      event['isLive'] ? 'Join Live' : 'Register',
                      style: const TextStyle(color: Colors.white),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCategoryChip(String label, IconData icon, Color color) {
    return Container(
      margin: const EdgeInsets.only(right: 12),
      child: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: color.withValues(alpha: 0.1),
              shape: BoxShape.circle,
            ),
            child: Icon(icon, color: color, size: 24),
          ),
          const SizedBox(height: 8),
          Text(
            label,
            style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w500),
          ),
        ],
      ),
    );
  }

  Widget _buildWebinarCard(Map<String, dynamic> webinar) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: ListTile(
        leading: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: AppColors.primary.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(Icons.play_circle_fill, color: AppColors.primary),
        ),
        title: Text(webinar['title'],
            style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('by ${webinar['speaker']}'),
            Row(
              children: [
                Text('${webinar['duration']} • ${webinar['views']} views'),
                const SizedBox(width: 8),
                Row(
                  children: [
                    const Icon(Icons.star, size: 12, color: Colors.amber),
                    Text('${webinar['rating']}',
                        style: const TextStyle(fontSize: 12)),
                  ],
                ),
              ],
            ),
          ],
        ),
        trailing: const Icon(Icons.play_arrow),
        onTap: () => _watchWebinar(webinar['title']),
      ),
    );
  }

  Widget _buildPodcastSeriesCard(Map<String, dynamic> series) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: ListTile(
        leading: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: AppColors.primary.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(Icons.podcasts, color: AppColors.primary),
        ),
        title: Text(series['title'],
            style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(series['description']),
            Text(
                '${series['episodes']} episodes • ${series['subscribers']} subscribers'),
          ],
        ),
        trailing: const Icon(Icons.arrow_forward),
        onTap: () => _viewPodcastSeries(series),
      ),
    );
  }

  Widget _buildPodcastEpisodeCard(Map<String, dynamic> episode) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        leading: const Icon(Icons.play_circle_outline),
        title: Text(episode['title'],
            style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Text(
            '${episode['series']} • ${episode['duration']} • ${episode['date']}'),
        trailing: IconButton(
          icon: const Icon(Icons.more_vert),
          onPressed: () => _showEpisodeOptions(episode),
        ),
        onTap: () => _playEpisode(episode),
      ),
    );
  }

  Widget _buildStoryCategoryCard(
      String title, IconData icon, Color color, int count) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: InkWell(
        onTap: () => _browseStoriesByCategory(title),
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: color.withValues(alpha: 0.1),
                  shape: BoxShape.circle,
                ),
                child: Icon(icon, color: color, size: 24),
              ),
              const SizedBox(height: 8),
              Text(
                title,
                style: const TextStyle(fontWeight: FontWeight.bold),
                textAlign: TextAlign.center,
              ),
              Text(
                '$count stories',
                style: TextStyle(fontSize: 12, color: Colors.grey[600]),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSuccessStoryCard(Map<String, dynamic> story) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: AppColors.primary.withValues(alpha: 0.1),
          child: Text(
            story['author'].substring(0, 1).toUpperCase(),
            style: TextStyle(
                color: AppColors.primary, fontWeight: FontWeight.bold),
          ),
        ),
        title: Text(story['title'],
            style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('${story['author']} • ${story['class']}'),
            Text('${story['field']} • ${story['readTime']}'),
          ],
        ),
        trailing: const Icon(Icons.arrow_forward),
        onTap: () => _readStory(story['title']),
      ),
    );
  }

  // Action methods
  void _joinLiveEvent() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Joining live event...')),
    );
  }

  void _viewEventDetails(Map<String, dynamic> event) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Viewing details for ${event['title']}')),
    );
  }

  void _joinEvent(Map<String, dynamic> event) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Registered for ${event['title']}')),
    );
  }

  void _watchWebinar(String title) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Watching: $title')),
    );
  }

  void _playPodcast() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Playing podcast...')),
    );
  }

  void _viewPodcastSeries(Map<String, dynamic> series) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Viewing series: ${series['title']}')),
    );
  }

  void _playEpisode(Map<String, dynamic> episode) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Playing: ${episode['title']}')),
    );
  }

  void _showEpisodeOptions(Map<String, dynamic> episode) {
    showModalBottomSheet(
      context: context,
      builder: (context) => Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          ListTile(
            leading: const Icon(Icons.play_arrow),
            title: const Text('Play'),
            onTap: () {
              Navigator.pop(context);
              _playEpisode(episode);
            },
          ),
          ListTile(
            leading: const Icon(Icons.download),
            title: const Text('Download'),
            onTap: () {
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Downloading episode...')),
              );
            },
          ),
          ListTile(
            leading: const Icon(Icons.share),
            title: const Text('Share'),
            onTap: () {
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Sharing episode...')),
              );
            },
          ),
        ],
      ),
    );
  }

  void _readStory(String title) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Reading story: $title')),
    );
  }

  void _browseStoriesByCategory(String category) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Browsing $category stories')),
    );
  }
}
