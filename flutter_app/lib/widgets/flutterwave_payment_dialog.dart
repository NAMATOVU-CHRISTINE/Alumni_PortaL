import 'package:flutter/material.dart';
import 'package:alumni_portal/services/flutterwave_service.dart';
import 'package:alumni_portal/config/theme.dart';

class FlutterwavePaymentDialog extends StatefulWidget {
  final double amount;
  final String purpose;
  final Function(bool success, String message) onComplete;

  const FlutterwavePaymentDialog({
    Key? key,
    required this.amount,
    required this.purpose,
    required this.onComplete,
  }) : super(key: key);

  @override
  State<FlutterwavePaymentDialog> createState() =>
      _FlutterwavePaymentDialogState();
}

class _FlutterwavePaymentDialogState extends State<FlutterwavePaymentDialog> {
  final _phoneController = TextEditingController();
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  bool _isProcessing = false;
  String _selectedProvider = 'MTN';

  @override
  void dispose() {
    _phoneController.dispose();
    _nameController.dispose();
    _emailController.dispose();
    super.dispose();
  }

  Future<void> _processPayment() async {
    if (_phoneController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter your phone number')),
      );
      return;
    }

    if (_nameController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter your name')),
      );
      return;
    }

    if (_emailController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter your email')),
      );
      return;
    }

    setState(() => _isProcessing = true);

    try {
      final formattedPhone = FlutterwaveService.formatPhoneNumber(
        _phoneController.text,
      );

      final txRef = FlutterwaveService.generateTxRef('convocation');

      final result = await FlutterwaveService.initiateMobileMoney(
        phoneNumber: formattedPhone,
        amount: widget.amount,
        email: _emailController.text,
        name: _nameController.text,
        description: widget.purpose,
        txRef: txRef,
      );

      if (!mounted) return;
      Navigator.pop(context);

      if (result['success']) {
        widget.onComplete(
          true,
          'Payment initiated! Check your phone for the mobile money prompt.',
        );
      } else {
        widget.onComplete(false, result['message'] ?? 'Payment failed');
      }
    } catch (e) {
      if (!mounted) return;
      widget.onComplete(false, 'Payment failed: $e');
    } finally {
      if (mounted) setState(() => _isProcessing = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text(
        'Mobile Money Payment',
        style: TextStyle(fontWeight: FontWeight.bold),
      ),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Amount display
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppColors.primary.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Amount:', style: TextStyle(fontSize: 14)),
                  Text(
                    'UGX ${widget.amount.toStringAsFixed(0).replaceAllMapped(RegExp(r'(\d{1,3})(?=(\d{3})+(?!\d))'), (m) => '${m[1]},')}',
                    style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: AppColors.primary,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Provider selection
            DropdownButtonFormField<String>(
              value: _selectedProvider,
              decoration: const InputDecoration(
                labelText: 'Mobile Money Provider',
                border: OutlineInputBorder(),
                contentPadding:
                    EdgeInsets.symmetric(horizontal: 12, vertical: 8),
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
            const SizedBox(height: 12),

            // Phone number
            TextField(
              controller: _phoneController,
              decoration: const InputDecoration(
                labelText: 'Phone Number',
                hintText: '0700123456',
                border: OutlineInputBorder(),
                prefixText: '+256 ',
                contentPadding:
                    EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              ),
              keyboardType: TextInputType.phone,
            ),
            const SizedBox(height: 12),

            // Full name
            TextField(
              controller: _nameController,
              decoration: const InputDecoration(
                labelText: 'Full Name',
                hintText: 'Enter your full name',
                border: OutlineInputBorder(),
                contentPadding:
                    EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              ),
            ),
            const SizedBox(height: 12),

            // Email
            TextField(
              controller: _emailController,
              decoration: const InputDecoration(
                labelText: 'Email',
                hintText: 'Enter your email',
                border: OutlineInputBorder(),
                contentPadding:
                    EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              ),
              keyboardType: TextInputType.emailAddress,
            ),
            const SizedBox(height: 8),

            Text(
              'You will receive a mobile money prompt on your phone to confirm payment.',
              style: TextStyle(
                fontSize: 12,
                color: Colors.grey[600],
                fontStyle: FontStyle.italic,
              ),
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: _isProcessing ? null : () => Navigator.pop(context),
          child: const Text('Cancel'),
        ),
        ElevatedButton(
          onPressed: _isProcessing ? null : _processPayment,
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primary,
          ),
          child: _isProcessing
              ? const SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: Colors.white,
                  ),
                )
              : const Text(
                  'Pay Now',
                  style: TextStyle(color: Colors.white),
                ),
        ),
      ],
    );
  }
}
