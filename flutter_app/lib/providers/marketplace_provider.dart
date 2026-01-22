import 'package:flutter/foundation.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/models/product_model.dart';

class MarketplaceProvider with ChangeNotifier {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;

  List<ProductModel> _products = [];
  List<ProductModel> _featuredProducts = [];
  List<OrderModel> _orders = [];
  final List<String> _cartItems = [];
  bool _isLoading = false;
  String? _error;

  List<ProductModel> get products => _products;
  List<ProductModel> get featuredProducts => _featuredProducts;
  List<OrderModel> get orders => _orders;
  List<String> get cartItems => _cartItems;
  bool get isLoading => _isLoading;
  String? get error => _error;

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }

  void _setError(String? value) {
    _error = value;
    notifyListeners();
  }

  Future<void> loadProducts({String? category, String? searchQuery}) async {
    try {
      _setLoading(true);
      Query query = _firestore
          .collection('products')
          .where('isAvailable', isEqualTo: true);

      if (category != null && category.isNotEmpty) {
        query = query.where('category', isEqualTo: category);
      }

      final snapshot = await query.orderBy('createdAt', descending: true).get();

      _products =
          snapshot.docs.map((doc) => ProductModel.fromFirestore(doc)).toList();

      // Apply search filter if provided
      if (searchQuery != null && searchQuery.isNotEmpty) {
        final query = searchQuery.toLowerCase();
        _products = _products.where((product) {
          return product.name.toLowerCase().contains(query) ||
              product.description.toLowerCase().contains(query) ||
              product.tags.any((tag) => tag.toLowerCase().contains(query));
        }).toList();
      }

      _setError(null);
    } catch (e) {
      _setError('Failed to load products: $e');
    } finally {
      _setLoading(false);
    }
  }

  Future<void> loadFeaturedProducts() async {
    try {
      final snapshot = await _firestore
          .collection('products')
          .where('isAvailable', isEqualTo: true)
          .where('rating', isGreaterThanOrEqualTo: 4.0)
          .orderBy('rating', descending: true)
          .limit(10)
          .get();

      _featuredProducts =
          snapshot.docs.map((doc) => ProductModel.fromFirestore(doc)).toList();
      notifyListeners();
    } catch (e) {
      _setError('Failed to load featured products: $e');
    }
  }

  Future<void> addProduct(ProductModel product) async {
    try {
      _setLoading(true);
      await _firestore.collection('products').add(product.toMap());
      await loadProducts();
      _setError(null);
    } catch (e) {
      _setError('Failed to add product: $e');
    } finally {
      _setLoading(false);
    }
  }

  Future<void> updateProduct(String productId, ProductModel product) async {
    try {
      _setLoading(true);
      await _firestore
          .collection('products')
          .doc(productId)
          .update(product.toMap());
      await loadProducts();
      _setError(null);
    } catch (e) {
      _setError('Failed to update product: $e');
    } finally {
      _setLoading(false);
    }
  }

  Future<void> deleteProduct(String productId) async {
    try {
      _setLoading(true);
      await _firestore.collection('products').doc(productId).delete();
      await loadProducts();
      _setError(null);
    } catch (e) {
      _setError('Failed to delete product: $e');
    } finally {
      _setLoading(false);
    }
  }

  void addToCart(String productId) {
    if (!_cartItems.contains(productId)) {
      _cartItems.add(productId);
      notifyListeners();
    }
  }

  void removeFromCart(String productId) {
    _cartItems.remove(productId);
    notifyListeners();
  }

  void clearCart() {
    _cartItems.clear();
    notifyListeners();
  }

  Future<void> placeOrder(OrderModel order) async {
    try {
      _setLoading(true);
      await _firestore.collection('orders').add(order.toMap());
      clearCart();
      await loadOrders();
      _setError(null);
    } catch (e) {
      _setError('Failed to place order: $e');
    } finally {
      _setLoading(false);
    }
  }

  Future<void> loadOrders() async {
    try {
      final userId = _auth.currentUser?.uid;
      if (userId == null) return;

      final snapshot = await _firestore
          .collection('orders')
          .where('buyerId', isEqualTo: userId)
          .orderBy('createdAt', descending: true)
          .get();

      _orders =
          snapshot.docs.map((doc) => OrderModel.fromFirestore(doc)).toList();
      notifyListeners();
    } catch (e) {
      _setError('Failed to load orders: $e');
    }
  }

  Future<void> updateOrderStatus(String orderId, String status) async {
    try {
      await _firestore.collection('orders').doc(orderId).update({
        'status': status,
        'updatedAt': DateTime.now().millisecondsSinceEpoch,
      });
      await loadOrders();
    } catch (e) {
      _setError('Failed to update order status: $e');
    }
  }

  List<String> getCategories() {
    return [
      'MUST Merchandise',
      'Textbooks',
      'Electronics',
      'Clothing',
      'Accessories',
      'Stationery',
      'Sports',
      'Other',
    ];
  }

  void clearError() => _setError(null);
}
