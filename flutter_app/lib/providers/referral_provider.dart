import 'package:flutter/foundation.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/models/referral_model.dart';
import 'package:alumni_portal/services/notification_service.dart';

class ReferralProvider with ChangeNotifier {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;

  List<ReferralModel> _sentReferrals = [];
  List<ReferralModel> _receivedReferrals = [];
  bool _isLoading = false;

  List<ReferralModel> get sentReferrals => _sentReferrals;
  List<ReferralModel> get receivedReferrals => _receivedReferrals;
  bool get isLoading => _isLoading;
  String? get currentUserId => _auth.currentUser?.uid;

  Future<void> loadReferrals() async {
    try {
      _isLoading = true;
      notifyListeners();

      final userId = currentUserId;
      if (userId == null) return;

      // Load sent referrals
      final sentSnapshot = await _firestore
          .collection('referrals')
          .where('referrerId', isEqualTo: userId)
          .orderBy('createdAt', descending: true)
          .get();

      _sentReferrals = sentSnapshot.docs
          .map((doc) => ReferralModel.fromFirestore(doc))
          .toList();

      // Load received referrals
      final receivedSnapshot = await _firestore
          .collection('referrals')
          .where('referredUserId', isEqualTo: userId)
          .orderBy('createdAt', descending: true)
          .get();

      _receivedReferrals = receivedSnapshot.docs
          .map((doc) => ReferralModel.fromFirestore(doc))
          .toList();
    } catch (e) {
      debugPrint('Error loading referrals: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> createReferral({
    required String jobId,
    required String jobTitle,
    required String company,
    required String referredUserId,
    String? note,
  }) async {
    try {
      final currentUser = _auth.currentUser;
      if (currentUser == null || currentUser.uid == referredUserId)
        return false;

      // Check if already referred
      final existing = await _firestore
          .collection('referrals')
          .where('jobId', isEqualTo: jobId)
          .where('referrerId', isEqualTo: currentUser.uid)
          .where('referredUserId', isEqualTo: referredUserId)
          .get();

      if (existing.docs.isNotEmpty) return false;

      // Get user details
      final referrerDoc =
          await _firestore.collection('users').doc(currentUser.uid).get();
      final referrerData = referrerDoc.data() ?? {};

      final referredDoc =
          await _firestore.collection('users').doc(referredUserId).get();
      final referredData = referredDoc.data() ?? {};

      final referral = ReferralModel(
        id: '',
        jobId: jobId,
        jobTitle: jobTitle,
        company: company,
        referrerId: currentUser.uid,
        referrerName: referrerData['fullName'] ?? 'Alumni',
        referrerImageUrl: referrerData['profileImageUrl'],
        referredUserId: referredUserId,
        referredUserName: referredData['fullName'] ?? 'Alumni',
        referredUserEmail: referredData['email'],
        note: note,
        createdAt: DateTime.now(),
      );

      await _firestore.collection('referrals').add(referral.toMap());

      // Send notification
      NotificationService.notifyJobReferral(
        referredUserId: referredUserId,
        referrerName: referrerData['fullName'] ?? 'Someone',
        jobTitle: jobTitle,
        referralId: jobId,
      );

      await loadReferrals();
      return true;
    } catch (e) {
      debugPrint('Error creating referral: $e');
      return false;
    }
  }

  Future<bool> updateReferralStatus(String referralId, String status) async {
    try {
      await _firestore.collection('referrals').doc(referralId).update({
        'status': status,
        'updatedAt': DateTime.now().millisecondsSinceEpoch,
      });
      await loadReferrals();
      return true;
    } catch (e) {
      debugPrint('Error updating referral status: $e');
      return false;
    }
  }

  int get pendingReceivedCount =>
      _receivedReferrals.where((r) => r.status == 'pending').length;
}
