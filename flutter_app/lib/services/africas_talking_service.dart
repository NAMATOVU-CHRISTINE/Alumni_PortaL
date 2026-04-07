import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:flutter_dotenv/flutter_dotenv.dart';

class AfricasTalkingService {
  static const String _baseUrl = 'https://payments.africastalking.com';
  static String get _apiKey => dotenv.env['AFRICAS_TALKING_API_KEY'] ?? '';
  static String get _username => dotenv.env['AFRICAS_TALKING_USERNAME'] ?? 'sandbox';
  static String get _productName => dotenv.env['AFRICAS_TALKING_PRODUCT_NAME'] ?? 'Convocation';

  // Mobile Checkout - Initiate payment
  static Future<Map<String, dynamic>> mobileCheckout({
    required String phoneNumber,
    required double amount,
    required String currencyCode, // UGX for Uganda
    required String metadata,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/mobile/checkout/request'),
        headers: {
          'apiKey': _apiKey,
          'Content-Type': 'application/x-www-form-urlencoded',
          'Accept': 'application/json',
        },
        body: {
          'username': _username,
          'productName': _productName,
          'phoneNumber': phoneNumber,
          'currencyCode': currencyCode,
          'amount': amount.toString(),
          'metadata': metadata,
        },
      );

      if (response.statusCode == 201) {
        return {
          'success': true,
          'data': json.decode(response.body),
        };
      } else {
        return {
          'success': false,
          'error': json.decode(response.body),
        };
      }
    } catch (e) {
      return {
        'success': false,
        'error': 'Connection error: $e',
      };
    }
  }

  // B2C Payment - Send money to users
  static Future<Map<String, dynamic>> b2cPayment({
    required String phoneNumber,
    required double amount,
    required String currencyCode,
    required String reason,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/mobile/b2c/request'),
        headers: {
          'apiKey': _apiKey,
          'Content-Type': 'application/x-www-form-urlencoded',
          'Accept': 'application/json',
        },
        body: {
          'username': _username,
          'productName': _productName,
          'recipients': json.encode([
            {
              'phoneNumber': phoneNumber,
              'currencyCode': currencyCode,
              'amount': amount,
              'reason': reason,
              'metadata': {},
            }
          ]),
        },
      );

      if (response.statusCode == 201) {
        return {
          'success': true,
          'data': json.decode(response.body),
        };
      } else {
        return {
          'success': false,
          'error': json.decode(response.body),
        };
      }
    } catch (e) {
      return {
        'success': false,
        'error': 'Connection error: $e',
      };
    }
  }

  // Query transaction status
  static Future<Map<String, dynamic>> queryTransactionStatus({
    required String transactionId,
  }) async {
    try {
      final response = await http.get(
        Uri.parse('$_baseUrl/query/transaction/fetch'),
        headers: {
          'apiKey': _apiKey,
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        return {
          'success': true,
          'data': json.decode(response.body),
        };
      } else {
        return {
          'success': false,
          'error': json.decode(response.body),
        };
      }
    } catch (e) {
      return {
        'success': false,
        'error': 'Connection error: $e',
      };
    }
  }

  // Format phone number for Africas Talking (must include country code)
  static String formatPhoneNumber(String phone) {
    // Remove spaces and special characters
    phone = phone.replaceAll(RegExp(r'[^\d+]'), '');
    
    // Add Uganda country code if not present
    if (!phone.startsWith('+')) {
      if (phone.startsWith('0')) {
        phone = '+256${phone.substring(1)}';
      } else if (!phone.startsWith('256')) {
        phone = '+256$phone';
      } else {
        phone = '+$phone';
      }
    }
    
    return phone;
  }
}
