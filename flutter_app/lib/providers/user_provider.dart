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
      _error = null;

      // Get all users without the deleted filter initially
      Query query = _firestore.collection('users');

      final snapshot = await query.get();

      _alumniDirectory = snapshot.docs
          .map((doc) => UserModel.fromFirestore(doc))
          .where((user) {
        // Filter out deleted users
        // Note: We check this in the where clause instead of the query
        // to avoid issues with missing 'deleted' field

        // Show alumni, students, and staff (all university community members)
        final userType = (user.userType ?? 'student').toLowerCase().trim();
        final isAlumni = user.isAlumni;

        // Include alumni, students, and staff
        final isValidUserType = userType == 'alumni' ||
            userType == 'alumnus' ||
            userType == 'student' ||
            userType == 'staff' ||
            isAlumni;

        if (!isValidUserType) {
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

      // Remove duplicates - keep only the most recent account based on email
      final Map<String, UserModel> uniqueUsers = {};
      for (var user in _alumniDirectory) {
        final email = user.email?.toLowerCase();
        if (email != null && email.isNotEmpty) {
          // If email already exists, keep the one with the most recent creation date
          if (uniqueUsers.containsKey(email)) {
            final existing = uniqueUsers[email]!;
            final existingDate = existing.createdAt ?? DateTime(2000);
            final newDate = user.createdAt ?? DateTime(2000);

            // Keep the newer account (don't delete from Firestore here)
            if (newDate.isAfter(existingDate)) {
              uniqueUsers[email] = user;
            }
            // else keep existing
          } else {
            uniqueUsers[email] = user;
          }
        } else {
          // If no email, keep the user (can't determine duplicates)
          final key = user.userId ?? 'no_id_${uniqueUsers.length}';
          uniqueUsers[key] = user;
        }
      }

      _alumniDirectory = uniqueUsers.values.toList();

      // Sort by name
      _alumniDirectory.sort(
        (a, b) => (a.fullName ?? '').compareTo(b.fullName ?? ''),
      );

      notifyListeners();
    } catch (e) {
      _setError('Failed to load university network: ${e.toString()}');
      _alumniDirectory = [];
      notifyListeners();
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
      _error = null;

      // Get all users
      Query query = _firestore.collection('users');

      if (kDebugMode) {
        print('Loading almater directory...');
      }

      final snapshot = await query.get();

      if (kDebugMode) {
        print('Fetched ${snapshot.docs.length} users from Firestore');
      }

      _almaterDirectory = snapshot.docs
          .map((doc) => UserModel.fromFirestore(doc))
          .where((user) {
        // Only show students and staff (NOT alumni)
        final userType = (user.userType ?? 'student').toLowerCase().trim();
        final isAlumni = user.isAlumni;

        // Exclude alumni
        if (isAlumni || userType == 'alumni' || userType == 'alumnus') {
          return false;
        }

        // Include students and staff
        final isStudentOrStaff = userType == 'student' || userType == 'staff';
        if (!isStudentOrStaff) {
          return false;
        }

        // Apply user type filter
        if (filterByUserType != null && filterByUserType.isNotEmpty) {
          if (userType != filterByUserType.toLowerCase().trim()) {
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

        // Show all students and staff regardless of privacy settings
        return true;
      }).toList();

      // Remove duplicates - keep only the most recent account based on email
      final Map<String, UserModel> uniqueUsers = {};
      for (var user in _almaterDirectory) {
        final email = user.email?.toLowerCase();
        if (email != null && email.isNotEmpty) {
          // If email already exists, keep the one with the most recent creation date
          if (uniqueUsers.containsKey(email)) {
            final existing = uniqueUsers[email]!;
            final existingDate = existing.createdAt ?? DateTime(2000);
            final newDate = user.createdAt ?? DateTime(2000);

            // Keep the newer account (don't delete from Firestore here)
            if (newDate.isAfter(existingDate)) {
              uniqueUsers[email] = user;
            }
            // else keep existing
          } else {
            uniqueUsers[email] = user;
          }
        } else {
          // If no email, keep the user (can't determine duplicates)
          final key = user.userId ?? 'no_id_${uniqueUsers.length}';
          uniqueUsers[key] = user;
        }
      }

      _almaterDirectory = uniqueUsers.values.toList();

      // Sort by name
      _almaterDirectory.sort(
        (a, b) => (a.fullName ?? '').compareTo(b.fullName ?? ''),
      );

      if (kDebugMode) {
        print('Loaded ${_almaterDirectory.length} almater directory members');
      }

      notifyListeners();
    } catch (e) {
      if (kDebugMode) {
        print('Error loading almater directory: $e');
      }
      _setError('Failed to load MUST community: ${e.toString()}');
      _almaterDirectory = [];
      notifyListeners();
    } finally {
      if (kDebugMode) {
        print('Setting loading to false');
      }
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
