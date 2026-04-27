import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:flutter/foundation.dart';

class PandoraPaymentService {
  static const String _baseUrl = 'https://api.pandorapayments.com/v1';
  static String get _apiKey {
    final key = dotenv.env['PANDORA_API_KEY'] ?? '';
    debugPrint('🔑 Pandora API Key: ${key.isEmpty ? "EMPTY" : "Loaded (${key.length} chars)"}');
    return key;
  }

  // Initiate Mobile Money Payment (MTN/Airtel Uganda)
  static Future<Map<String, dynamic>> initiateMobileMoney({
    required String phoneNumber,
    required double amount,
    required String reference,
    required String narrative,
    required String callbackUrl,
  }) async {
    try {
      // Check if API key is available
      if (_apiKey.isEmpty) {
        debugPrint('❌ ERROR: Pandora API key is missing or empty');
        return {
          'success': false,
          'message': 'Payment service not configured. Please contact support.',
        };
      }
      
      final formattedPhone = formatPhoneNumber(phoneNumber);
      
      debugPrint('📱 ========== PANDORA PAYMENT REQUEST ==========');
      debugPrint('💰 Amount: UGX $amount');
      debugPrint('📞 Phone (original): $phoneNumber');
      debugPrint('📞 Phone (formatted): $formattedPhone');
      debugPrint('🔖 Reference: $reference');
      debugPrint('📝 Narrative: $narrative');
      debugPrint('🔗 Callback: $callbackUrl');
      debugPrint('🔑 API Key: ${_apiKey.substring(0, 20)}...');
      
      final requestBody = {
        'amount': amount,
        'transaction_ref': reference,
        'contact': formattedPhone,
        'narrative': narrative,
        'callback_url': callbackUrl,
      };
      
      debugPrint('📤 Request Body: ${json.encode(requestBody)}');

      final response = await http.post(
        Uri.parse('$_baseUrl/transactions/mobile-money'),
        headers: {
          'X-API-Key': _apiKey,
          'Content-Type': 'application/json',
        },
        body: json.encode(requestBody),
      );

      debugPrint('📥 Response Status: ${response.statusCode}');
      debugPrint('📥 Response Body: ${response.body}');
      debugPrint('===============================================');

      final data = json.decode(response.body);

      // Pandora returns success in the root level
      if (response.statusCode == 200 || response.statusCode == 201) {
        final bool apiSuccess = data['success'] ?? false;
        String message = (data['messages'] as List?)?.first ?? 
                         'Payment initiated. Check your phone for the prompt.';
        
        // Remove Pandora branding from message
        message = message.replaceAll(RegExp(r'thank you for using pandora', caseSensitive: false), '');
        message = message.replaceAll(RegExp(r'pandora', caseSensitive: false), '');
        message = message.trim();
        
        // If message is empty after removal, use default
        if (message.isEmpty) {
          message = 'Payment initiated. Check your phone for the prompt.';
        }
        
        return {
          'success': apiSuccess,
          'data': data,
          'message': message,
        };
      } else {
        String errorMessage = (data['messages'] as List?)?.first ?? 
                              data['message'] ?? 
                              'Payment failed. Please try again.';
        
        // Remove Pandora branding from error messages too
        errorMessage = errorMessage.replaceAll(RegExp(r'pandora', caseSensitive: false), '');
        errorMessage = errorMessage.trim();
        
        if (errorMessage.isEmpty) {
          errorMessage = 'Payment failed. Please try again.';
        }
        
        return {
          'success': false,
          'message': errorMessage,
        };
      }
    } catch (e) {
      debugPrint('❌ PAYMENT ERROR: $e');
      return {
        'success': false,
        'message': 'Connection error: $e',
      };
    }
  }

  // Format phone number to Pandora format: 256XXXXXXXXX
  static String formatPhoneNumber(String phone) {
    // Remove all non-digit characters except +
    phone = phone.replaceAll(RegExp(r'[^\d]'), '');

    // Handle different formats
    if (phone.startsWith('0')) {
      // 0700123456 -> 256700123456
      phone = '256${phone.substring(1)}';
    } else if (phone.startsWith('256')) {
      // Already in correct format
      phone = phone;
    } else if (phone.length == 9) {
      // 700123456 -> 256700123456
      phone = '256$phone';
    }

    return phone;
  }

  // Generate unique transaction reference
  static String generateReference(String prefix) {
    final timestamp = DateTime.now().millisecondsSinceEpoch;
    return '${prefix}_$timestamp';
  }
}
