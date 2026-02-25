import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/widgets/app_bar_decoration.dart';

class EnhancedCareerCenter extends StatefulWidget {
  const EnhancedCareerCenter({super.key});

  @override
  State<EnhancedCareerCenter> createState() => _EnhancedCareerCenterState();
}

class _EnhancedCareerCenterState extends State<EnhancedCareerCenter>
    with TickerProviderStateMixin {
  late TabController _tabController;
  AnimationController? _animationController;
  Animation<double>? _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 5, vsync: this);
    _animationController = AnimationController(
      duration: const Duration(milliseconds: 800),
      vsync: this,
    );
    _fadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _animationController!, curve: Curves.easeIn),
    );
    _animationController!.forward();
  }

  @override
  void dispose() {
    _tabController.dispose();
    _animationController?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: MustAppBar(
        title: const Text('Career Center'),
      ),
      body: Column(
        children: [
          // Header with decorative line
          Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [
                  AppColors.primary.withOpacity(0.05),
                  AppColors.accent.withOpacity(0.05),
                ],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
            ),
            child: Column(
              children: [
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 16, 20, 12),
                  child: Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            colors: [AppColors.primary, AppColors.accent],
                          ),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const Icon(
                          Icons.rocket_launch,
                          color: Colors.white,
                          size: 28,
                        ),
                      ),
                      const SizedBox(width: 12),
                      const Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Fuel Your Career',
                              style: TextStyle(
                                fontSize: 20,
                                fontWeight: FontWeight.bold,
                                color: AppColors.primary,
                              ),
                            ),
                            SizedBox(height: 2),
                            Text(
                              'Tools & resources for success',
                              style: TextStyle(
                                fontSize: 13,
                                color: AppColors.textSecondary,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                // Decorative line
                Container(
                  height: 3,
                  margin: const EdgeInsets.symmetric(horizontal: 20),
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [
                        AppColors.primary,
                        AppColors.accent,
                        AppColors.secondary,
                      ],
                    ),
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
                const SizedBox(height: 12),
              ],
            ),
          ),

          // Tabs
          Container(
            color: Colors.white,
            child: TabBar(
              controller: _tabController,
              isScrollable: true,
              labelColor: AppColors.primary,
              unselectedLabelColor: AppColors.textSecondary,
              indicatorColor: AppColors.accent,
              indicatorWeight: 3,
              labelStyle: const TextStyle(
                fontWeight: FontWeight.w600,
                fontSize: 14,
              ),
              tabs: const [
                Tab(text: 'Overview'),
                Tab(text: 'Resume'),
                Tab(text: 'Interview'),
                Tab(text: 'Salary'),
                Tab(text: 'Skills'),
              ],
            ),
          ),

          // Tab Content
          Expanded(
            child: _fadeAnimation != null
                ? FadeTransition(
                    opacity: _fadeAnimation!,
                    child: TabBarView(
                      controller: _tabController,
                      children: [
                        _buildOverviewTab(),
                        _buildResumeBuilderTab(),
                        _buildInterviewPrepTab(),
                        _buildSalaryInsightsTab(),
                        _buildSkillsAssessmentTab(),
                      ],
                    ),
                  )
                : TabBarView(
                    controller: _tabController,
                    children: [
                      _buildOverviewTab(),
                      _buildResumeBuilderTab(),
                      _buildInterviewPrepTab(),
                      _buildSalaryInsightsTab(),
                      _buildSkillsAssessmentTab(),
                    ],
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildOverviewTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Quick Stats Card
          Card(
            elevation: 2,
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Your Career Progress',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 16),
                  Row(
                    children: [
                      Expanded(
                        child: _buildStatItem('Profile Strength', '85%',
                            Icons.person, Colors.green),
                      ),
                      Expanded(
                        child: _buildStatItem(
                            'Applications', '12', Icons.work, Colors.blue),
                      ),
                      Expanded(
                        child: _buildStatItem(
                            'Interviews', '3', Icons.chat, Colors.orange),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 24),

          // Quick Actions Grid
          const Text(
            'Quick Actions',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          GridView.count(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            crossAxisCount: 2,
            childAspectRatio: 1.2,
            crossAxisSpacing: 12,
            mainAxisSpacing: 12,
            children: [
              _buildActionCard(
                'Build Resume',
                'Create professional resume',
                Icons.description,
                Colors.blue,
                () => _tabController.animateTo(1),
              ),
              _buildActionCard(
                'Practice Interview',
                'Prepare for interviews',
                Icons.mic,
                Colors.green,
                () => _tabController.animateTo(2),
              ),
              _buildActionCard(
                'Salary Research',
                'Know your market value',
                Icons.attach_money,
                Colors.orange,
                () => _tabController.animateTo(3),
              ),
              _buildActionCard(
                'Skills Test',
                'Assess your abilities',
                Icons.quiz,
                Colors.purple,
                () => _tabController.animateTo(4),
              ),
            ],
          ),

          const SizedBox(height: 24),

          // Recent Job Matches
          const Text(
            'Recommended Jobs',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          ...List.generate(
              3,
              (index) => _buildJobCard({
                    'title': [
                      'Software Engineer',
                      'Product Manager',
                      'Data Analyst'
                    ][index],
                    'company': ['TechCorp', 'StartupXYZ', 'DataCo'][index],
                    'location': 'Kampala, Uganda',
                    'salary': ['UGX 3-5M', 'UGX 4-6M', 'UGX 2.5-4M'][index],
                    'match': [95, 87, 92][index],
                  })),
        ],
      ),
    );
  }

  Widget _buildResumeBuilderTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Resume Templates
          const Text(
            'Choose a Template',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          SizedBox(
            height: 200,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              itemCount: 4,
              itemBuilder: (context, index) {
                final templates = ['Modern', 'Classic', 'Creative', 'Minimal'];
                return Container(
                  width: 140,
                  margin: const EdgeInsets.only(right: 12),
                  child: Card(
                    elevation: 2,
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12)),
                    child: InkWell(
                      onTap: () => _selectTemplate(templates[index]),
                      borderRadius: BorderRadius.circular(12),
                      child: Column(
                        children: [
                          Expanded(
                            child: Container(
                              decoration: BoxDecoration(
                                borderRadius: const BorderRadius.vertical(
                                    top: Radius.circular(12)),
                                color: Colors.grey[200],
                              ),
                              child: const Center(
                                child: Icon(Icons.description,
                                    size: 48, color: Colors.grey),
                              ),
                            ),
                          ),
                          Padding(
                            padding: const EdgeInsets.all(12),
                            child: Text(
                              templates[index],
                              style:
                                  const TextStyle(fontWeight: FontWeight.w600),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                );
              },
            ),
          ),

          const SizedBox(height: 24),

          // Resume Sections
          const Text(
            'Resume Sections',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          ...[
            'Personal Information',
            'Professional Summary',
            'Work Experience',
            'Education',
            'Skills',
            'Projects'
          ].map((section) => _buildResumeSectionCard(section)),

          const SizedBox(height: 24),

          // Action Buttons
          Row(
            children: [
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: () => _previewResume(),
                  icon: const Icon(Icons.preview),
                  label: const Text('Preview'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: ElevatedButton.icon(
                  onPressed: () => _downloadResume(),
                  icon: const Icon(Icons.download),
                  label: const Text('Download'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primary,
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildInterviewPrepTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Interview Types
          const Text(
            'Practice by Interview Type',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          ...[
            'Technical Interview',
            'Behavioral Interview',
            'Case Study',
            'System Design'
          ].map((type) => _buildInterviewTypeCard(type)),

          const SizedBox(height: 24),

          // Common Questions
          const Text(
            'Common Questions',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          Card(
            elevation: 2,
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Column(
              children: [
                ...[
                  'Tell me about yourself',
                  'Why do you want this job?',
                  'What are your strengths?',
                  'Where do you see yourself in 5 years?'
                ].map((question) => ListTile(
                      leading: const Icon(Icons.help_outline),
                      title: Text(question),
                      trailing: const Icon(Icons.play_arrow),
                      onTap: () => _practiceQuestion(question),
                    )),
              ],
            ),
          ),

          const SizedBox(height: 24),

          // Mock Interview
          Card(
            elevation: 2,
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                children: [
                  const Icon(Icons.videocam, size: 48, color: Colors.red),
                  const SizedBox(height: 16),
                  const Text(
                    'AI Mock Interview',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Practice with our AI interviewer and get instant feedback',
                    textAlign: TextAlign.center,
                    style: TextStyle(color: Colors.grey[600]),
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => _startMockInterview(),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red,
                    ),
                    child: const Text('Start Mock Interview',
                        style: TextStyle(color: Colors.white)),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSalaryInsightsTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Salary Calculator
          Card(
            elevation: 2,
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Salary Calculator',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 16),
                  DropdownButtonFormField<String>(
                    decoration: const InputDecoration(
                      labelText: 'Job Title',
                      prefixIcon: Icon(Icons.work),
                    ),
                    items: [
                      'Software Engineer',
                      'Product Manager',
                      'Data Analyst',
                      'Marketing Manager'
                    ]
                        .map((title) =>
                            DropdownMenuItem(value: title, child: Text(title)))
                        .toList(),
                    onChanged: (value) {},
                  ),
                  const SizedBox(height: 16),
                  DropdownButtonFormField<String>(
                    decoration: const InputDecoration(
                      labelText: 'Experience Level',
                      prefixIcon: Icon(Icons.timeline),
                    ),
                    items: [
                      'Entry Level',
                      'Mid Level',
                      'Senior Level',
                      'Executive'
                    ]
                        .map((level) =>
                            DropdownMenuItem(value: level, child: Text(level)))
                        .toList(),
                    onChanged: (value) {},
                  ),
                  const SizedBox(height: 16),
                  DropdownButtonFormField<String>(
                    decoration: const InputDecoration(
                      labelText: 'Location',
                      prefixIcon: Icon(Icons.location_on),
                    ),
                    items: ['Kampala', 'Entebbe', 'Jinja', 'Mbarara']
                        .map((location) => DropdownMenuItem(
                            value: location, child: Text(location)))
                        .toList(),
                    onChanged: (value) {},
                  ),
                  const SizedBox(height: 20),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: () => _calculateSalary(),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primary,
                      ),
                      child: const Text('Calculate Salary',
                          style: TextStyle(color: Colors.white)),
                    ),
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 24),

          // Salary Trends
          const Text(
            'Salary Trends',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          Card(
            elevation: 2,
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                children: [
                  _buildSalaryTrendItem('Software Engineer', 'UGX 2.5M - 8M',
                      '+15%', Colors.green),
                  _buildSalaryTrendItem(
                      'Product Manager', 'UGX 3M - 10M', '+12%', Colors.green),
                  _buildSalaryTrendItem(
                      'Data Analyst', 'UGX 2M - 6M', '+8%', Colors.green),
                  _buildSalaryTrendItem('Marketing Manager', 'UGX 2.5M - 7M',
                      '+5%', Colors.orange),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSkillsAssessmentTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Skill Categories
          const Text(
            'Assess Your Skills',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          ...['Technical Skills', 'Soft Skills', 'Leadership', 'Communication']
              .map((category) => _buildSkillCategoryCard(category)),

          const SizedBox(height: 24),

          // Recent Assessments
          const Text(
            'Your Recent Assessments',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),

          Card(
            elevation: 2,
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Column(
              children: [
                _buildAssessmentResultItem('JavaScript', 85, Colors.green),
                _buildAssessmentResultItem(
                    'Project Management', 78, Colors.blue),
                _buildAssessmentResultItem('Communication', 92, Colors.green),
                _buildAssessmentResultItem('Leadership', 65, Colors.orange),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatItem(
      String label, String value, IconData icon, Color color) {
    return Column(
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
          value,
          style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        Text(
          label,
          style: TextStyle(fontSize: 12, color: Colors.grey[600]),
          textAlign: TextAlign.center,
        ),
      ],
    );
  }

  Widget _buildActionCard(String title, String subtitle, IconData icon,
      Color color, VoidCallback onTap) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: color.withOpacity(0.1),
                  shape: BoxShape.circle,
                ),
                child: Icon(icon, color: color, size: 24),
              ),
              const SizedBox(height: 8),
              Flexible(
                child: Text(
                  title,
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 13,
                  ),
                  textAlign: TextAlign.center,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
              const SizedBox(height: 2),
              Flexible(
                child: Text(
                  subtitle,
                  style: TextStyle(fontSize: 11, color: Colors.grey[600]),
                  textAlign: TextAlign.center,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildJobCard(Map<String, dynamic> job) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: ListTile(
        leading: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: AppColors.primary.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(Icons.work, color: AppColors.primary),
        ),
        title: Text(job['title'],
            style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('${job['company']} • ${job['location']}'),
            Text(job['salary'], style: TextStyle(color: AppColors.primary)),
          ],
        ),
        trailing: Container(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
          decoration: BoxDecoration(
            color: Colors.green.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Text(
            '${job['match']}% match',
            style: const TextStyle(
              color: Colors.green,
              fontSize: 12,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
        onTap: () => _viewJob(job),
      ),
    );
  }

  Widget _buildResumeSectionCard(String section) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        leading: const Icon(Icons.edit),
        title: Text(section),
        trailing: const Icon(Icons.chevron_right),
        onTap: () => _editResumeSection(section),
      ),
    );
  }

  Widget _buildInterviewTypeCard(String type) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: ListTile(
        leading: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: AppColors.primary.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(Icons.quiz, color: AppColors.primary),
        ),
        title: Text(type, style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: const Text('Practice common questions'),
        trailing: const Icon(Icons.play_arrow),
        onTap: () => _startInterviewPractice(type),
      ),
    );
  }

  Widget _buildSalaryTrendItem(
      String role, String range, String trend, Color trendColor) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(role, style: const TextStyle(fontWeight: FontWeight.w600)),
                Text(range, style: TextStyle(color: Colors.grey[600])),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            decoration: BoxDecoration(
              color: trendColor.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              trend,
              style: TextStyle(
                color: trendColor,
                fontWeight: FontWeight.w600,
                fontSize: 12,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSkillCategoryCard(String category) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: ListTile(
        leading: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: AppColors.primary.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(Icons.psychology, color: AppColors.primary),
        ),
        title:
            Text(category, style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: const Text('Take assessment'),
        trailing: const Icon(Icons.arrow_forward),
        onTap: () => _startSkillAssessment(category),
      ),
    );
  }

  Widget _buildAssessmentResultItem(String skill, int score, Color color) {
    return ListTile(
      title: Text(skill),
      subtitle: LinearProgressIndicator(
        value: score / 100,
        backgroundColor: Colors.grey[300],
        valueColor: AlwaysStoppedAnimation<Color>(color),
      ),
      trailing: Text(
        '$score%',
        style: TextStyle(
          fontWeight: FontWeight.bold,
          color: color,
        ),
      ),
    );
  }

  // Action methods
  void _selectTemplate(String template) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Selected $template template')),
    );
  }

  void _previewResume() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Opening resume preview...')),
    );
  }

  void _downloadResume() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Downloading resume...')),
    );
  }

  void _practiceQuestion(String question) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Practicing: $question')),
    );
  }

  void _startMockInterview() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Starting mock interview...')),
    );
  }

  void _calculateSalary() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Calculating salary range...')),
    );
  }

  void _viewJob(Map<String, dynamic> job) {
    context.push('/job-details/${job['title']}');
  }

  void _editResumeSection(String section) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Editing $section section')),
    );
  }

  void _startInterviewPractice(String type) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Starting $type practice')),
    );
  }

  void _startSkillAssessment(String category) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Starting $category assessment')),
    );
  }
}
