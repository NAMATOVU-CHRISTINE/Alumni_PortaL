import 'package:flutter/foundation.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import '../models/guest_lecture_application.dart';

class GuestLectureProvider with ChangeNotifier {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;

  final List<GuestLectureApplication> _applications = [];
  List<GuestLectureApplication> _myApplications = [];
  List<GuestLectureApplication> _scheduledLectures = [];
  bool _isLoading = false;
  String? _error;

  List<GuestLectureApplication> get applications => _applications;
  List<GuestLectureApplication> get myApplications => _myApplications;
  List<GuestLectureApplication> get scheduledLectures => _scheduledLectures;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> fetchScheduledLectures() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final snapshot = await _firestore
          .collection('guest_lectures')
          .where('status', isEqualTo: 'scheduled')
          .orderBy('scheduledDate')
          .get();

      _scheduledLectures = snapshot.docs
          .map((doc) => GuestLectureApplication.fromFirestore(doc))
          .toList();

      _error = null;
    } catch (e) {
      _error = 'Failed to load scheduled lectures: $e';
      debugPrint(_error);
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchMyApplications() async {
    final userId = _auth.currentUser?.uid;
    if (userId == null) return;

    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final snapshot = await _firestore
          .collection('guest_lectures')
          .where('applicantId', isEqualTo: userId)
          .orderBy('submittedAt', descending: true)
          .get();

      _myApplications = snapshot.docs
          .map((doc) => GuestLectureApplication.fromFirestore(doc))
          .toList();

      _error = null;
    } catch (e) {
      _error = 'Failed to load applications: $e';
      debugPrint(_error);
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> submitApplication(GuestLectureApplication application) async {
    try {
      await _firestore
          .collection('guest_lectures')
          .add(application.toFirestore());
      await fetchMyApplications();
      return true;
    } catch (e) {
      _error = 'Failed to submit application: $e';
      debugPrint(_error);
      notifyListeners();
      return false;
    }
  }

  Future<bool> updateApplication(
      String id, GuestLectureApplication application) async {
    try {
      await _firestore
          .collection('guest_lectures')
          .doc(id)
          .update(application.toFirestore());
      await fetchMyApplications();
      return true;
    } catch (e) {
      _error = 'Failed to update application: $e';
      debugPrint(_error);
      notifyListeners();
      return false;
    }
  }

  Future<bool> withdrawApplication(String id) async {
    try {
      await _firestore.collection('guest_lectures').doc(id).delete();
      await fetchMyApplications();
      return true;
    } catch (e) {
      _error = 'Failed to withdraw application: $e';
      debugPrint(_error);
      notifyListeners();
      return false;
    }
  }

  Future<GuestLectureApplication?> getApplication(String id) async {
    try {
      final doc = await _firestore.collection('guest_lectures').doc(id).get();
      if (doc.exists) {
        return GuestLectureApplication.fromFirestore(doc);
      }
    } catch (e) {
      debugPrint('Error fetching application: $e');
    }
    return null;
  }

  Future<bool> registerForLecture(String lectureId) async {
    final userId = _auth.currentUser?.uid;
    if (userId == null) return false;

    try {
      await _firestore.collection('guest_lectures').doc(lectureId).update({
        'attendeeCount': FieldValue.increment(1),
      });

      await _firestore
          .collection('guest_lectures')
          .doc(lectureId)
          .collection('attendees')
          .doc(userId)
          .set({
        'userId': userId,
        'registeredAt': FieldValue.serverTimestamp(),
      });

      await fetchScheduledLectures();
      return true;
    } catch (e) {
      _error = 'Failed to register for lecture: $e';
      debugPrint(_error);
      notifyListeners();
      return false;
    }
  }
}
