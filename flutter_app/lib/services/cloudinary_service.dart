import 'dart:io';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:crypto/crypto.dart';

class CloudinaryService {
  static String get cloudName => 'dekdqvxwi';
  static String get apiKey => dotenv.env['CLOUDINARY_API_KEY'] ?? '';
  static String get apiSecret => dotenv.env['CLOUDINARY_API_SECRET'] ?? '';
  static String get uploadUrl =>
      'https://api.cloudinary.com/v1_1/$cloudName/image/upload';

  static Future<String?> uploadImage(File imageFile,
      {String folder = 'alumni_portal'}) async {
    try {
      final timestamp = DateTime.now().millisecondsSinceEpoch.toString();
      final signature = _generateSignature(timestamp, folder);

      final request = http.MultipartRequest('POST', Uri.parse(uploadUrl));
      request.fields['timestamp'] = timestamp;
      request.fields['api_key'] = apiKey;
      request.fields['signature'] = signature;
      request.fields['folder'] = folder;

      request.files
          .add(await http.MultipartFile.fromPath('file', imageFile.path));

      final response = await request.send();
      final responseData = await response.stream.bytesToString();

      if (response.statusCode == 200) {
        final jsonResponse = json.decode(responseData);
        return jsonResponse['secure_url'] as String;
      } else {
        print('Cloudinary upload failed: $responseData');
        return null;
      }
    } catch (e) {
      print('Error uploading to Cloudinary: $e');
      return null;
    }
  }

  static String _generateSignature(String timestamp, String folder) {
    final params = 'folder=$folder&timestamp=$timestamp$apiSecret';
    final bytes = utf8.encode(params);
    final digest = sha1.convert(bytes);
    return digest.toString();
  }
}
