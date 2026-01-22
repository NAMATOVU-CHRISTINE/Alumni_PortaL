import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:alumni_portal/providers/post_provider.dart';
import 'package:alumni_portal/models/post_model.dart';
import 'package:alumni_portal/config/theme.dart';

class SharedPostCard extends StatelessWidget {
  final PostModel post;
  final VoidCallback? onShare;
  final VoidCallback? onComment;

  const SharedPostCard({
    super.key,
    required this.post,
    this.onShare,
    this.onComment,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Author header
          ListTile(
            contentPadding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
            leading: CircleAvatar(
              radius: 22,
              backgroundImage: post.authorImageUrl != null
                  ? CachedNetworkImageProvider(post.authorImageUrl!)
                  : null,
              child: post.authorImageUrl == null
                  ? Text(
                      post.authorName.substring(0, 1).toUpperCase(),
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 18,
                      ),
                    )
                  : null,
            ),
            title: Text(
              post.authorName,
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
            ),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (post.authorTitle != null)
                  Text(
                    post.authorTitle!,
                    style: Theme.of(context).textTheme.bodySmall,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                const SizedBox(height: 2),
                Row(
                  children: [
                    Text(
                      timeago.format(post.createdAt),
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                    const SizedBox(width: 4),
                    Icon(
                      Icons.public,
                      size: 12,
                      color: Theme.of(context).textTheme.bodySmall?.color,
                    ),
                  ],
                ),
              ],
            ),
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                _buildPostTypeIcon(context),
                PopupMenuButton(
                  icon: Icon(
                    Icons.more_horiz,
                    color: Theme.of(context).iconTheme.color,
                  ),
                  itemBuilder: (context) => [
                    if (post.authorId ==
                        context.read<PostProvider>().currentUserId)
                      const PopupMenuItem(
                        value: 'delete',
                        child: Row(
                          children: [
                            Icon(Icons.delete, color: Colors.red),
                            SizedBox(width: 8),
                            Text('Delete Post',
                                style: TextStyle(color: Colors.red)),
                          ],
                        ),
                      ),
                    const PopupMenuItem(
                      value: 'save',
                      child: Row(
                        children: [
                          Icon(Icons.bookmark_outline),
                          SizedBox(width: 8),
                          Text('Save post'),
                        ],
                      ),
                    ),
                    const PopupMenuItem(
                      value: 'report',
                      child: Row(
                        children: [
                          Icon(Icons.report),
                          SizedBox(width: 8),
                          Text('Report'),
                        ],
                      ),
                    ),
                  ],
                  onSelected: (value) => _handleMenuAction(context, value),
                ),
              ],
            ),
            onTap: () => context.push('/view-profile/${post.authorId}'),
          ),

          // Content
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
            child: Text(
              post.content,
              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    height: 1.4,
                  ),
            ),
          ),

          // Poll widget
          if (post.postType == 'poll' && post.pollData != null)
            _buildPollWidget(context, post.pollData!),

          // Image
          if (post.imageUrl != null) ...[
            const SizedBox(height: 8),
            ClipRRect(
              borderRadius: const BorderRadius.all(Radius.circular(8)),
              child: CachedNetworkImage(
                imageUrl: post.imageUrl!,
                width: double.infinity,
                fit: BoxFit.cover,
                placeholder: (_, __) => Container(
                  height: 200,
                  color: Theme.of(context).cardColor,
                  child: const Center(child: CircularProgressIndicator()),
                ),
              ),
            ),
          ],

          // Reaction counts
          if (post.totalReactions > 0 || post.commentCount > 0)
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
              child: Row(
                children: [
                  if (post.totalReactions > 0) ...[
                    _buildReactionIcons(),
                    const SizedBox(width: 6),
                    Text(
                      '${post.totalReactions}',
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            fontWeight: FontWeight.w500,
                          ),
                    ),
                  ],
                  const Spacer(),
                  if (post.commentCount > 0)
                    Text(
                      '${post.commentCount} comments',
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            fontWeight: FontWeight.w500,
                          ),
                    ),
                ],
              ),
            ),

          Divider(
            height: 24,
            thickness: 0.5,
            color: Theme.of(context).dividerColor,
          ),

          // Action buttons
          Padding(
            padding: const EdgeInsets.fromLTRB(8, 0, 8, 12),
            child: Row(
              children: [
                Expanded(
                  child: _ReactionButton(
                    post: post,
                    type: 'likes',
                    icon: Icons.thumb_up_outlined,
                    activeIcon: Icons.thumb_up,
                    label: 'Like',
                  ),
                ),
                Expanded(
                  child: TextButton.icon(
                    onPressed: onComment,
                    icon: const Icon(Icons.comment_outlined, size: 18),
                    label:
                        const Text('Comment', style: TextStyle(fontSize: 12)),
                    style: TextButton.styleFrom(
                      foregroundColor:
                          Theme.of(context).textTheme.bodyMedium?.color,
                      padding: const EdgeInsets.symmetric(
                          horizontal: 8, vertical: 8),
                    ),
                  ),
                ),
                Expanded(
                  child: TextButton.icon(
                    onPressed: onShare,
                    icon: const Icon(Icons.share_outlined, size: 18),
                    label: const Text('Share', style: TextStyle(fontSize: 12)),
                    style: TextButton.styleFrom(
                      foregroundColor:
                          Theme.of(context).textTheme.bodyMedium?.color,
                      padding: const EdgeInsets.symmetric(
                          horizontal: 8, vertical: 8),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPostTypeIcon(BuildContext context) {
    IconData icon;
    Color color;
    switch (post.postType) {
      case 'achievement':
        icon = Icons.emoji_events;
        color = Colors.amber;
        break;
      case 'job_update':
        icon = Icons.work;
        color = Colors.blue;
        break;
      case 'article':
        icon = Icons.article;
        color = Colors.green;
        break;
      case 'poll':
        icon = Icons.poll;
        color = Colors.purple;
        break;
      default:
        return const SizedBox.shrink();
    }
    return Container(
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Icon(icon, color: color, size: 16),
    );
  }

  Widget _buildPollWidget(BuildContext context, PollData pollData) {
    return Container(
      margin: const EdgeInsets.fromLTRB(16, 0, 16, 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        border: Border.all(color: Theme.of(context).dividerColor),
        borderRadius: BorderRadius.circular(12),
        color: Theme.of(context).cardColor,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.poll, color: Colors.purple, size: 20),
              const SizedBox(width: 8),
              Text(
                'Poll • ${pollData.totalVotes} votes',
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          ...pollData.options.asMap().entries.map((entry) {
            final index = entry.key;
            final option = entry.value;
            final percentage = pollData.totalVotes > 0
                ? (option.votes / pollData.totalVotes * 100).round()
                : 0;

            return Container(
              margin: const EdgeInsets.only(bottom: 8),
              child: InkWell(
                onTap: () {
                  // Poll voting functionality
                },
                borderRadius: BorderRadius.circular(8),
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    border: Border.all(color: Theme.of(context).dividerColor),
                    borderRadius: BorderRadius.circular(8),
                    color: Theme.of(context).scaffoldBackgroundColor,
                  ),
                  child: Row(
                    children: [
                      Icon(
                        Icons.radio_button_unchecked,
                        color: Colors.purple,
                        size: 20,
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              option.text,
                              style: Theme.of(context)
                                  .textTheme
                                  .bodyMedium
                                  ?.copyWith(
                                    fontWeight: FontWeight.w500,
                                  ),
                            ),
                            const SizedBox(height: 4),
                            Row(
                              children: [
                                Expanded(
                                  child: LinearProgressIndicator(
                                    value: pollData.totalVotes > 0
                                        ? option.votes / pollData.totalVotes
                                        : 0,
                                    backgroundColor:
                                        Theme.of(context).dividerColor,
                                    valueColor: AlwaysStoppedAnimation<Color>(
                                      Colors.purple.withValues(alpha: 0.7),
                                    ),
                                  ),
                                ),
                                const SizedBox(width: 8),
                                Text(
                                  '$percentage%',
                                  style: Theme.of(context)
                                      .textTheme
                                      .bodySmall
                                      ?.copyWith(
                                        fontWeight: FontWeight.w500,
                                      ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          }),
        ],
      ),
    );
  }

  Widget _buildReactionIcons() {
    final icons = <Widget>[];
    if (post.likes.isNotEmpty) {
      icons.add(const Icon(Icons.thumb_up, size: 14, color: Colors.blue));
    }
    return Row(mainAxisSize: MainAxisSize.min, children: icons);
  }

  void _handleMenuAction(BuildContext context, String action) {
    switch (action) {
      case 'delete':
        _showDeleteConfirmation(context);
        break;
      case 'save':
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Post saved!'),
            backgroundColor: AppColors.success,
          ),
        );
        break;
      case 'report':
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Post reported. Thank you for your feedback.'),
          ),
        );
        break;
    }
  }

  void _showDeleteConfirmation(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Post'),
        content: const Text(
            'Are you sure you want to delete this post? This action cannot be undone.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () async {
              Navigator.pop(context);
              final success =
                  await context.read<PostProvider>().deletePost(post.id);
              if (success && context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Post deleted successfully'),
                    backgroundColor: AppColors.success,
                  ),
                );
              } else if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Failed to delete post'),
                    backgroundColor: Colors.red,
                  ),
                );
              }
            },
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }
}

class _ReactionButton extends StatelessWidget {
  final PostModel post;
  final String type;
  final IconData icon;
  final IconData activeIcon;
  final String label;

  const _ReactionButton({
    required this.post,
    required this.type,
    required this.icon,
    required this.activeIcon,
    required this.label,
  });

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<PostProvider>();
    final isActive = provider.hasUserReacted(post, type);

    Color getColor() {
      if (!isActive) {
        return Theme.of(context).textTheme.bodyMedium?.color ??
            Colors.grey[600]!;
      }
      switch (type) {
        case 'likes':
          return Colors.blue;
        default:
          return Theme.of(context).textTheme.bodyMedium?.color ??
              Colors.grey[600]!;
      }
    }

    return TextButton.icon(
      onPressed: () => provider.toggleReaction(post.id, type),
      icon: Icon(isActive ? activeIcon : icon, size: 18, color: getColor()),
      label: Text(label, style: TextStyle(color: getColor(), fontSize: 12)),
      style: TextButton.styleFrom(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
      ),
    );
  }
}
