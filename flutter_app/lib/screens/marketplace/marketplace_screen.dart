import 'package:flutter/material.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/services/payment_helper.dart';

class MarketplaceScreen extends StatefulWidget {
  const MarketplaceScreen({super.key});

  @override
  State<MarketplaceScreen> createState() => _MarketplaceScreenState();
}

class _MarketplaceScreenState extends State<MarketplaceScreen> {
  final List<Map<String, dynamic>> _cartItems = [];
  
  final List<Map<String, dynamic>> _marketplaceItems = [
    {
      'id': '1',
      'title': 'MUST Alumni T-Shirt',
      'description': 'Official MUST Alumni branded t-shirt in various sizes and colors',
      'price': 'UGX 35,000',
      'image': 'assets/images/merchandise/MUST Shirts.jpeg',
      'category': 'Apparel',
      'seller': 'MUST Store',
    },
    {
      'id': '2',
      'title': 'MUST Hoodie',
      'description': 'Comfortable hoodie with university logo and alumni branding',
      'price': 'UGX 65,000',
      'image': 'assets/images/merchandise/MUST hoodie.jpeg',
      'category': 'Apparel',
      'seller': 'MUST Store',
    },
    {
      'id': '3',
      'title': 'MUST Collar Shirts',
      'description': 'Professional collar shirts with MUST branding',
      'price': 'UGX 45,000',
      'image': 'assets/images/merchandise/MUST Collar Shirts.jpeg',
      'category': 'Apparel',
      'seller': 'MUST Store',
    },
    {
      'id': '4',
      'title': 'MUST Cap',
      'description': 'Official MUST baseball cap with embroidered logo',
      'price': 'UGX 25,000',
      'image': 'assets/images/merchandise/MUST Cap.jpeg',
      'category': 'Accessories',
      'seller': 'MUST Store',
    },
    {
      'id': '5',
      'title': 'MUST Bottle',
      'description': 'Reusable water bottle with MUST logo',
      'price': 'UGX 20,000',
      'image': 'assets/images/merchandise/MUST Bottle.jpeg',
      'category': 'Accessories',
      'seller': 'MUST Store',
    },
    {
      'id': '6',
      'title': 'MUST Signature Pack',
      'description': 'Complete merchandise package with multiple items',
      'price': 'UGX 150,000',
      'image': 'assets/images/merchandise/MUST Signature Pack.jpeg',
      'category': 'Bundles',
      'seller': 'MUST Store',
    },
  ];

