import 'package:flutter/material.dart';
import 'package:alumni_portal/services/pandora_payment_service.dart';
import 'package:alumni_portal/config/theme.dart';

class PandoraPaymentDialog extends StatefulWidget {
  final double amount;
  final String purpose;
  final Function(bool success, String message) onComplete;

  const PandoraPaymentDialog({
    Key? key,
    required this.amount,
    required this.purpose,
    required this.onComplete,
  }) : super(key: key);

  @override
  State<PandoraPaymentDialog> createState() => _PandoraPaymentDialogState();
}

class _PandoraPaymentDialogState extends State<PandoraPaymentDialog> {
  late final TextEditingController _phoneController;
  bool _isProcessing = false;
  String _selectedProvider = 'MTN';

  @override
  void initState() {
    super.initState();
    // Create a fresh controller each time
    _phoneController = TextEditingController(text: '');
    debugPrint('🆕 Payment dialog initialized with empty phone field');
  }

  @override
  void dispose() {
    _phoneController.dispose();
    super.dispose();
  }

  Future<void> _processPayment() async {
    if (_phoneController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Please enter your phone number'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    // Validate phone number length
    final phone = _phoneController.text.replaceAll(RegExp(r'[^\d]'), '');
    if (phone.length < 9) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Please enter a valid phone number'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    // Detect provider from phone number
    String detectedProvider = 'Unknown';
    if (phone.startsWith('078') || phone.startsWith('077') || phone.startsWith('076') || 
        phone.startsWith('25678') || phone.startsWith('25677') || phone.startsWith('25676')) {
      detectedProvider = 'MTN';
    } else if (phone.startsWith('075') || phone.startsWith('070') || 
               phone.startsWith('25675') || phone.startsWith('25670')) {
      detectedProvider = 'Airtel';
    }

    // Warn if selected provider doesn't match phone number
    if (detectedProvider != 'Unknown' && detectedProvider != _selectedProvider) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Warning: This looks like a $detectedProvider number but you selected $_selectedProvider'),
          backgroundColor: Colors.orange,
          duration: const Duration(seconds: 3),
        ),
      );
      await Future.delayed(const Duration(seconds: 1));
    }

    setState(() => _isProcessing = true);

    try {
      final reference = PandoraPaymentService.generateReference('convocation');
      final phoneToUse = _phoneController.text;
      
      debugPrint('🎯 DIALOG: Using phone number from input: $phoneToUse');
      debugPrint('🎯 DIALOG: Selected provider: $_selectedProvider');
      debugPrint('🎯 DIALOG: Detected provider: $detectedProvider');

      final result = await PandoraPaymentService.initiateMobileMoney(
        phoneNumber: phoneToUse,
        amount: widget.amount,
        reference: reference,
        narrative: widget.purpose,
        callbackUrl: 'https://convocation.app/api/payments/callback',
      );

      if (!mounted) return;
      
      // Only show simple message
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: const Text('Check your phone for payment prompt'),
          backgroundColor: Colors.blue,
          duration: const Duration(seconds: 3),
        ),
      );
      
      Navigator.pop(context);

      widget.onComplete(
        result['success'],
        'Check your phone for payment prompt',
      );
    } catch (e) {
      if (!mounted) return;
      
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Payment failed: $e'),
          backgroundColor: Colors.red,
          duration: const Duration(seconds: 4),
        ),
      );
      
      widget.onComplete(false, 'Payment failed: $e');
    } finally {
      if (mounted) setState(() => _isProcessing = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      title: Row(
        children: [
          Icon(Icons.payment, color: AppColors.primary),
          const SizedBox(width: 8),
          const Text(
            'Mobile Money Payment',
            style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
          ),
        ],
      ),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Amount display
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppColors.primary.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(8),
                border: Border.all(
                  color: AppColors.primary.withValues(alpha: 0.3),
                ),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    widget.purpose,
                    style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'UGX ${widget.amount.toStringAsFixed(0).replaceAllMapped(RegExp(r'(\d{1,3})(?=(\d{3})+(?!\d))'), (m) => '${m[1]},')}',
                    style: const TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                      color: AppColors.primary,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Provider selection
            const Text(
              'Select Provider',
              style: TextStyle(fontWeight: FontWeight.w600, fontSize: 13),
            ),
            const SizedBox(height: 8),
            // Provider selection buttons
            Row(
              children: [
                // MTN
                Expanded(
                  child: GestureDetector(
                    onTap: () => setState(() => _selectedProvider = 'MTN'),
                    child: Container(
                      margin: const EdgeInsets.only(right: 8),
                      padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 8),
                      decoration: BoxDecoration(
                        color: _selectedProvider == 'MTN'
                            ? const Color(0xFFFFCB05)
                            : Colors.grey[200],
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(
                          color: _selectedProvider == 'MTN'
                              ? const Color(0xFFFFCB05)
                              : Colors.grey[300]!,
                          width: 2,
                        ),
                      ),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Container(
                            width: 28,
                            height: 28,
                            padding: const EdgeInsets.all(4),
                            decoration: BoxDecoration(
                              color: Colors.white,
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Image.asset(
                              'assets/icons/mtn_logo.png',
                              fit: BoxFit.contain,
                            ),
                          ),
                          const SizedBox(width: 8),
                          Text(
                            'MTN',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 16,
                              color: _selectedProvider == 'MTN'
                                  ? Colors.black
                                  : Colors.black54,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
                // Airtel
                Expanded(
                  child: GestureDetector(
                    onTap: () => setState(() => _selectedProvider = 'Airtel'),
                    child: Container(
                      padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 8),
                      decoration: BoxDecoration(
                        color: _selectedProvider == 'Airtel'
                            ? Colors.red
                            : Colors.grey[200],
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(
                          color: _selectedProvider == 'Airtel'
                              ? Colors.red
                              : Colors.grey[300]!,
                          width: 2,
                        ),
                      ),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Container(
                            width: 28,
                            height: 28,
                            padding: const EdgeInsets.all(4),
                            decoration: BoxDecoration(
                              color: Colors.white,
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Image.asset(
                              'assets/icons/airtel_logo.png',
                              fit: BoxFit.contain,
                            ),
                          ),
                          const SizedBox(width: 8),
                          Text(
                            'Airtel',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 16,
                              color: _selectedProvider == 'Airtel'
                                  ? Colors.white
                                  : Colors.black54,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Phone number input
            const Text(
              'Phone Number',
              style: TextStyle(fontWeight: FontWeight.w600, fontSize: 13),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _phoneController,
              decoration: InputDecoration(
                hintText: '0700 123 456',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
                prefixIcon: const Icon(Icons.phone),
                suffixIcon: IconButton(
                  icon: const Icon(Icons.clear),
                  onPressed: () {
                    _phoneController.clear();
                    setState(() {});
                  },
                ),
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 12,
                ),
              ),
              keyboardType: TextInputType.phone,
              onChanged: (value) {
                setState(() {}); // Rebuild to show/hide clear button
              },
            ),
            const SizedBox(height: 8),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.blue.shade50,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.blue.shade200),
              ),
              child: Row(
                children: [
                  Icon(Icons.info_outline, color: Colors.blue.shade700, size: 20),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'A payment prompt will be sent to this number. Enter your PIN to complete the payment.',
                      style: TextStyle(
                        fontSize: 11,
                        color: Colors.blue.shade900,
                      ),
                    ),
                  ),
                ],
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
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
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
                  style: TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                  ),
                ),
        ),
      ],
    );
  }
}
