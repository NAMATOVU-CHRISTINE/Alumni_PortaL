import 'package:flutter/material.dart';
import 'package:alumni_portal/config/theme.dart';

class MarketplaceScreen extends StatefulWidget {
  const MarketplaceScreen({super.key});

  @override
  State<MarketplaceScreen> createState() => _MarketplaceScreenState();
}

class _MarketplaceScreenState extends State<MarketplaceScreen> {
  final List<Map<String, dynamic>> _marketplaceItems = [
    {
      'id': '1',
      'title': 'MUST Alumni T-Shirt',
      'description': 'Official MUST Alumni branded t-shirt in various sizes',
      'price': 'UGX 35,000',
      'image': 'assets/images/tshirt.jpg',
      'category': 'Apparel',
      'seller': 'MUST Store',
    },
    {
      'id': '2',
      'title': 'University Mug',
      'description': 'Ceramic mug with MUST logo and alumni branding',
      'price': 'UGX 15,000',
      'image': 'assets/images/mug.jpg',
      'category': 'Accessories',
      'seller': 'MUST Store',
    },
    {
      'id': '3',
      'title': 'Alumni Directory Book',
      'description': 'Comprehensive directory of MUST alumni contacts',
      'price': 'UGX 50,000',
      'image': 'assets/images/directory.jpg',
      'category': 'Books',
      'seller': 'MUST Publications',
    },
  ];

  String _selectedCategory = 'All';
  final List<String> _categories = ['All', 'Apparel', 'Accessories', 'Books', 'Services'];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Marketplace'),
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () {
              // TODO: Implement search
            },
          ),
          IconButton(
            icon: const Icon(Icons.shopping_cart),
            onPressed: () {
              // TODO: Implement cart
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // Category filter
          Container(
            height: 50,
            padding: const EdgeInsets.symmetric(vertical: 8),
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              itemCount: _categories.length,
              itemBuilder: (context, index) {
                final category = _categories[index];
                final isSelected = category == _selectedCategory;
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: FilterChip(
                    label: Text(category),
                    selected: isSelected,
                    onSelected: (selected) {
                      setState(() {
                        _selectedCategory = category;
                      });
                    },
                    selectedColor: AppColors.primary.withValues(alpha: 0.2),
                    checkmarkColor: AppColors.primary,
                  ),
                );
              },
            ),
          ),
          // Items grid
          Expanded(
            child: GridView.builder(
              padding: const EdgeInsets.all(16),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2,
                childAspectRatio: 0.75,
                crossAxisSpacing: 16,
                mainAxisSpacing: 16,
              ),
              itemCount: _getFilteredItems().length,
              itemBuilder: (context, index) {
                final item = _getFilteredItems()[index];
                return _buildMarketplaceItem(item);
              },
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          // TODO: Add new item
          _showAddItemDialog();
        },
        child: const Icon(Icons.add),
      ),
    );
  }

  List<Map<String, dynamic>> _getFilteredItems() {
    if (_selectedCategory == 'All') {
      return _marketplaceItems;
    }
    return _marketplaceItems
        .where((item) => item['category'] == _selectedCategory)
        .toList();
  }

  Widget _buildMarketplaceItem(Map<String, dynamic> item) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: InkWell(
        onTap: () => _showItemDetails(item),
        borderRadius: BorderRadius.circular(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Image placeholder
            Expanded(
              flex: 3,
              child: Container(
                width: double.infinity,
                decoration: BoxDecoration(
                  color: Colors.grey[200],
                  borderRadius: const BorderRadius.vertical(
                    top: Radius.circular(12),
                  ),
                ),
                child: Icon(
                  Icons.image,
                  size: 48,
                  color: Colors.grey[400],
                ),
              ),
            ),
            // Item details
            Expanded(
              flex: 2,
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      item['title'],
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 14,
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      item['price'],
                      style: TextStyle(
                        color: AppColors.primary,
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                      ),
                    ),
                    const Spacer(),
                    Text(
                      item['seller'],
                      style: TextStyle(
                        color: Colors.grey[600],
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
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
              _showContactSeller(item);
            },
            child: const Text('Contact Seller'),
          ),
        ],
      ),
    );
  }

  void _showContactSeller(Map<String, dynamic> item) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Contacting seller for ${item['title']}...'),
        action: SnackBarAction(
          label: 'OK',
          onPressed: () {},
        ),
      ),
    );
  }

  void _showAddItemDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Add New Item'),
        content: const Text(
          'Feature coming soon! You will be able to add items to the marketplace.',
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