  String _selectedCategory = 'All';
  final List<String> _categories = ['All', 'Apparel', 'Accessories', 'Bundles'];

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
            icon: Stack(
              children: [
                const Icon(Icons.shopping_cart),
                if (_cartItems.isNotEmpty)
                  Positioned(
                    right: 0,
                    top: 0,
                    child: Container(
                      padding: const EdgeInsets.all(2),
                      decoration: BoxDecoration(
                        color: Colors.red,
                        borderRadius: BorderRadius.circular(10),
                      ),
                      constraints: const BoxConstraints(
                        minWidth: 16,
                        minHeight: 16,
                      ),
                      child: Text(
                        '${_cartItems.length}',
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 10,
                          fontWeight: FontWeight.bold,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
              ],
            ),
            onPressed: _showCart,
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
            child: _getFilteredItems().isEmpty
                ? Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          Icons.store,
                          size: 80,
                          color: Colors.grey[300],
                        ),
                        const SizedBox(height: 16),
                        const Text(
                          'No items in this category',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                  )
                : GridView.builder(
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
            // Image
            Expanded(
              flex: 3,
              child: ClipRRect(
                borderRadius: const BorderRadius.vertical(
                  top: Radius.circular(12),
                ),
                child: Image.asset(
                  item['image'],
                  width: double.infinity,
                  fit: BoxFit.cover,
                  errorBuilder: (context, error, stackTrace) => Container(
                    color: Colors.grey[200],
                    child: Icon(
                      Icons.image,
                      size: 48,
                      color: Colors.grey[400],
                    ),
                  ),
                ),
              ),
            ),
            // Item details
            Padding(
              padding: const EdgeInsets.all(6),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    item['title'],
                    style: const TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 12,
                    ),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 2),
                  Text(
                    item['price'],
                    style: TextStyle(
                      color: AppColors.primary,
                      fontWeight: FontWeight.bold,
                      fontSize: 13,
                    ),
                  ),
                  Text(
                    item['seller'],
                    style: TextStyle(
                      color: Colors.grey[600],
                      fontSize: 10,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
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
        contentPadding: EdgeInsets.zero,
        title: Text(
          item['title'],
          style: const TextStyle(fontSize: 16),
        ),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: Image.asset(
                  item['image'],
                  width: double.infinity,
                  height: 180,
                  fit: BoxFit.cover,
                  errorBuilder: (context, error, stackTrace) => Container(
                    height: 180,
                    color: Colors.grey[200],
                    child: Icon(Icons.image, size: 48, color: Colors.grey[400]),
                  ),
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      item['description'],
                      style: const TextStyle(fontSize: 14),
                    ),
                    const SizedBox(height: 12),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          item['price'],
                          style: TextStyle(
                            color: AppColors.primary,
                            fontWeight: FontWeight.bold,
                            fontSize: 18,
                          ),
                        ),
                        Chip(
                          label: Text(
                            item['category'],
                            style: const TextStyle(fontSize: 11),
                          ),
                          backgroundColor: AppColors.primary.withValues(alpha: 0.1),
                          padding: const EdgeInsets.symmetric(horizontal: 8),
                          materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Seller: ${item['seller']}',
                      style: TextStyle(
                        color: Colors.grey[600],
                        fontSize: 13,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
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

  void _addToCart(Map<String, dynamic> item) {
    setState(() {
      _cartItems.add(item);
    });
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('${item['title']} added to cart'),
        action: SnackBarAction(
          label: 'View Cart',
          onPressed: _showCart,
        ),
      ),
    );
  }

  void _showCart() {
    if (_cartItems.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Your cart is empty')),
      );
      return;
    }

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => DraggableScrollableSheet(
        initialChildSize: 0.7,
        minChildSize: 0.5,
        maxChildSize: 0.95,
        expand: false,
        builder: (context, scrollController) => Column(
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: AppColors.primary,
                borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Shopping Cart',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  Text(
                    '${_cartItems.length} items',
                    style: const TextStyle(
                      color: Colors.white70,
                      fontSize: 14,
                    ),
                  ),
                ],
              ),
            ),
            Expanded(
              child: ListView.builder(
                controller: scrollController,
                padding: const EdgeInsets.all(16),
                itemCount: _cartItems.length,
                itemBuilder: (context, index) {
                  final item = _cartItems[index];
                  return Card(
                    margin: const EdgeInsets.only(bottom: 12),
                    child: ListTile(
                      leading: ClipRRect(
                        borderRadius: BorderRadius.circular(8),
                        child: Image.asset(
                          item['image'],
                          width: 60,
                          height: 60,
                          fit: BoxFit.cover,
                          errorBuilder: (context, error, stackTrace) => Container(
                            width: 60,
                            height: 60,
                            color: Colors.grey[200],
                            child: Icon(Icons.image, color: Colors.grey[400]),
                          ),
                        ),
                      ),
                      title: Text(
                        item['title'],
                        style: const TextStyle(fontWeight: FontWeight.bold),
                      ),
                      subtitle: Text(item['price']),
                      trailing: IconButton(
                        icon: const Icon(Icons.delete, color: Colors.red),
                        onPressed: () {
                          setState(() {
                            _cartItems.removeAt(index);
                          });
                          Navigator.pop(context);
                          if (_cartItems.isNotEmpty) {
                            _showCart();
                          }
                        },
                      ),
                    ),
                  );
                },
              ),
            ),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white,
                boxShadow: [
                  BoxShadow(
                    color: Colors.grey.withValues(alpha: 0.3),
                    blurRadius: 10,
                    offset: const Offset(0, -5),
                  ),
                ],
              ),
              child: Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Total:',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text(
                        _calculateTotal(),
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                          color: AppColors.primary,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: () {
                        Navigator.pop(context);
                        _checkout();
                      },
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 16),
                      ),
                      child: const Text(
                        'Checkout',
                        style: TextStyle(fontSize: 16),
                      ),
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

  String _calculateTotal() {
    int total = 0;
    for (var item in _cartItems) {
      final priceStr = item['price'].toString().replaceAll(RegExp(r'[^0-9]'), '');
      total += int.tryParse(priceStr) ?? 0;
    }
    return 'UGX ${total.toString().replaceAllMapped(
      RegExp(r'(\d{1,3})(?=(\d{3})+(?!\d))'),
      (Match m) => '${m[1]},',
    )}';
  }

  void _checkout() {
    final total = _calculateTotal();
    
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Checkout'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Total Items: ${_cartItems.length}',
              style: const TextStyle(fontSize: 16),
            ),
            const SizedBox(height: 8),
            Text(
              'Total: $total',
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: AppColors.primary,
              ),
            ),
            const SizedBox(height: 16),
            const Text(
              'Payment via Mobile Money (MTN, Airtel)',
              style: TextStyle(fontSize: 14),
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
              
              // Extract amount from total string
              final amountString = total.replaceAll(RegExp(r'[^\d]'), '');
              final amount = double.tryParse(amountString) ?? 0;
              
              // Show Africas Talking payment dialog
              PaymentHelper.showMarketplacePayment(
                context,
                amount: amount,
                itemName: '${_cartItems.length} item(s) from Marketplace',
              );
              
              // Clear cart after payment initiated
              setState(() {
                _cartItems.clear();
              });
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primary,
            ),
            child: const Text(
              'Pay Now',
              style: TextStyle(color: Colors.white),
            ),
          ),
        ],
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