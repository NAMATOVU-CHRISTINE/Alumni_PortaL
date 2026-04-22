import 'package:flutter/material.dart';
import 'package:alumni_portal/widgets/flutterwave_payment_dialog.dart';

class PaymentHelper {
  // Show payment dialog for donations
  static void showDonationPayment(
    BuildContext context, {
    required double amount,
    required String purpose,
  }) {
    showDialog(
      context: context,
      builder: (context) => FlutterwavePaymentDialog(
        amount: amount,
        purpose: purpose,
        onComplete: (success, message) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(message),
              backgroundColor: success ? Colors.green : Colors.red,
            ),
          );
        },
      ),
    );
  }

  // Show payment dialog for subscriptions
  static void showSubscriptionPayment(
    BuildContext context, {
    required double amount,
    required String subscriptionType,
  }) {
    showDialog(
      context: context,
      builder: (context) => FlutterwavePaymentDialog(
        amount: amount,
        purpose: 'Convocation Subscription: $subscriptionType',
        onComplete: (success, message) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(success
                  ? 'Subscription activated! $message'
                  : message),
              backgroundColor: success ? Colors.green : Colors.red,
            ),
          );
        },
      ),
    );
  }

  // Show payment dialog for marketplace purchases
  static void showMarketplacePayment(
    BuildContext context, {
    required double amount,
    required String itemName,
  }) {
    showDialog(
      context: context,
      builder: (context) => FlutterwavePaymentDialog(
        amount: amount,
        purpose: 'Purchase: $itemName',
        onComplete: (success, message) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(message),
              backgroundColor: success ? Colors.green : Colors.red,
            ),
          );
        },
      ),
    );
  }
}
        onComplete: (success, message) {
          if (success) {
            // Update user subscription status in Firebase
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text('Subscription activated! $message'),
                backgroundColor: Colors.green,
              ),
            );
          } else {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(message),
                backgroundColor: Colors.red,
              ),
            );
          }
        },
      ),
    );
  }

  // Show payment dialog for marketplace purchases
  static void showMarketplacePayment(
    BuildContext context, {
    required double amount,
    required String itemName,
  }) {
    showDialog(
      context: context,
      builder: (context) => PaymentDialog(
        amount: amount,
        purpose: 'Purchase: $itemName',
        onComplete: (success, message) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(message),
              backgroundColor: success ? Colors.green : Colors.red,
            ),
          );
        },
      ),
    );
  }
}
