import 'package:flutter/material.dart';
import 'package:alumni_portal/config/theme.dart';

class SupportProjectsScreen extends StatefulWidget {
  const SupportProjectsScreen({super.key});

  @override
  State<SupportProjectsScreen> createState() => _SupportProjectsScreenState();
}

class _SupportProjectsScreenState extends State<SupportProjectsScreen> {
  int _selectedTabIndex = 0;

  final List<Map<String, dynamic>> _projects = [
    {
      'id': '1',
      'title': 'Student Scholarship Fund',
      'description': 'Support deserving students with financial assistance for their education at MUST',
      'target': 10000000,
      'raised': 6500000,
      'donors': 245,
      'category': 'Education',
      'urgency': 'Medium',
      'deadline': '2024-12-31',
    },
    {
      'id': '2',
      'title': 'Library Expansion Project',
      'description': 'Help expand the university library with modern facilities and digital resources',
      'target': 25000000,
      'raised': 18200000,
      'donors': 156,
      'category': 'Infrastructure',
      'urgency': 'Low',
      'deadline': '2025-06-30',
    },
    {
      'id': '3',
      'title': 'Research Equipment Fund',
      'description': 'Provide state-of-the-art research equipment for science and technology departments',
      'target': 15000000,
      'raised': 8900000,
      'donors': 89,
      'category': 'Research',
      'urgency': 'High',
      'deadline': '2024-08-15',
    },
  ];

