import 'package:flutter/foundation.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/models/endorsement_model.dart';
import 'package:alumni_portal/services/notification_service.dart';

class EndorsementProvider with ChangeNotifier {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;

  List<SkillEndorsement> _skillEndorsements = [];
  List<RecommendationModel> _recommendations = [];
  bool _isLoading = false;

  List<SkillEndorsement> get skillEndorsements => _skillEndorsements;
  List<RecommendationModel> get recommendations => _recommendations;
  bool get isLoading => _isLoading;
  String? get currentUserId => _auth.currentUser?.uid;

  Future<void> loadEndorsements(String userId) async {
    try {
      _isLoading = true;
      notifyListeners();

      final snapshot = await _firestore
          .collection('endorsements')
          .where('endorsedUserId', isEqualTo: userId)
          .get();

      final endorsements = snapshot.docs
          .map((doc) => EndorsementModel.fromFirestore(doc))
          .toList();

      // Group by skill
      final Map<String, List<EndorsementModel>> grouped = {};
      for (final e in endorsements) {
        grouped.putIfAbsent(e.skill, () => []).add(e);
      }

      _skillEndorsements = grouped.entries
          .map((entry) => SkillEndorsement(
                skill: entry.key,
                count: entry.value.length,
                endorsements: entry.value,
              ))
          .toList()
        ..sort((a, b) => b.count.compareTo(a.count));
    } catch (e) {
      debugPrint('Error loading endorsements: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadRecommendations(String userId) async {
    try {
      final snapshot = await _firestore
          .collection('recommendations')
          .where('recommendedUserId', isEqualTo: userId)
          .orderBy('createdAt', descending: true)
          .get();

      _recommendations = snapshot.docs
          .map((doc) => RecommendationModel.fromFirestore(doc))
          .toList();
      notifyListeners();
    } catch (e) {
      debugPrint('Error loading recommendations: $e');
    }
  }

  Future<bool> endorseSkill(String userId, String skill) async {
    try {
      final currentUser = _auth.currentUser;
      if (currentUser == null || currentUser.uid == userId) return false;

      // Check if already endorsed
      final existing = await _firestore
          .collection('endorsements')
          .where('endorserId', isEqualTo: currentUser.uid)
          .where('endorsedUserId', isEqualTo: userId)
          .where('skill', isEqualTo: skill)
          .get();

      if (existing.docs.isNotEmpty) return false;

      final userDoc =
          await _firestore.collection('users').doc(currentUser.uid).get();
      final userData = userDoc.data() ?? {};

      final endorsement = EndorsementModel(
        id: '',
        endorserId: currentUser.uid,
        endorserName: userData['fullName'] ?? 'Alumni',
        endorserImageUrl: userData['profileImageUrl'],
        endorsedUserId: userId,
        skill: skill,
        createdAt: DateTime.now(),
      );

      await _firestore.collection('endorsements').add(endorsement.toMap());

      // Send notification
      NotificationService.notifySkillEndorsement(
        endorsedUserId: userId,
        endorserName: userData['fullName'] ?? 'Someone',
        skill: skill,
      );

      await loadEndorsements(userId);
      return true;
    } catch (e) {
      debugPrint('Error endorsing skill: $e');
      return false;
    }
  }

  Future<bool> removeEndorsement(String userId, String skill) async {
    try {
      final currentUser = _auth.currentUser;
      if (currentUser == null) return false;

      final snapshot = await _firestore
          .collection('endorsements')
          .where('endorserId', isEqualTo: currentUser.uid)
          .where('endorsedUserId', isEqualTo: userId)
          .where('skill', isEqualTo: skill)
          .get();

      for (final doc in snapshot.docs) {
        await doc.reference.delete();
      }

      await loadEndorsements(userId);
      return true;
    } catch (e) {
      debugPrint('Error removing endorsement: $e');
      return false;
    }
  }

  bool hasEndorsed(String skill) {
    for (final se in _skillEndorsements) {
      if (se.skill == skill) {
        return se.endorsements.any((e) => e.endorserId == currentUserId);
      }
    }
    return false;
  }

  Future<bool> writeRecommendation({
    required String userId,
    required String relationship,
    required String content,
  }) async {
    try {
      final currentUser = _auth.currentUser;
      if (currentUser == null || currentUser.uid == userId) return false;

      final userDoc =
          await _firestore.collection('users').doc(currentUser.uid).get();
      final userData = userDoc.data() ?? {};

      final recommendation = RecommendationModel(
        id: '',
        recommenderId: currentUser.uid,
        recommenderName: userData['fullName'] ?? 'Alumni',
        recommenderImageUrl: userData['profileImageUrl'],
        recommenderTitle: userData['currentJob'],
        recommendedUserId: userId,
        relationship: relationship,
        content: content,
        createdAt: DateTime.now(),
      );

      await _firestore
          .collection('recommendations')
          .add(recommendation.toMap());
      await loadRecommendations(userId);
      return true;
    } catch (e) {
      debugPrint('Error writing recommendation: $e');
      return false;
    }
  }
}
