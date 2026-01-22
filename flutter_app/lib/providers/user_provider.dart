import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:alumni_portal/models/user_model.dart';

class UserProvider with ChangeNotifier {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;
  final FirebaseStorage _storage = FirebaseStorage.instance;

  UserModel? _currentUser;
  List<UserModel> _alumniDirectory = [];
  List<UserModel> _almaterDirectory = [];
  bool _isLoading = false;
  String? _error;

  UserModel? get currentUser => _currentUser;
  List<UserModel> get alumniDirectory => _alumniDirectory;
  List<UserModel> get almaterDirectory => _almaterDirectory;
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

  Future<void> loadCurrentUser() async {
    try {
      _setLoading(true);
      final userId = _auth.currentUser?.uid;
      if (userId == null) return;

      final doc = await _firestore.collection('users').doc(userId).get();
      if (doc.exists) {
        _currentUser = UserModel.fromFirestore(doc);

        // Update last active and online status
        await _firestore.collection('users').doc(userId).update({
          'lastActive': DateTime.now().millisecondsSinceEpoch,
          'isOnline': true,
        });
      }
    } catch (e) {
      _setError('Failed to load user profile');
    } finally {
      _setLoading(false);
    }
  }

  // Method to update user activity status
  Future<void> updateUserActivity() async {
    try {
      final userId = _auth.currentUser?.uid;
      if (userId == null) return;

      await _firestore.collection('users').doc(userId).update({
        'lastActive': DateTime.now().millisecondsSinceEpoch,
        'isOnline': true,
      });
    } catch (e) {
      // Silently handle errors for activity updates
    }
  }

  // Method to set user offline
  Future<void> setUserOffline() async {
    try {
      final userId = _auth.currentUser?.uid;
      if (userId == null) return;

      await _firestore.collection('users').doc(userId).update({
        'isOnline': false,
        'lastActive': DateTime.now().millisecondsSinceEpoch,
      });
    } catch (e) {
      // Silently handle errors
    }
  }

  Future<UserModel?> getUserById(String userId) async {
    try {
      final doc = await _firestore.collection('users').doc(userId).get();
      if (doc.exists) {
        return UserModel.fromFirestore(doc);
      }
      return null;
    } catch (e) {
      return null;
    }
  }

  Future<void> loadAlumniDirectory({
    String? searchQuery,
    String? filterByMajor,
    String? filterByYear,
    String? filterByLocation,
  }) async {
    try {
      _setLoading(true);

      Query query =
          _firestore.collection('users').where('deleted', isNotEqualTo: true);

      final snapshot = await query.get();

      _alumniDirectory = snapshot.docs
          .map((doc) => UserModel.fromFirestore(doc))
          .where((user) {
        // Show alumni, students, and staff (all university community members)
        final userType = user.userType.toLowerCase();
        if (userType != 'alumni' &&
            userType != 'student' &&
            userType != 'staff') {
          return false;
        }

        // Apply search filter
        if (searchQuery != null && searchQuery.isNotEmpty) {
          final query = searchQuery.toLowerCase();
          final matchesName =
              user.fullName?.toLowerCase().contains(query) ?? false;
          final matchesUsername =
              user.username?.toLowerCase().contains(query) ?? false;
          final matchesMajor =
              user.major?.toLowerCase().contains(query) ?? false;
          final matchesCompany =
              user.company?.toLowerCase().contains(query) ?? false;
          if (!matchesName &&
              !matchesUsername &&
              !matchesMajor &&
              !matchesCompany) {
            return false;
          }
        }

        // Apply major filter
        if (filterByMajor != null && filterByMajor.isNotEmpty) {
          if (user.major?.toLowerCase() != filterByMajor.toLowerCase()) {
            return false;
          }
        }

        // Apply year filter
        if (filterByYear != null && filterByYear.isNotEmpty) {
          if (user.graduationYear != filterByYear) {
            return false;
          }
        }

        // Apply location filter
        if (filterByLocation != null && filterByLocation.isNotEmpty) {
          if (user.location?.toLowerCase() != filterByLocation.toLowerCase()) {
            return false;
          }
        }

        return true;
      }).toList();

      // Sort by name
      _alumniDirectory.sort(
        (a, b) => (a.fullName ?? '').compareTo(b.fullName ?? ''),
      );
    } catch (e) {
      _setError('Failed to load university network');
    } finally {
      _setLoading(false);
    }
  }