  final List<Map<String, dynamic>> _emergencies = [
    {
      'id': '1',
      'title': 'Student Emergency Medical Fund',
      'description': 'Urgent medical assistance for students facing health emergencies',
      'target': 5000000,
      'raised': 2800000,
      'donors': 178,
      'urgency': 'Critical',
      'timeLeft': '15 days',
      'beneficiaries': '12 students',
    },
    {
      'id': '2',
      'title': 'Flood Damage Repair',
      'description': 'Emergency repairs needed for dormitory buildings damaged by recent floods',
      'target': 8000000,
      'raised': 4200000,
      'donors': 95,
      'urgency': 'High',
      'timeLeft': '30 days',
      'beneficiaries': '200+ students',
    },
    {
      'id': '3',
      'title': 'Food Security Program',
      'description': 'Emergency food assistance for students facing financial hardship',
      'target': 3000000,
      'raised': 1900000,
      'donors': 234,
      'urgency': 'High',
      'timeLeft': '7 days',
      'beneficiaries': '150+ students',
    },
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Support & Donations'),
        elevation: 0,
        bottom: TabBar(
          controller: null,
          onTap: (index) => setState(() => _selectedTabIndex = index),
          tabs: const [
            Tab(text: 'Projects', icon: Icon(Icons.assignment)),
            Tab(text: 'Emergency', icon: Icon(Icons.warning)),
          ],
        ),
      ),
      body: IndexedStack(
        index: _selectedTabIndex,
        children: [
          _buildProjectsTab(),
          _buildEmergencyTab(),
        ],
      ),
    );
  }

  Widget _buildProjectsTab() {
    return SingleChildScrollView(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header section
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(24),
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
                const Icon(
                  Icons.assignment,
                  color: Colors.white,
                  size: 48,
                ),
                const SizedBox(height: 16),
                const Text(
                  'University Projects',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 28,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                const Text(
                  'Support long-term development projects that enhance education and facilities',
                  style: TextStyle(
                    color: Colors.white70,
                    fontSize: 16,
                  ),
                ),
              ],
            ),
          ),

          // Projects list
          Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Active Projects',
                  style: TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 16),
                
                ...(_projects.map((project) => _buildProjectCard(project))),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEmergencyTab() {
    return SingleChildScrollView(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Emergency header
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(24),
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                colors: [Colors.red, Colors.redAccent],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Icon(
                  Icons.warning,
                  color: Colors.white,
                  size: 48,
                ),
                const SizedBox(height: 16),
                const Text(
                  'Emergency Appeals',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 28,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                const Text(
                  'Urgent support needed for critical situations affecting our community',
                  style: TextStyle(
                    color: Colors.white70,
                    fontSize: 16,
                  ),
                ),
              ],
            ),
          ),

          // Emergency list
          Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Urgent Appeals',
                  style: TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 16),
                
                ...(_emergencies.map((emergency) => _buildEmergencyCard(emergency))),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProjectCard(Map<String, dynamic> project) {
    final progress = (project['raised'] / project['target']).clamp(0.0, 1.0);
    final progressPercentage = (progress * 100).round();
    final urgencyColor = _getUrgencyColor(project['urgency']);

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        project['title'],
                        style: const TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        project['description'],
                        style: const TextStyle(
                          color: Colors.grey,
                        ),
                      ),
                    ],
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: urgencyColor,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    project['urgency'],
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 10,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            
            // Progress section
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'UGX ${_formatAmount(project['raised'])}',
                      style: TextStyle(
                        color: AppColors.primary,
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                      ),
                    ),
                    Text(
                      '$progressPercentage%',
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                LinearProgressIndicator(
                  value: progress,
                  backgroundColor: Colors.grey[200],
                  valueColor: AlwaysStoppedAnimation<Color>(AppColors.primary),
                ),
                const SizedBox(height: 8),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'Goal: UGX ${_formatAmount(project['target'])}',
                      style: const TextStyle(
                        fontSize: 12,
                        color: Colors.grey,
                      ),
                    ),
                    Text(
                      '${project['donors']} donors',
                      style: const TextStyle(
                        fontSize: 12,
                        color: Colors.grey,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    Icon(Icons.schedule, size: 14, color: Colors.grey[600]),
                    const SizedBox(width: 4),
                    Text(
                      'Deadline: ${project['deadline']}',
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.grey[600],
                      ),
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 16),
            
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () => _showDonationDialog(project, false),
                child: const Text('Support Project'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEmergencyCard(Map<String, dynamic> emergency) {
    final progress = (emergency['raised'] / emergency['target']).clamp(0.0, 1.0);
    final progressPercentage = (progress * 100).round();

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 3,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: const BorderSide(color: Colors.red, width: 1),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: const BoxDecoration(
                    color: Colors.red,
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(
                    Icons.warning,
                    color: Colors.white,
                    size: 20,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        emergency['title'],
                        style: const TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text(
                        emergency['description'],
                        style: const TextStyle(
                          color: Colors.grey,
                          fontSize: 14,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            
            // Emergency details
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.red.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Time Left: ${emergency['timeLeft']}',
                          style: const TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.red,
                          ),
                        ),
                        Text(
                          'Beneficiaries: ${emergency['beneficiaries']}',
                          style: const TextStyle(fontSize: 12),
                        ),
                      ],
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: Colors.red,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      emergency['urgency'],
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            
            // Progress section
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'UGX ${_formatAmount(emergency['raised'])}',
                      style: const TextStyle(
                        color: Colors.red,
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                      ),
                    ),
                    Text(
                      '$progressPercentage%',
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                LinearProgressIndicator(
                  value: progress,
                  backgroundColor: Colors.grey[200],
                  valueColor: const AlwaysStoppedAnimation<Color>(Colors.red),
                ),
                const SizedBox(height: 8),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'Goal: UGX ${_formatAmount(emergency['target'])}',
                      style: const TextStyle(
                        fontSize: 12,
                        color: Colors.grey,
                      ),
                    ),
                    Text(
                      '${emergency['donors']} donors',
                      style: const TextStyle(
                        fontSize: 12,
                        color: Colors.grey,
                      ),
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 16),
            
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () => _showDonationDialog(emergency, true),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.red,
                ),
                child: const Text(
                  'Donate Now - Urgent',
                  style: TextStyle(color: Colors.white),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Color _getUrgencyColor(String urgency) {
    switch (urgency.toLowerCase()) {
      case 'critical':
        return Colors.red;
      case 'high':
        return Colors.orange;
      case 'medium':
        return Colors.amber;
      case 'low':
        return Colors.green;
      default:
        return Colors.grey;
    }
  }

  String _formatAmount(int amount) {
    if (amount >= 1000000) {
      return '${(amount / 1000000).toStringAsFixed(1)}M';
    } else if (amount >= 1000) {
      return '${(amount / 1000).toStringAsFixed(0)}K';
    }
    return amount.toString();
  }

  void _showDonationDialog(Map<String, dynamic> item, bool isEmergency) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(isEmergency ? 'Emergency Donation' : 'Support Project'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('You are about to donate to:'),
            const SizedBox(height: 8),
            Text(
              item['title'],
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            const Text('Quick amounts:'),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              children: [10000, 25000, 50000, 100000].map((amount) => 
                ChoiceChip(
                  label: Text('${_formatAmount(amount)}'),
                  selected: false,
                  onSelected: (selected) {
                    // Handle selection
                  },
                ),
              ).toList(),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text(
                    'Donation processing for ${item['title']} - Feature coming soon!',
                  ),
                ),
              );
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: isEmergency ? Colors.red : AppColors.primary,
            ),
            child: Text(
              'Proceed to Payment',
              style: TextStyle(color: Colors.white),
            ),
          ),
        ],
      ),
    );
  }
}