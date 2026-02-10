import 'package:flutter/foundation.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/convocation_team_member.dart';

class ConvocationProvider with ChangeNotifier {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  List<ConvocationTeamMember> _teamMembers = [];
  bool _isLoading = false;
  String? _error;

  List<ConvocationTeamMember> get teamMembers => _teamMembers;
  bool get isLoading => _isLoading;
  String? get error => _error;

  List<ConvocationTeamMember> get activeMembers =>
      _teamMembers.where((m) => m.isActive).toList()
        ..sort((a, b) => a.displayOrder.compareTo(b.displayOrder));

  Map<String, List<ConvocationTeamMember>> get membersByCommittee {
    final Map<String, List<ConvocationTeamMember>> grouped = {};
    for (var member in activeMembers) {
      if (!grouped.containsKey(member.committee)) {
        grouped[member.committee] = [];
      }
      grouped[member.committee]!.add(member);
    }
    return grouped;
  }

  Future<void> fetchTeamMembers() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final snapshot = await _firestore
          .collection('convocation_team')
          .orderBy('displayOrder')
          .get();

      _teamMembers = snapshot.docs
          .map((doc) => ConvocationTeamMember.fromFirestore(doc))
          .toList();

      _error = null;
    } catch (e) {
      _error = 'Failed to load team members: $e';
      debugPrint(_error);
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<ConvocationTeamMember?> getTeamMember(String id) async {
    try {
      final doc = await _firestore.collection('convocation_team').doc(id).get();
      if (doc.exists) {
        return ConvocationTeamMember.fromFirestore(doc);
      }
    } catch (e) {
      debugPrint('Error fetching team member: $e');
    }
    return null;
  }

  Future<bool> addTeamMember(ConvocationTeamMember member) async {
    try {
      await _firestore.collection('convocation_team').add(member.toFirestore());
      await fetchTeamMembers();
      return true;
    } catch (e) {
      _error = 'Failed to add team member: $e';
      debugPrint(_error);
      notifyListeners();
      return false;
    }
  }

  Future<bool> updateTeamMember(String id, ConvocationTeamMember member) async {
    try {
      await _firestore
          .collection('convocation_team')
          .doc(id)
          .update(member.toFirestore());
      await fetchTeamMembers();
      return true;
    } catch (e) {
      _error = 'Failed to update team member: $e';
      debugPrint(_error);
      notifyListeners();
      return false;
    }
  }

  Future<bool> deleteTeamMember(String id) async {
    try {
      await _firestore.collection('convocation_team').doc(id).delete();
      await fetchTeamMembers();
      return true;
    } catch (e) {
      _error = 'Failed to delete team member: $e';
      debugPrint(_error);
      notifyListeners();
      return false;
    }
  }
}
