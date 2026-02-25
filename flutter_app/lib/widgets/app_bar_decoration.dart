import 'package:flutter/material.dart';
import 'package:alumni_portal/config/theme.dart';

/// Widget that adds MUST colored lines below app bar
class AppBarColoredBorder extends StatelessWidget implements PreferredSizeWidget {
  const AppBarColoredBorder({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 4,
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.accent,      // Blue
            AppColors.accent,      // Blue
            AppColors.secondary,   // Orange
            AppColors.secondary,   // Orange
          ],
          stops: [0.0, 0.5, 0.5, 1.0],
        ),
      ),
    );
  }

  @override
  Size get preferredSize => const Size.fromHeight(4);
}

/// Custom AppBar with MUST colored lines
class MustAppBar extends StatelessWidget implements PreferredSizeWidget {
  final Widget? title;
  final List<Widget>? actions;
  final Widget? leading;
  final bool automaticallyImplyLeading;
  final PreferredSizeWidget? bottom;
  final Color? backgroundColor;
  final double? elevation;
  final bool centerTitle;

  const MustAppBar({
    super.key,
    this.title,
    this.actions,
    this.leading,
    this.automaticallyImplyLeading = true,
    this.bottom,
    this.backgroundColor,
    this.elevation,
    this.centerTitle = false,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        AppBar(
          title: title,
          actions: actions,
          leading: leading,
          automaticallyImplyLeading: automaticallyImplyLeading,
          bottom: bottom,
          backgroundColor: backgroundColor,
          elevation: elevation,
          centerTitle: centerTitle,
        ),
        const AppBarColoredBorder(),
      ],
    );
  }

  @override
  Size get preferredSize {
    final appBarHeight = kToolbarHeight;
    final bottomHeight = bottom?.preferredSize.height ?? 0;
    final borderHeight = 4.0;
    return Size.fromHeight(appBarHeight + bottomHeight + borderHeight);
  }
}
