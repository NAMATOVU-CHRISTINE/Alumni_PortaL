import 'package:cloud_firestore/cloud_firestore.dart';

class GroupInitializationService {
  static final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  /// Initialize predefined class groups from 1989 to current year
  static Future<void> initializeClassGroups() async {
    final currentYear = DateTime.now().year;
    const startYear = 1989; // MUST was opened in October 1989

    try {
      // Check if groups already exist
      final existingGroups = await _firestore
          .collection('groups')
          .where('groupType', isEqualTo: 'Class')
          .limit(1)
          .get();

      if (existingGroups.docs.isNotEmpty) {
        print('Class groups already initialized');
        return;
      }

      // Create class groups for each year
      for (int year = startYear; year <= currentYear; year++) {
        final groupData = {
          'groupName': 'Class of $year',
          'groupDescription':
              'Official group for MUST alumni who graduated in $year. Connect with your classmates and share memories!',
          'groupType': 'Class',
          'graduationYear': year.toString(),
          'isPrivate': false,
          'memberCount': 0,
          'createdAt': DateTime.now().millisecondsSinceEpoch,
          'updatedAt': DateTime.now().millisecondsSinceEpoch,
          'isOfficial': true, // Mark as official MUST group
          'canBeDeleted': false, // Prevent deletion
        };

        await _firestore.collection('groups').add(groupData);
        print('Created group: Class of $year');
      }

      // Create additional official groups
      await _createOfficialGroups();

      print('Successfully initialized all class groups');
    } catch (e) {
      print('Error initializing class groups: $e');
    }
  }

  static Future<void> _createOfficialGroups() async {
    final officialGroups = [
      {
        'groupName': 'MUST Alumni Association',
        'groupDescription':
            'Official MUST Alumni Association. Stay connected with the university and fellow alumni.',
        'groupType': 'Official',
        'isPrivate': false,
        'isOfficial': true,
        'canBeDeleted': false,
      },
      {
        'groupName': 'Faculty of Medicine',
        'groupDescription':
            'Connect with fellow medical professionals and alumni from the Faculty of Medicine.',
        'groupType': 'Faculty',
        'isPrivate': false,
        'isOfficial': true,
        'canBeDeleted': false,
      },
      {
        'groupName': 'Faculty of Science',
        'groupDescription':
            'For alumni from the Faculty of Science. Share research, opportunities, and stay connected.',
        'groupType': 'Faculty',
        'isPrivate': false,
        'isOfficial': true,
        'canBeDeleted': false,
      },
      {
        'groupName': 'Faculty of Computing and Informatics',
        'groupDescription':
            'Tech professionals and IT alumni from MUST. Network and share opportunities.',
        'groupType': 'Faculty',
        'isPrivate': false,
        'isOfficial': true,
        'canBeDeleted': false,
      },
      {
        'groupName': 'Faculty of Business and Management',
        'groupDescription':
            'Business and management alumni. Connect for networking and career opportunities.',
        'groupType': 'Faculty',
        'isPrivate': false,
        'isOfficial': true,
        'canBeDeleted': false,
      },
      {
        'groupName': 'Faculty of Development Studies',
        'groupDescription':
            'Alumni from the Faculty of Development Studies. Share insights and opportunities.',
        'groupType': 'Faculty',
        'isPrivate': false,
        'isOfficial': true,
        'canBeDeleted': false,
      },
    ];

    for (final groupData in officialGroups) {
      final data = {
        ...groupData,
        'memberCount': 0,
        'createdAt': DateTime.now().millisecondsSinceEpoch,
        'updatedAt': DateTime.now().millisecondsSinceEpoch,
      };
      await _firestore.collection('groups').add(data);
      print('Created official group: ${groupData['groupName']}');
    }
  }

  /// Check if user is admin (can create custom groups)
  static Future<bool> isUserAdmin(String userId) async {
    try {
      final userDoc = await _firestore.collection('users').doc(userId).get();
      final userData = userDoc.data();
      return userData?['isAdmin'] == true || userData?['role'] == 'admin';
    } catch (e) {
      return false;
    }
  }
}
