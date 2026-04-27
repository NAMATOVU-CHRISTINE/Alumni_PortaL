import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:alumni_portal/config/theme.dart';

class CustomAppBar extends StatelessWidget implements PreferredSizeWidget {
  final String title;
  final List<Widget>? actions;
  final bool showBackButton;
  final PreferredSizeWidget? bottom;
  final Color? backgroundColor;
  final Color? foregroundColor;
  final bool showColorLine;

  const CustomAppBar({
    super.key,
    required this.title,
    this.actions,
    this.showBackButton = true,
    this.bottom,
    this.backgroundColor,
    this.foregroundColor,
    this.showColorLine = true,
  });

  @override
  Widget build(BuildContext context) {
    final canPop = context.canPop();

    return AppBar(
      title: Text(title),
      backgroundColor: backgroundColor ?? AppColors.primary,
      foregroundColor: foregroundColor ?? Colors.white,
      elevation: 0,
      leading: showBackButton
          ? IconButton(
              icon: const Icon(Icons.arrow_back),
              onPressed: () {
                if (canPop) {
                  context.pop();
                } else {
                  context.go('/home');
                }
              },
              tooltip: canPop ? 'Back' : 'Home',
            )
          : null,
      actions: actions,
      bottom: bottom != null
          ? PreferredSize(
              preferredSize: Size.fromHeight(
                bottom!.preferredSize.height + (showColorLine ? 3 : 0),
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  bottom!,
                  if (showColorLine)
                    Container(
                      height: 3,
                      child: Row(
                        children: [
                          Expanded(child: Container(color: AppColors.primary)),
                          Expanded(child: Container(color: AppColors.accent)),
                          Expanded(child: Container(color: AppColors.secondary)),
                        ],
                      ),
                    ),
                ],
              ),
            )
          : showColorLine
              ? PreferredSize(
                  preferredSize: const Size.fromHeight(3),
                  child: Container(
                    height: 3,
                    child: Row(
                      children: [
                        Expanded(child: Container(color: AppColors.primary)),
                        Expanded(child: Container(color: AppColors.accent)),
                        Expanded(child: Container(color: AppColors.secondary)),
                      ],
                    ),
                  ),
                )
              : null,
    );
  }

  @override
  Size get preferredSize => Size.fromHeight(
        kToolbarHeight +
            (bottom?.preferredSize.height ?? 0.0) +
            (showColorLine ? 3 : 0),
      );
}
