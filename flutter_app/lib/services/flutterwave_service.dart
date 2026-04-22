import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:flutter_dotenv/flutter_dotenv.dart';

class FlutterwaveService {
  static const String _baseUrl = 'https://api.flutterwave.com/v3';
  static String get _secretKey => dotenv.env['FLUTTERWAVE_SECRET_KEY'] ?? '';
  static String get _publicKey => dotenv.env['FLUTTERWAVE_PUBLIC_KEY'] ?? '';

  // Initiate Mobile Money Payment (MTN/Airtel Uganda)
  static Future<Map<String, dynamic>> initiateMobileMoney({
    required String phoneNumber,
    required double amount,
    required String email,
    required String name,
    required String description,
    required String txRef,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/charges?type=mobile_money_uganda'),
        headers: {
          'Authorization': 'Bearer $_secretKey',
          'Content-Type': 'application/json',
        },
        body: json.encode({
          'phone_number': phoneNumber,
          'amount': amount,
          'currency': 'UGX',
          'email': email,
          'fullname': name,
          'tx_ref': txRef,
          'narration': description,
        }),
      );

      final data = json.decode(response.body);

      if (response.statusCode == 200 && data['status'] == 'success') {
        return {
          'success': true,
          'data': data['data'],
          'message': data['message'],
        };
      } else {
        return {
          'success': false,
          'message': data['message'] ?? 'Payment failed',
        };
      }
    } catch (e) {
      return {
        'success': false,
        'message': 'Connection error: $e',
      };
    }
  }

  // Verify Transaction
  static Future<Map<String, dynamic>> verifyTransaction({
    required String transactionId,
  }) async {
    try {
      final response = await http.get(
        Uri.parse('$_baseUrl/transactions/$transactionId/verify'),
        headers: {
          'Authorization': 'Bearer $_secretKey',
          'Content-Type': 'application/json',
        },
      );

      final data = json.decode(response.body);

      if (response.statusCode == 200 && data['status'] == 'success') {
        return {
          'success': true,
          'data': data['data'],
        };
      } else {
        return {
          'success': false,
          'message': data['message'] ?? 'Verification failed',
        };
      }
    } catch (e) {
      return {
        'success': false,
        'message': 'Connection error: $e',
      };
    }
  }

  // Format phone number for Flutterwave (Uganda format)
  static String formatPhoneNumber(String phone) {
    phone = phone.replaceAll(RegExp(r'[^\d+]'), '');
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

  // Generate unique transaction reference
  static String generateTxRef(String prefix) {
    final timestamp = DateTime.now().millisecondsSinceEpoch;
    return '${prefix}_$timestamp';
  }
}
