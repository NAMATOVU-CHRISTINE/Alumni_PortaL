import 'package:firebase_core/firebase_core.dart' show FirebaseOptions;
import 'package:flutter/foundation.dart'
    show defaultTargetPlatform, kIsWeb, TargetPlatform;

class DefaultFirebaseOptions {
  static FirebaseOptions get currentPlatform {
    if (kIsWeb) {
      return web;
    }
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return android;
      case TargetPlatform.iOS:
        return ios;
      default:
        throw UnsupportedError('Unsupported platform');
    }
  }

  static const FirebaseOptions web = FirebaseOptions(
    apiKey: 'AIzaSyDbfdl9IsTtrMIOyW8pmuWYXqhJvvnRfH8',
    appId: '1:511866402860:web:5ac7f30fd0cea70ff3b989',
    messagingSenderId: '511866402860',
    projectId: 'alumniportal-198ec',
    storageBucket: 'alumniportal-198ec.firebasestorage.app',
    authDomain: 'alumniportal-198ec.firebaseapp.com',
  );

  static const FirebaseOptions android = FirebaseOptions(
    apiKey: 'AIzaSyDbfdl9IsTtrMIOyW8pmuWYXqhJvvnRfH8',
    appId: '1:511866402860:android:fe63612d9563517bf3b989',
    messagingSenderId: '511866402860',
    projectId: 'alumniportal-198ec',
    storageBucket: 'alumniportal-198ec.firebasestorage.app',
    databaseURL: 'https://alumniportal-198ec-default-rtdb.firebaseio.com',
  );

  static const FirebaseOptions ios = FirebaseOptions(
    apiKey: 'AIzaSyDbfdl9IsTtrMIOyW8pmuWYXqhJvvnRfH8',
    appId: '1:511866402860:ios:YOUR_IOS_APP_ID',
    messagingSenderId: '511866402860',
    projectId: 'alumniportal-198ec',
    storageBucket: 'alumniportal-198ec.firebasestorage.app',
    databaseURL: 'https://alumniportal-198ec-default-rtdb.firebaseio.com',
    iosBundleId: 'com.namatovu.alumniportal',
  );
}
