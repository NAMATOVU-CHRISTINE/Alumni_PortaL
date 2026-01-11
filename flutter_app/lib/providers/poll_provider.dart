import 'package:flutter/foundation.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/models/poll_model.dart';
import 'package:alumni_portal/services/notification_service.dart';

class PollProvider with ChangeNotifier {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;

  List<PollModel> _polls = [];
  bool _isLoading = false;
  String? _error;

  List<PollModel> get polls => _polls;
  List<PollModel> get activePolls =>
      _polls.where((p) => !p.isExpired && p.isActive).toList();
  bool get isLoading => _isLoading;
  String? get error => _error;

  String? get currentUserId => _auth.currentUser?.uid;

  Future<void> loadPolls() async {
    try {
      _isLoading = true;
      notifyListeners();

      final snapshot = await _firestore
          .collection('polls')
          .orderBy('createdAt', descending: true)
          .limit(20)
          .get();

      _polls =
          snapshot.docs.map((doc) => PollModel.fromFirestore(doc)).toList();
    } catch (e) {
      _error = 'Failed to load polls';
      debugPrint('Error loading polls: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> createPoll({
    required String question,
    required List<String> options,
    int durationDays = 7,
  }) async {
    try {
      _isLoading = true;
      notifyListeners();

      final user = _auth.currentUser;
      if (user == null) return false;

      final userDoc = await _firestore.collection('users').doc(user.uid).get();
      final userData = userDoc.data() ?? {};

      final pollOptions = options
          .asMap()
          .entries
          .map((e) => PollOption(
                id: 'opt_${e.key}',
                text: e.value,
                votes: 0,
              ))
          .toList();

      final poll = PollModel(
        id: '',
        authorId: user.uid,
        authorName: userData['fullName'] ?? 'Alumni',
        authorImageUrl: userData['profileImageUrl'],
        question: question,
        options: pollOptions,
        createdAt: DateTime.now(),
        expiresAt: DateTime.now().add(Duration(days: durationDays)),
      );

      await _firestore.collection('polls').add(poll.toMap());
      await loadPolls();
      return true;
    } catch (e) {
      _error = 'Failed to create poll';
      debugPrint('Error creating poll: $e');
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> vote(String pollId, String optionId) async {
    try {
      final userId = currentUserId;
      if (userId == null) return false;

      final pollRef = _firestore.collection('polls').doc(pollId);
      final pollDoc = await pollRef.get();

      if (!pollDoc.exists) return false;

      final poll = PollModel.fromFirestore(pollDoc);

      // Check if already voted
      if (poll.votedUserIds.contains(userId)) return false;

      // Update vote count for the option
      final updatedOptions = poll.options.map((opt) {
        if (opt.id == optionId) {
          return PollOption(id: opt.id, text: opt.text, votes: opt.votes + 1);
        }
        return opt;
      }).toList();

      await pollRef.update({
        'options': updatedOptions.map((o) => o.toMap()).toList(),
        'votedUserIds': FieldValue.arrayUnion([userId]),
      });

      // Send notification to poll author
      if (poll.authorId != userId) {
        final userDoc = await _firestore.collection('users').doc(userId).get();
        final voterName = userDoc.data()?['fullName'] ?? 'Someone';
        NotificationService.notifyPollVote(
          pollAuthorId: poll.authorId,
          voterName: voterName,
          pollId: pollId,
        );
      }

      await loadPolls();
      return true;
    } catch (e) {
      _error = 'Failed to vote';
      debugPrint('Error voting: $e');
      return false;
    }
  }

  bool hasVoted(PollModel poll) {
    return poll.votedUserIds.contains(currentUserId);
  }
}
