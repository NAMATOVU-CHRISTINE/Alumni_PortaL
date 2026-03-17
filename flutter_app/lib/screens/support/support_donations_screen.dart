import 'package:flutter/material.dart';
import 'package:alumni_portal/config/theme.dart';

class SupportDonationsScreen extends StatefulWidget {
  const SupportDonationsScreen({super.key});

  @override
  State<SupportDonationsScreen> createState() => _SupportDonationsScreenState();
}

class _SupportDonationsScreenState extends State<SupportDonationsScreen> {
  final List<Map<String, dynamic>> _donationCauses = [
    {
      'id': '1',
      'title': 'Student Scholarship Fund',
      'description': 'Support deserving students with financial assistance for their education',
      'target': 10000000,
      'raised': 6500000,
      'donors': 245,
      'image': 'assets/images/scholarship.jpg',
      'category': 'Education',
    },
    {
      'id': '2',
      'title': 'Library Expansion Project',
      'description': 'Help expand the university library with modern facilities and resources',
      'target': 25000000,
      'raised': 18200000,
      'donors': 156,
      'image': 'assets/images/library.jpg',
      'category': 'Infrastructure',
    },
    {
      'id': '3',
      'title': 'Research Equipment Fund',
      'description': 'Provide state-of-the-art research equipment for science departments',
      'target': 15000000,
      'raised': 8900000,
      'donors': 89,
      'image': 'assets/images/research.jpg',
      'category': 'Research',
    },
  ];

  final List<int> _quickAmounts = [10000, 25000, 50000, 100000, 250000, 500000];
  int? _selectedAmount;
  final TextEditingController _customAmountController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Support & Donations'),
        elevation: 0,
      ),
      body: SingleChildScrollView(
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
                    Icons.favorite,
                    color: Colors.white,
                    size: 48,
                  ),
                  const SizedBox(height: 16),
                  const Text(
                    'Give Back to MUST',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 28,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'Your contributions help build a brighter future for current and future students',
                    style: TextStyle(
                      color: Colors.white70,
                      fontSize: 16,
                    ),
                  ),
                ],
              ),
            ),

            // Impact statistics
            Container(
              padding: const EdgeInsets.all(20),
              child: Row(
                children: [
                  Expanded(
                    child: _buildStatCard('Total Raised', 'UGX 33.6M', Icons.monetization_on),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: _buildStatCard('Active Donors', '490+', Icons.people),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: _buildStatCard('Projects', '12', Icons.assignment),
                  ),
                ],
              ),
            ),

            // Donation causes
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Current Campaigns',
                    style: TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 16),
                  
                  ...(_donationCauses.map((cause) => _buildDonationCard(cause))),
                ],
              ),
            ),

            // How to donate section
            Container(
              margin: const EdgeInsets.all(20),
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: Colors.grey[50],
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.grey[200]!),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Icon(
                        Icons.info_outline,
                        color: AppColors.primary,
                      ),
                      const SizedBox(width: 8),
                      const Text(
                        'How to Donate',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  _buildHowToStep('1', 'Choose a cause', 'Select from our active campaigns'),
                  _buildHowToStep('2', 'Select amount', 'Choose or enter your donation amount'),
                  _buildHowToStep('3', 'Make payment', 'Use Mobile Money, Bank Transfer, or Card'),
                  _buildHowToStep('4', 'Get receipt', 'Receive confirmation and tax receipt'),
                ],
              ),
            ),

            // Contact section
            Container(
              margin: const EdgeInsets.all(20),
              child: Card(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Need Help?',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 12),
                      const Text(
                        'Contact our donations team for assistance or questions about giving.',
                      ),
                      const SizedBox(height: 16),
                      Row(
                        children: [
                          Icon(Icons.email, color: AppColors.primary),
                          const SizedBox(width: 8),
                          const Text('donations@must.ac.ug'),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          Icon(Icons.phone, color: AppColors.primary),
                          const SizedBox(width: 8),
                          const Text('+256 485 421 373'),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatCard(String title, String value, IconData icon) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Icon(
              icon,
              color: AppColors.primary,
              size: 32,
            ),
            const SizedBox(height: 8),
            Text(
              value,
              style: const TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
              ),
            ),
            Text(
              title,
              style: const TextStyle(
                fontSize: 12,
                color: Colors.grey,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDonationCard(Map<String, dynamic> cause) {
    final progress = (cause['raised'] / cause['target']).clamp(0.0, 1.0);
    final progressPercentage = (progress * 100).round();

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  width: 60,
                  height: 60,
                  decoration: BoxDecoration(
                    color: Colors.grey[200],
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Icon(
                    Icons.image,
                    color: Colors.grey[400],
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        cause['title'],
                        style: const TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        cause['description'],
                        style: const TextStyle(
                          color: Colors.grey,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            
            // Progress bar
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'UGX ${_formatAmount(cause['raised'])}',
                      style: TextStyle(
                        color: AppColors.primary,
                        fontWeight: FontWeight.bold,
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
                const SizedBox(height: 4),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'Goal: UGX ${_formatAmount(cause['target'])}',
                      style: const TextStyle(
                        fontSize: 12,
                        color: Colors.grey,
                      ),
                    ),
                    Text(
                      '${cause['donors']} donors',
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
                onPressed: () => _showDonationDialog(cause),
                child: const Text('Donate Now'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildHowToStep(String number, String title, String description) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        children: [
          Container(
            width: 32,
            height: 32,
            decoration: BoxDecoration(
              color: AppColors.primary,
              shape: BoxShape.circle,
            ),
            child: Center(
              child: Text(
                number,
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Text(
                  description,
                  style: const TextStyle(
                    color: Colors.grey,
                    fontSize: 12,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  String _formatAmount(int amount) {
    if (amount >= 1000000) {
      return '${(amount / 1000000).toStringAsFixed(1)}M';
    } else if (amount >= 1000) {
      return '${(amount / 1000).toStringAsFixed(0)}K';
    }
    return amount.toString();
  }

  void _showDonationDialog(Map<String, dynamic> cause) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Donate to ${cause['title']}'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Select donation amount:'),
            const SizedBox(height: 16),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: _quickAmounts.map((amount) => 
                ChoiceChip(
                  label: Text('UGX ${_formatAmount(amount)}'),
                  selected: _selectedAmount == amount,
                  onSelected: (selected) {
                    setState(() {
                      _selectedAmount = selected ? amount : null;
                      if (selected) {
                        _customAmountController.clear();
                      }
                    });
                  },
                ),
              ).toList(),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _customAmountController,
              decoration: const InputDecoration(
                labelText: 'Custom Amount (UGX)',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.number,
              onChanged: (value) {
                if (value.isNotEmpty) {
                  setState(() {
                    _selectedAmount = null;
                  });
                }
              },
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
              _processDonation(cause);
            },
            child: const Text('Proceed to Payment'),
          ),
        ],
      ),
    );
  }

  void _processDonation(Map<String, dynamic> cause) {
    final amount = _selectedAmount ?? 
        (int.tryParse(_customAmountController.text) ?? 0);
    
    if (amount > 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Processing donation of UGX ${_formatAmount(amount)} to ${cause['title']} - Feature coming soon!',
          ),
          duration: const Duration(seconds: 3),
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Please select or enter a donation amount'),
        ),
      );
    }
  }

  @override
  void dispose() {
    _customAmountController.dispose();
    super.dispose();
  }
}