  Future<void> loadAlmaterDirectory({
    String? searchQuery,
    String? filterByMajor,
    String? filterByUserType,
  }) async {
    try {
      _setLoading(true);

      Query query =
          _firestore.collection('users').where('deleted', isNotEqualTo: true);

      final snapshot = await query.get();

      _almaterDirectory = snapshot.docs
          .map((doc) => UserModel.fromFirestore(doc))
          .where((user) {
        // Only show students and staff (NOT alumni)
        final userType = user.userType.toLowerCase();
        if (userType != 'student' && userType != 'staff') {
          return false;
        }

        // Apply user type filter
        if (filterByUserType != null && filterByUserType.isNotEmpty) {
          if (userType != filterByUserType.toLowerCase()) {
            return false;
          }
        }

        // Apply search filter
        if (searchQuery != null && searchQuery.isNotEmpty) {
          final query = searchQuery.toLowerCase();
          final matchesName =
              user.fullName?.toLowerCase().contains(query) ?? false;
          final matchesUsername =
              user.username?.toLowerCase().contains(query) ?? false;
          final matchesMajor =
              user.major?.toLowerCase().contains(query) ?? false;
          final matchesCompany =
              user.company?.toLowerCase().contains(query) ?? false;
          final matchesBio = user.bio?.toLowerCase().contains(query) ?? false;
          final matchesLocation =
              user.location?.toLowerCase().contains(query) ?? false;
          if (!matchesName &&
              !matchesUsername &&
              !matchesMajor &&
              !matchesCompany &&
              !matchesBio &&
              !matchesLocation) {
            return false;
          }
        }

        // Apply major filter
        if (filterByMajor != null && filterByMajor.isNotEmpty) {
          if (!(user.major
                  ?.toLowerCase()
                  .contains(filterByMajor.toLowerCase()) ??
              false)) {
            return false;
          }
        }

        // Check privacy settings
        final showInDirectory = user.privacySettings['showInDirectory'] ?? true;
        return showInDirectory;
      }).toList();

      // Sort by name
      _almaterDirectory.sort(
        (a, b) => (a.fullName ?? '').compareTo(b.fullName ?? ''),
      );
    } catch (e) {
      _setError('Failed to load almater directory');
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> updateProfile(Map<String, dynamic> updates) async {
    try {
      _setLoading(true);
      final userId = _auth.currentUser?.uid;
      if (userId == null) return false;

      updates['updatedAt'] = DateTime.now().millisecondsSinceEpoch;

      await _firestore.collection('users').doc(userId).update(updates);
      await loadCurrentUser();

      return true;
    } catch (e) {
      _setError('Failed to update profile');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<String?> uploadProfileImage(File imageFile) async {
    try {
      _setLoading(true);
      final userId = _auth.currentUser?.uid;
      if (userId == null) return null;

      final ref = _storage.ref().child('profile_images/$userId.jpg');
      await ref.putFile(imageFile);
      final url = await ref.getDownloadURL();

      await _firestore.collection('users').doc(userId).update({
        'profileImageUrl': url,
        'updatedAt': DateTime.now().millisecondsSinceEpoch,
      });

      await loadCurrentUser();
      return url;
    } catch (e) {
      _setError('Failed to upload image');
      return null;
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> updatePrivacySettings(Map<String, bool> settings) async {
    try {
      final userId = _auth.currentUser?.uid;
      if (userId == null) return false;

      await _firestore.collection('users').doc(userId).update({
        'privacySettings': settings,
        'updatedAt': DateTime.now().millisecondsSinceEpoch,
      });

      await loadCurrentUser();
      return true;
    } catch (e) {
      _setError('Failed to update privacy settings');
      return false;
    }
  }

  Future<List<UserModel>> searchMentors({
    String? industry,
    String? skill,
    String? location,
  }) async {
    try {
      Query query =
          _firestore.collection('users').where('isAlumni', isEqualTo: true);

      final snapshot = await query.get();

      return snapshot.docs.map((doc) => UserModel.fromFirestore(doc)).where((
        user,
      ) {
        // Filter by industry
        if (industry != null && industry.isNotEmpty) {
          if (user.industry?.toLowerCase() != industry.toLowerCase()) {
            return false;
          }
        }

        // Filter by skill
        if (skill != null && skill.isNotEmpty) {
          final hasSkill = user.skills.any(
            (s) => s.toLowerCase().contains(skill.toLowerCase()),
          );
          if (!hasSkill) return false;
        }

        // Filter by location
        if (location != null && location.isNotEmpty) {
          if (user.location?.toLowerCase() != location.toLowerCase()) {
            return false;
          }
        }

        // Check if user allows mentor requests
        final allowMentorRequests =
            user.privacySettings['allowMentorRequests'] ?? true;
        return allowMentorRequests;
      }).toList();
    } catch (e) {
      return [];
    }
  }

  void clearError() => _setError(null);
}
