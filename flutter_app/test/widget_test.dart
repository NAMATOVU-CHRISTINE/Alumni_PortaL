// Basic Flutter widget test for Alumni Portal

import 'package:flutter_test/flutter_test.dart';
import 'package:alumni_portal/main.dart';

void main() {
  testWidgets('App loads successfully', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const AlumniPortalApp());

    // Verify the app loads (basic smoke test)
    expect(find.text('Alumni Portal'), findsWidgets);
  });
}
