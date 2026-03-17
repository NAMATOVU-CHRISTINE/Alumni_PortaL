import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/providers/user_provider.dart';

class AnnualSubscriptionsScreen extends StatefulWidget {
  const AnnualSubscriptionsScreen({super.key});

  @override
  State<AnnualSubscriptionsScreen> createState() => _AnnualSubscriptionsScreenState();
}

class _AnnualSubscriptionsScreenState extends State<AnnualSubscriptionsScreen> {
  final List<Map<String, dynamic>> _subscriptionPlans = [
    {
      'id': 'basic',
      'name': 'Basic Alumni',
      'price': 'UGX 20,000',
      'duration': 'Annual',
      'features': [
        'Access to alumni directory',
        'Basic networking features',
        'Event notifications',
        'Monthly newsletter',
      ],
      'color': Colors.blue,
      'popular': true,
    },
    {
      'id': 'premium',
      'name': 'Premium Alumni',
      'price': 'UGX 50,000',
      'duration': 'Annual',
      'features': [
        'All Basic features',
        'Priority job postings',
        'Mentorship program access',
        'Exclusive events',
        'Career counseling sessions',
        'Alumni merchandise discounts',
      ],
      'color': AppColors.primary,
      'popular': false,
    },
  ];

  String? _currentSubscription;

  @override
  void initState() {
    super.initState();
    _loadCurrentSubscription();
  }

  void _loadCurrentSubscription() {
    // TODO: Load from user provider or Firebase
    setState(() {
      _currentSubscription = 'basic'; // Example current subscription
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Annual Subscriptions'),
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(
                          Icons.card_membership,
                          color: AppColors.primary,
                          size: 28,
                        ),
                        const SizedBox(width: 12),
                        const Text(
                          'Alumni Membership',
                          style: TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    const Text(
                      'Join our alumni membership program to unlock exclusive benefits, networking opportunities, and lifelong connections with the MUST community.',
                      style: TextStyle(
                        fontSize: 16,
                        color: Colors.grey,
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Current subscription status
            if (_currentSubscription != null) ...[
              Card(
                color: AppColors.primary.withValues(alpha: 0.1),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Row(
                    children: [
                      Icon(
                        Icons.check_circle,
                        color: AppColors.primary,
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Current Subscription',
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            Text(
                              _getSubscriptionName(_currentSubscription!),
                              style: TextStyle(
                                color: AppColors.primary,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          ],
                        ),
                      ),
                      TextButton(
                        onPressed: _showManageSubscription,
                        child: const Text('Manage'),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),
            ],

            // Subscription plans
            const Text(
              'Choose Your Plan',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),

            ...(_subscriptionPlans.map((plan) => _buildSubscriptionCard(plan))),

            const SizedBox(height: 24),

            // Benefits section
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Why Subscribe?',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 12),
                    _buildBenefitItem(Icons.network_check, 'Enhanced Networking', 
                        'Connect with alumni worldwide'),
                    _buildBenefitItem(Icons.work, 'Career Opportunities', 
                        'Access exclusive job postings'),
                    _buildBenefitItem(Icons.school, 'Continued Learning', 
                        'Professional development resources'),
                    _buildBenefitItem(Icons.event, 'Exclusive Events', 
                        'VIP access to alumni gatherings'),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSubscriptionCard(Map<String, dynamic> plan) {
    final isCurrentPlan = _currentSubscription == plan['id'];
    final isPremium = plan['popular'] == true;

    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      child: Stack(
        children: [
          Card(
            elevation: isPremium ? 8 : 2,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
              side: BorderSide(
                color: isCurrentPlan 
                    ? AppColors.primary 
                    : (isPremium ? plan['color'] : Colors.transparent),
                width: isCurrentPlan ? 2 : (isPremium ? 1 : 0),
              ),
            ),
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            plan['name'],
                            style: TextStyle(
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                              color: plan['color'],
                            ),
                          ),
                          Text(
                            plan['duration'],
                            style: const TextStyle(
                              color: Colors.grey,
                            ),
                          ),
                        ],
                      ),
                      Text(
                        plan['price'],
                        style: TextStyle(
                          fontSize: 24,
                          fontWeight: FontWeight.bold,
                          color: plan['color'],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  
                  // Features
                  ...((plan['features'] as List<String>).map((feature) => 
                    Padding(
                      padding: const EdgeInsets.only(bottom: 8),
                      child: Row(
                        children: [
                          Icon(
                            Icons.check,
                            color: plan['color'],
                            size: 20,
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(feature),
                          ),
                        ],
                      ),
                    ),
                  )),
                  
                  const SizedBox(height: 20),
                  
                  // Action button
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: isCurrentPlan ? null : () => _subscribeToPlan(plan),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: isCurrentPlan ? Colors.grey : plan['color'],
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ),
                      child: Text(
                        isCurrentPlan ? 'Current Plan' : 'Subscribe Now',
                        style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          
          // Popular badge
          if (isPremium)
            Positioned(
              top: 0,
              right: 20,
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                decoration: BoxDecoration(
                  color: plan['color'],
                  borderRadius: const BorderRadius.vertical(
                    bottom: Radius.circular(8),
                  ),
                ),
                child: const Text(
                  'POPULAR',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildBenefitItem(IconData icon, String title, String description) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: AppColors.primary.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Icon(
              icon,
              color: AppColors.primary,
              size: 20,
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
                    fontWeight: FontWeight.w600,
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

  String _getSubscriptionName(String id) {
    final plan = _subscriptionPlans.firstWhere((p) => p['id'] == id);
    return plan['name'];
  }

  void _subscribeToPlan(Map<String, dynamic> plan) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Subscribe to ${plan['name']}'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('You are about to subscribe to ${plan['name']} for ${plan['price']}.'),
            const SizedBox(height: 16),
            const Text(
              'Payment methods:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            const Text('• Mobile Money (MTN, Airtel)'),
            const Text('• Bank Transfer'),
            const Text('• Credit/Debit Card'),
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
              _processPayment(plan);
            },
            child: const Text('Proceed to Payment'),
          ),
        ],
      ),
    );
  }

  void _processPayment(Map<String, dynamic> plan) {
    // TODO: Implement payment processing
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Payment processing for ${plan['name']} - Feature coming soon!'),
        duration: const Duration(seconds: 3),
      ),
    );
  }

  void _showManageSubscription() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Manage Subscription'),
        content: const Text(
          'Here you can:\n\n'
          '• View subscription details\n'
          '• Update payment method\n'
          '• Cancel subscription\n'
          '• View billing history\n\n'
          'Feature coming soon!',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }
}