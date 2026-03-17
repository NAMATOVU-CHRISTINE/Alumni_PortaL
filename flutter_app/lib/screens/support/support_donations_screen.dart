import 'package:flutter/material.dart';
import 'package:alumni_portal/config/theme.dart';

class SupportDonationsScreen extends StatefulWidget {
  const SupportDonationsScreen({super.key});

  @override
  State<SupportDonationsScreen> createState() => _SupportDonationsScreenState();
}

class _SupportDonationsScreenState extends State<SupportDonationsScreen> {
  final List<Map<String, dynamic>> _marketplaceItems = [
    {
      'id': '1',
      'title': 'MUST Alumni T-Shirt',
      'description': 'Official MUST Alumni branded t-shirt in various sizes and colors',
      'price': 'UGX 35,000',
      'category': 'Apparel',
      'seller': 'MUST Store',
      'rating': 4.5,
      'reviews': 23,
    },
    {
      'id': '2',
      'title': 'University Mug',
      'description': 'Ceramic mug with MUST logo and alumni branding',
      'price': 'UGX 15,000',
      'category': 'Accessories',
      'seller': 'MUST Store',
      'rating': 4.8,
      'reviews': 45,
    },
    {
      'id': '3',
      'title': 'Alumni Directory Book',
      'description': 'Comprehensive directory of MUST alumni contacts and achievements',
      'price': 'UGX 50,000',
      'category': 'Books',
      'seller': 'MUST Publications',
      'rating': 4.2,
      'reviews': 12,
    },
    {
      'id': '4',
      'title': 'MUST Hoodie',
      'description': 'Comfortable hoodie with university logo and alumni branding',
      'price': 'UGX 65,000',
      'category': 'Apparel',
      'seller': 'MUST Store',
      'rating': 4.7,
      'reviews': 31,
    },
    {
      'id': '5',
      'title': 'University Cap',
      'description': 'Official MUST baseball cap with embroidered logo',
      'price': 'UGX 25,000',
      'category': 'Accessories',
      'seller': 'MUST Store',
      'rating': 4.4,
      'reviews': 18,
    },
    {
      'id': '6',
      'title': 'Alumni Notebook Set',
      'description': 'Set of 3 premium notebooks with MUST alumni branding',
      'price': 'UGX 30,000',
      'category': 'Stationery',
      'seller': 'MUST Publications',
      'rating': 4.6,
      'reviews': 27,
    },
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Marketplace'),
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
                    Icons.store,
                    color: Colors.white,
                    size: 48,
                  ),
                  const SizedBox(height: 16),
                  const Text(
                    'MUST Marketplace',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 28,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'Shop official MUST merchandise and alumni products',
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
                    child: _buildStatCard('Products', '25+', Icons.inventory),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: _buildStatCard('Happy Customers', '150+', Icons.people),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: _buildStatCard('Categories', '4', Icons.category),
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
                    'Featured Products',
                    style: TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 16),
                  
                  ...(_marketplaceItems.map((item) => _buildMarketplaceCard(item))),
                ],
              ),
            ),

            // How to shop section
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
                        Icons.shopping_bag,
                        color: AppColors.primary,
                      ),
                      const SizedBox(width: 8),
                      const Text(
                        'How to Shop',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  _buildHowToStep('1', 'Browse products', 'Explore our merchandise collection'),
                  _buildHowToStep('2', 'Add to cart', 'Select items and quantities'),
                  _buildHowToStep('3', 'Checkout', 'Use Mobile Money, Bank Transfer, or Card'),
                  _buildHowToStep('4', 'Delivery', 'Get your items delivered or pick up'),
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
                        'Contact our store team for product inquiries or order assistance.',
                      ),
                      const SizedBox(height: 16),
                      Row(
                        children: [
                          Icon(Icons.email, color: AppColors.primary),
                          const SizedBox(width: 8),
                          const Text('store@must.ac.ug'),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          Icon(Icons.phone, color: AppColors.primary),
                          const SizedBox(width: 8),
                          const Text('+256 485 421 374'),
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

  Widget _buildMarketplaceCard(Map<String, dynamic> item) {
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
                  width: 80,
                  height: 80,
                  decoration: BoxDecoration(
                    color: Colors.grey[200],
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Icon(
                    Icons.image,
                    color: Colors.grey[400],
                    size: 32,
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        item['title'],
                        style: const TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        item['description'],
                        style: const TextStyle(
                          color: Colors.grey,
                        ),
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          Text(
                            item['price'],
                            style: TextStyle(
                              color: AppColors.primary,
                              fontWeight: FontWeight.bold,
                              fontSize: 18,
                            ),
                          ),
                          const SizedBox(width: 16),
                          Row(
                            children: [
                              Icon(Icons.star, color: Colors.amber, size: 16),
                              const SizedBox(width: 4),
                              Text(
                                '${item['rating']} (${item['reviews']})',
                                style: const TextStyle(
                                  fontSize: 12,
                                  color: Colors.grey,
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            
            Row(
              children: [
                Chip(
                  label: Text(item['category']),
                  backgroundColor: AppColors.primary.withValues(alpha: 0.1),
                ),
                const SizedBox(width: 8),
                Text(
                  'Sold by: ${item['seller']}',
                  style: const TextStyle(
                    fontSize: 12,
                    color: Colors.grey,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _showItemDetails(item),
                    icon: const Icon(Icons.info_outline),
                    label: const Text('View Details'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () => _addToCart(item),
                    icon: const Icon(Icons.shopping_cart),
                    label: const Text('Add to Cart'),
                  ),
                ),
              ],
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

  void _showItemDetails(Map<String, dynamic> item) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(item['title']),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              width: double.infinity,
              height: 120,
              decoration: BoxDecoration(
                color: Colors.grey[200],
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(
                Icons.image,
                color: Colors.grey[400],
                size: 48,
              ),
            ),
            const SizedBox(height: 16),
            Text(
              item['description'],
              style: const TextStyle(fontSize: 16),
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Price: ${item['price']}',
                  style: TextStyle(
                    color: AppColors.primary,
                    fontWeight: FontWeight.bold,
                    fontSize: 18,
                  ),
                ),
                Chip(
                  label: Text(item['category']),
                  backgroundColor: AppColors.primary.withValues(alpha: 0.1),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Icon(Icons.star, color: Colors.amber, size: 16),
                const SizedBox(width: 4),
                Text('${item['rating']} (${item['reviews']} reviews)'),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              'Seller: ${item['seller']}',
              style: TextStyle(
                color: Colors.grey[600],
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Close'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              _addToCart(item);
            },
            child: const Text('Add to Cart'),
          ),
        ],
      ),
    );
  }

  void _addToCart(Map<String, dynamic> item) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('${item['title']} added to cart!'),
        action: SnackBarAction(
          label: 'View Cart',
          onPressed: () {
            // TODO: Navigate to cart
          },
        ),
      ),
    );
  }
}