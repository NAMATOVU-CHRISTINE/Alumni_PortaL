import 'package:flutter/material.dart';

/// A safe text widget that prevents overflow by automatically handling ellipsis
class SafeText extends StatelessWidget {
  final String text;
  final TextStyle? style;
  final int maxLines;
  final TextAlign textAlign;
  final TextOverflow overflow;

  const SafeText(
    this.text, {
    super.key,
    this.style,
    this.maxLines = 1,
    this.textAlign = TextAlign.start,
    this.overflow = TextOverflow.ellipsis,
  });

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: style,
      maxLines: maxLines,
      textAlign: textAlign,
      overflow: overflow,
      softWrap: true,
    );
  }
}

/// A safe column widget that prevents overflow by using MainAxisSize.min
class SafeColumn extends StatelessWidget {
  final List<Widget> children;
  final CrossAxisAlignment crossAxisAlignment;
  final MainAxisAlignment mainAxisAlignment;
  final MainAxisSize mainAxisSize;

  const SafeColumn({
    super.key,
    required this.children,
    this.crossAxisAlignment = CrossAxisAlignment.center,
    this.mainAxisAlignment = MainAxisAlignment.start,
    this.mainAxisSize = MainAxisSize.min,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: crossAxisAlignment,
      mainAxisAlignment: mainAxisAlignment,
      mainAxisSize: mainAxisSize,
      children: children,
    );
  }
}

/// A safe row widget that prevents overflow by using Flexible widgets
class SafeRow extends StatelessWidget {
  final List<Widget> children;
  final CrossAxisAlignment crossAxisAlignment;
  final MainAxisAlignment mainAxisAlignment;
  final MainAxisSize mainAxisSize;

  const SafeRow({
    super.key,
    required this.children,
    this.crossAxisAlignment = CrossAxisAlignment.center,
    this.mainAxisAlignment = MainAxisAlignment.start,
    this.mainAxisSize = MainAxisSize.max,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: crossAxisAlignment,
      mainAxisAlignment: mainAxisAlignment,
      mainAxisSize: mainAxisSize,
      children: children.map((child) {
        if (child is Text) {
          return Flexible(
            child: SafeText(
              (child).data ?? '',
              style: (child).style,
              maxLines: (child).maxLines ?? 1,
            ),
          );
        }
        return child;
      }).toList(),
    );
  }
}

/// A safe ListTile that prevents subtitle overflow
class SafeListTile extends StatelessWidget {
  final Widget? leading;
  final Widget? title;
  final Widget? subtitle;
  final Widget? trailing;
  final VoidCallback? onTap;
  final EdgeInsetsGeometry? contentPadding;

  const SafeListTile({
    super.key,
    this.leading,
    this.title,
    this.subtitle,
    this.trailing,
    this.onTap,
    this.contentPadding,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: leading,
      title: title,
      subtitle: subtitle != null
          ? IntrinsicHeight(
              child: subtitle is Column
                  ? SafeColumn(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: (subtitle as Column).children,
                    )
                  : subtitle,
            )
          : null,
      trailing: trailing,
      onTap: onTap,
      contentPadding: contentPadding,
      isThreeLine: false,
    );
  }
}

/// A responsive container that adjusts padding based on screen size
class ResponsiveContainer extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry? padding;
  final EdgeInsetsGeometry? margin;

  const ResponsiveContainer({
    super.key,
    required this.child,
    this.padding,
    this.margin,
  });

  @override
  Widget build(BuildContext context) {
    final screenWidth = MediaQuery.of(context).size.width;
    final isTablet = screenWidth > 600;

    final responsivePadding = padding ?? EdgeInsets.all(isTablet ? 24 : 16);
    final responsiveMargin = margin ?? EdgeInsets.all(isTablet ? 12 : 8);

    return Container(
      padding: responsivePadding,
      margin: responsiveMargin,
      child: child,
    );
  }
}

/// A widget that ensures minimum height to prevent layout issues
class MinHeightContainer extends StatelessWidget {
  final Widget child;
  final double minHeight;
  final double? width;

  const MinHeightContainer({
    super.key,
    required this.child,
    this.minHeight = 48.0,
    this.width,
  });

  @override
  Widget build(BuildContext context) {
    return ConstrainedBox(
      constraints: BoxConstraints(
        minHeight: minHeight,
        minWidth: width ?? 0,
      ),
      child: child,
    );
  }
}
