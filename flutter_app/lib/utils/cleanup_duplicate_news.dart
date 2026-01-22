import 'package:cloud_firestore/cloud_firestore.dart';

/// Utility to find and delete duplicate news articles in Firestore
/// Run this once to clean up your database
class CleanupDuplicateNews {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  /// Find and delete duplicate news articles based on title
  /// Keeps the most recent article (by publishedAt) for each unique title
  Future<void> cleanupDuplicates() async {
    print('🔍 Starting duplicate cleanup...');

    try {
      // Get all news documents
      final snapshot = await _firestore
          .collection('news')
          .orderBy('publishedAt', descending: true)
          .get();

      print('📊 Total documents: ${snapshot.docs.length}');

      // Group by title
      final Map<String, List<DocumentSnapshot>> groupedByTitle = {};

      for (var doc in snapshot.docs) {
        final data = doc.data();
        final title = data['title'] as String?;

        if (title == null || title.isEmpty) {
          print('⚠️  Skipping document ${doc.id} - no title');
          continue;
        }

        if (!groupedByTitle.containsKey(title)) {
          groupedByTitle[title] = [];
        }
        groupedByTitle[title]!.add(doc);
      }

      // Find duplicates
      int duplicateCount = 0;
      int deletedCount = 0;
      final List<String> toDelete = [];

      for (var entry in groupedByTitle.entries) {
        final title = entry.key;
        final docs = entry.value;

        if (docs.length > 1) {
          duplicateCount += docs.length - 1;
          print('\n⚠️  Found ${docs.length} copies of: "$title"');

          // Keep the first one (most recent), mark others for deletion
          for (int i = 1; i < docs.length; i++) {
            final docToDelete = docs[i];
            print('   🗑️  Will delete: ${docToDelete.id}');
            toDelete.add(docToDelete.id);
          }

          print('   ✅ Keeping: ${docs[0].id}');
        }
      }

      print('\n📈 Summary:');
      print('   Total articles: ${snapshot.docs.length}');
      print('   Unique titles: ${groupedByTitle.length}');
      print('   Duplicates found: $duplicateCount');
      print('   Documents to delete: ${toDelete.length}');

      if (toDelete.isEmpty) {
        print('\n✨ No duplicates found! Database is clean.');
        return;
      }

      // Delete duplicates
      print('\n🗑️  Deleting ${toDelete.length} duplicate documents...');

      final batch = _firestore.batch();
      for (var docId in toDelete) {
        batch.delete(_firestore.collection('news').doc(docId));
        deletedCount++;
      }

      await batch.commit();

      print('✅ Successfully deleted $deletedCount duplicate documents!');
      print('🎉 Cleanup complete!');
    } catch (e) {
      print('❌ Error during cleanup: $e');
      rethrow;
    }
  }

  /// Preview duplicates without deleting (safe to run)
  Future<void> previewDuplicates() async {
    print('🔍 Previewing duplicates (no changes will be made)...\n');

    try {
      final snapshot = await _firestore
          .collection('news')
          .orderBy('publishedAt', descending: true)
          .get();

      print('📊 Total documents: ${snapshot.docs.length}');

      final Map<String, List<Map<String, dynamic>>> groupedByTitle = {};

      for (var doc in snapshot.docs) {
        final data = doc.data();
        final title = data['title'] as String?;

        if (title == null || title.isEmpty) continue;

        if (!groupedByTitle.containsKey(title)) {
          groupedByTitle[title] = [];
        }

        groupedByTitle[title]!.add({
          'id': doc.id,
          'publishedAt': data['publishedAt'],
          'category': data['category'],
        });
      }

      int duplicateCount = 0;

      for (var entry in groupedByTitle.entries) {
        final title = entry.key;
        final docs = entry.value;

        if (docs.length > 1) {
          duplicateCount += docs.length - 1;
          print('\n📰 "$title" (${docs.length} copies):');

          for (int i = 0; i < docs.length; i++) {
            final doc = docs[i];
            final status = i == 0 ? '✅ KEEP' : '🗑️  DELETE';
            print('   $status - ID: ${doc['id']}');
          }
        }
      }

      print('\n📈 Summary:');
      print('   Total articles: ${snapshot.docs.length}');
      print('   Unique titles: ${groupedByTitle.length}');
      print('   Duplicates found: $duplicateCount');
    } catch (e) {
      print('❌ Error during preview: $e');
      rethrow;
    }
  }
}
