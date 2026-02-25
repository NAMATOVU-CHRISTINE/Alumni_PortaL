import 'package:flutter/foundation.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:alumni_portal/services/error_handler_service.dart';

class AuthProvider with ChangeNotifier {
  final FirebaseAuth _auth = FirebaseAuth.instance;
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final GoogleSignIn _googleSignIn = GoogleSignIn(
    scopes: ['email'],
    serverClientId: '511866402860-rtp3f0a1k9j6bmigtn8vbsv36kh2d9h2.apps.googleusercontent.com',
  );

  User? _user;
  bool _isLoading = false;
  String? _error;

  User? get user => _user;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get isAuthenticated => _user != null;
  String? get userId => _user?.uid;

  AuthProvider() {
    // Initialize with current user if already logged in
    _user = _auth.currentUser;

    // Listen for auth state changes
    _auth.authStateChanges().listen((User? user) {
      _user = user;
      notifyListeners();
    });
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }

  void _setError(String? value) {
    _error = value;
    notifyListeners();
  }

  Future<bool> signInWithEmail(String username, String password) async {
    try {
      _setLoading(true);
      _setError(null);

      // Look up email by username
      final querySnapshot = await _firestore
          .collection('users')
          .where('username', isEqualTo: username)
          .limit(1)
          .get();

      if (querySnapshot.docs.isEmpty) {
        _setError('Username not found');
        return false;
      }

      final email = querySnapshot.docs.first.data()['email'] as String?;
      if (email == null) {
        _setError('Could not find email for this user');
        return false;
      }

      // Check if account is deleted
      final isDeleted =
          querySnapshot.docs.first.data()['deleted'] as bool? ?? false;
      if (isDeleted) {
        _setError('This account has been deleted');
        return false;
      }

      // Sign in with email
      final credential = await _auth.signInWithEmailAndPassword(
        email: email,
        password: password,
      );

      // Check email verification
      await credential.user?.reload();
      if (credential.user?.emailVerified != true) {
        _setError('Please verify your email first');
        await _auth.signOut();
        return false;
      }

      // Update email verification status in Firestore
      await _firestore.collection('users').doc(credential.user!.uid).update({
        'emailVerified': true,
      });

      // Update FCM token
      await _updateFcmToken();

      return true;
    } on FirebaseAuthException catch (e) {
      _setError(ErrorHandlerService.getAuthErrorMessage(e));
      return false;
    } catch (e) {
      _setError('An unexpected error occurred. Please try again.');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> signUpWithEmail({
    required String fullName,
    required String username,
    required String studentId,
    required String email,
    required String password,
    required String userType,
  }) async {
    try {
      _setLoading(true);
      _setError(null);

      // Check if username exists
      final usernameQuery = await _firestore
          .collection('users')
          .where('username', isEqualTo: username)
          .get();

      if (usernameQuery.docs.isNotEmpty) {
        _setError('Username already exists');
        return false;
      }

      // Create auth account
      final credential = await _auth.createUserWithEmailAndPassword(
        email: email,
        password: password,
      );

      final userId = credential.user!.uid;

      // Get FCM token
      String? fcmToken;
      try {
        fcmToken = await FirebaseMessaging.instance.getToken();
      } catch (_) {}

      // Create user document
      final userData = {
        'fullName': fullName,
        'username': username,
        'email': email,
        'userId': userId,
        'userType': userType.toLowerCase(),
        'isAlumni': userType.toLowerCase() == 'alumni',
        'emailVerified': false,
        'createdAt': DateTime.now().millisecondsSinceEpoch,
        'fcmToken': fcmToken,
      };

      // Add appropriate ID field
      if (userType.toLowerCase() == 'alumni') {
        userData['alumniID'] = studentId;
      } else if (userType.toLowerCase() == 'staff') {
        userData['staffID'] = studentId;
      } else {
        userData['studentId'] = studentId;
      }

      await _firestore.collection('users').doc(userId).set(userData);

      // Send verification email
      await credential.user?.sendEmailVerification();

      // Sign out until email is verified
      await _auth.signOut();

      return true;
    } on FirebaseAuthException catch (e) {
      _setError(ErrorHandlerService.getAuthErrorMessage(e));
      return false;
    } catch (e) {
      _setError('An unexpected error occurred during registration.');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> signInWithGoogle() async {
    try {
      _setLoading(true);
      _setError(null);

      // Sign out first to show account picker
      await _googleSignIn.signOut();

      final googleUser = await _googleSignIn.signIn();
      if (googleUser == null) {
        _setError('Google sign in cancelled');
        return false;
      }

      final googleAuth = await googleUser.authentication;
      final credential = GoogleAuthProvider.credential(
        accessToken: googleAuth.accessToken,
        idToken: googleAuth.idToken,
      );

      final userCredential = await _auth.signInWithCredential(credential);
      final userId = userCredential.user!.uid;

      // Check if user exists in Firestore
      final userDoc = await _firestore.collection('users').doc(userId).get();

      if (!userDoc.exists) {
        // New user - needs to complete profile
        return false; // Return false to indicate profile completion needed
      }

      // Check if account is deleted
      final isDeleted = userDoc.data()?['deleted'] as bool? ?? false;
      if (isDeleted) {
        _setError('This account has been deleted');
        await _auth.signOut();
        return false;
      }

      // Update FCM token
      await _updateFcmToken();

      return true;
    } on FirebaseAuthException catch (e) {
      _setError(ErrorHandlerService.getAuthErrorMessage(e));
      return false;
    } catch (e) {
      _setError('Google sign-in failed. Please try again.');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> completeGoogleSignup({
    required String fullName,
    required String username,
    required String studentId,
    required String userType,
  }) async {
    try {
      _setLoading(true);
      _setError(null);

      final userId = _user?.uid;
      if (userId == null) {
        _setError('User not authenticated');
        return false;
      }

      // Check if username exists
      final usernameQuery = await _firestore
          .collection('users')
          .where('username', isEqualTo: username)
          .get();

      if (usernameQuery.docs.isNotEmpty) {
        _setError('Username already exists');
        return false;
      }

      // Get FCM token
      String? fcmToken;
      try {
        fcmToken = await FirebaseMessaging.instance.getToken();
      } catch (_) {}

      // Create user document
      final userData = {
        'fullName': fullName,
        'username': username,
        'email': _user?.email,
        'userId': userId,
        'userType': userType.toLowerCase(),
        'isAlumni': userType.toLowerCase() == 'alumni',
        'emailVerified': true, // Google accounts are pre-verified
        'createdAt': DateTime.now().millisecondsSinceEpoch,
        'fcmToken': fcmToken,
      };

      // Add appropriate ID field
      if (userType.toLowerCase() == 'alumni') {
        userData['alumniID'] = studentId;
      } else if (userType.toLowerCase() == 'staff') {
        userData['staffID'] = studentId;
      } else {
        userData['studentId'] = studentId;
      }

      await _firestore.collection('users').doc(userId).set(userData);

      return true;
    } catch (e) {
      _setError('Failed to complete signup');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> sendPasswordResetEmail(String email) async {
    try {
      _setLoading(true);
      _setError(null);

      await _auth.sendPasswordResetEmail(email: email);
      return true;
    } on FirebaseAuthException catch (e) {
      _setError(e.message ?? 'Failed to send reset email');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<void> signOut() async {
    try {
      await _googleSignIn.signOut();
      await _auth.signOut();
    } catch (_) {}
  }

  Future<void> _updateFcmToken() async {
    try {
      final token = await FirebaseMessaging.instance.getToken();
      if (token != null && _user != null) {
        await _firestore.collection('users').doc(_user!.uid).update({
          'fcmToken': token,
          'lastTokenUpdate': DateTime.now().millisecondsSinceEpoch,
        });
      }
    } catch (_) {}
  }

  Future<bool> checkUserExists() async {
    if (_user == null) return false;
    final doc = await _firestore.collection('users').doc(_user!.uid).get();
    return doc.exists;
  }

  void clearError() => _setError(null);
}
