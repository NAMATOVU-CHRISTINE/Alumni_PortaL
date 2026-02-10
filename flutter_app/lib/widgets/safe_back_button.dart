import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

/// A safe back button that handles navigation properly
/// Falls back to home if there's no previous route
class SafeBackButton extends StatelessWidget {
  final Color? color;
  final String fallbackRoute;

  const SafeBackButton({
    super.key,
    this.color,
    this.fallbackRoute = '/home',
  });

  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: const Icon(Icons.arrow_back),
      color: color,
      onPressed: () {
        if (context.canPop()) {
          context.pop();
        } else {
          context.go(fallbackRoute);
        }
      },
    );
  }
}
