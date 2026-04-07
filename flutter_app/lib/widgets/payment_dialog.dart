import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:alumni_portal/services/africas_talking_service.dart';

class PaymentDialog extends StatefulWidget {
  final double amount;
  final String purpose;
  final Function(bool success, String message) onComplete;

  const PaymentDialog({
    Key? key,
    required this.amount,
    required this.purpose,
    required this.onComplete,
  }) : super(key: key);

  @override
  State<PaymentDialog> createState() => _PaymentDialogState();
}

class _PaymentDialogState extends State<PaymentDialog> {
  final _phoneController = TextEditingController();
  bool _isProcessing = false;
  String _selectedProvider = 'MTN';

  @override
  void dispose() {
    _phoneController.dispose();
    super.dispose();
  }

  Future<void> _processPayment() async {
    if (_phoneController.text.isEmpty) {
      widget.onComplete(false, 'Please enter phone number');
      return;
    }

    setState(() => _isProcessing = true);

    try {
      final formattedPhone = AfricasTalkingService.formatPhoneNumber(
        _phoneController.text,
      );

      final result = await AfricasTalkingService.mobileCheckout(
        phoneNumber: formattedPhone,
        amount: widget.amount,
        currencyCode: 'UGX',
        metadata: json.encode({
          'purpose': widget.purpose,
          'provider': _selectedProvider,
        }),
      );

      if (result['success']) {
        Navigator.pop(context);
        widget.onComplete(true, 'Payment initiated successfully!');
      } else {
        widget.onComplete(false, result['error'].toString());
      }
    } catch (e) {
      widget.onComplete(false, 'Payment failed: $e');
    } finally {
      setState(() => _isProcessing = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('Mobile Money Payment'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            'Amount: UGX ${widget.amount.toStringAsFixed(0)}',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          SizedBox(height: 16),
          DropdownButtonFormField<String>(
            value: _selectedProvider,
            decoration: InputDecoration(
              labelText: 'Select Provider',
              border: OutlineInputBorder(),
            ),
            items: ['MTN', 'Airtel']
                .map((provider) => DropdownMenuItem(
                      value: provider,
                      child: Text(provider),
                    ))
                .toList(),
            onChanged: (value) {
              setState(() => _selectedProvider = value!);
            },
          ),
          SizedBox(height: 16),
          TextField(
            controller: _phoneController,
            decoration: InputDecoration(
              labelText: 'Phone Number',
              hintText: '0700123456',
              border: OutlineInputBorder(),
              prefixText: '+256 ',
            ),
            keyboardType: TextInputType.phone,
          ),
          SizedBox(height: 8),
          Text(
            'You will receive a prompt on your phone to confirm payment',
            style: TextStyle(fontSize: 12, color: Colors.grey),
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: _isProcessing ? null : () => Navigator.pop(context),
          child: Text('Cancel'),
        ),
        ElevatedButton(
          onPressed: _isProcessing ? null : _processPayment,
          child: _isProcessing
              ? SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              : Text('Pay Now'),
        ),
      ],
    );
  }
}
