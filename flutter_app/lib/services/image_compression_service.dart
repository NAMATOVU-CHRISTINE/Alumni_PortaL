import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:image/image.dart' as img;
import 'package:path_provider/path_provider.dart';

class ImageCompressionService {
  /// Compress image before upload
  /// Reduces file size while maintaining quality
  static Future<File> compressImage(
    File file, {
    int maxWidth = 1920,
    int maxHeight = 1920,
    int quality = 85,
  }) async {
    try {
      // Read image
      final bytes = await file.readAsBytes();
      img.Image? image = img.decodeImage(bytes);
      
      if (image == null) return file;

      // Resize if needed
      if (image.width > maxWidth || image.height > maxHeight) {
        image = img.copyResize(
          image,
          width: image.width > maxWidth ? maxWidth : null,
          height: image.height > maxHeight ? maxHeight : null,
        );
      }

      // Compress
      final compressedBytes = img.encodeJpg(image, quality: quality);

      // Save to temp file
      final tempDir = await getTemporaryDirectory();
      final tempFile = File(
        '${tempDir.path}/compressed_${DateTime.now().millisecondsSinceEpoch}.jpg',
      );
      await tempFile.writeAsBytes(compressedBytes);

      // Log compression results
      final originalSize = await file.length();
      final compressedSize = await tempFile.length();
      final reduction = ((originalSize - compressedSize) / originalSize * 100).toStringAsFixed(1);
      
      debugPrint('Image compressed: ${originalSize ~/ 1024}KB → ${compressedSize ~/ 1024}KB ($reduction% reduction)');

      return tempFile;
    } catch (e) {
      debugPrint('Error compressing image: $e');
      return file; // Return original if compression fails
    }
  }

  /// Compress image for profile pictures (smaller size)
  static Future<File> compressProfileImage(File file) {
    return compressImage(
      file,
      maxWidth: 800,
      maxHeight: 800,
      quality: 80,
    );
  }

  /// Compress image for stories (medium size)
  static Future<File> compressStoryImage(File file) {
    return compressImage(
      file,
      maxWidth: 1080,
      maxHeight: 1920,
      quality: 85,
    );
  }

  /// Compress image for posts (standard size)
  static Future<File> compressPostImage(File file) {
    return compressImage(
      file,
      maxWidth: 1920,
      maxHeight: 1920,
      quality: 85,
    );
  }
}
