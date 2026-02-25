import 'package:flutter/material.dart';
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
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: Colors.grey.shade200, width: 1),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Author header
          Padding(
            padding: const EdgeInsets.fromLTRB(12, 12, 12, 8),
            child: Row(
              children: [
                CircleAvatar(
                  radius: 20,
                  backgroundImage: post.authorImageUrl != null
                      ? CachedNetworkImageProvider(post.authorImageUrl!)
                      : null,
                  child: post.authorImageUrl == null
                      ? Text(
                          post.authorName.substring(0, 1).toUpperCase(),
                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                        )
                      : null,
                ),
                const SizedBox(width: 8),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      post.authorName,
                      style: const TextStyle(
                        fontWeight: FontWeight.w600,
                        fontSize: 14,
                      ),
                    ),
                    if (post.authorTitle != null)
                      Text(
                        post.authorTitle!,
                        style: TextStyle(
                          fontSize: 12,
                          color: Colors.grey[600],
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    Text(
                      timeago.format(post.createdAt),
                      style: TextStyle(
                        fontSize: 11,
                        color: Colors.grey[500],
                      ),
                    ),
                  ],
                ),
              ),
              PopupMenuButton(
                padding: EdgeInsets.zero,
                icon: Icon(Icons.more_horiz, size: 20, color: Colors.grey[600]),
                itemBuilder: (context) => [
                  if (post.authorId == context.read<PostProvider>().currentUserId)
                    const PopupMenuItem(
                      value: 'delete',
                      child: Row(
                        children: [
                          Icon(Icons.delete, color: Colors.red, size: 18),
                          SizedBox(width: 8),
                          Text('Delete', style: TextStyle(color: Colors.red, fontSize: 14)),
                        ],
                      ),
                    ),
                  const PopupMenuItem(
                    value: 'save',
                    child: Row(
                      children: [
                        Icon(Icons.bookmark_outline, size: 18),
                        SizedBox(width: 8),
                        Text('Save', style: TextStyle(fontSize: 14)),
                      ],
                    ),
                  ),
                ],
                onSelected: (value) => _handleMenuAction(context, value),
              ),
            ],
          ),
        ),

          // Content
          Padding(
            padding: const EdgeInsets.fromLTRB(12, 0, 12, 8),
            child: Text(
              post.content,
              style: const TextStyle(fontSize: 14, height: 1.4),
            ),
          ),

          // Poll widget
          if (post.postType == 'poll' && post.pollData != null)
            Padding(
              padding: const EdgeInsets.fromLTRB(12, 0, 12, 8),
              child: _buildPollWidget(context, post.pollData!),
            ),

          // Image
          if (post.imageUrl != null)
            CachedNetworkImage(
              imageUrl: post.imageUrl!,
              width: double.infinity,
              fit: BoxFit.fitWidth,
              placeholder: (_, __) => Container(
                height: 200,
                color: Colors.grey[200],
                child: const Center(child: CircularProgressIndicator()),
              ),
            ),

          // Reaction counts
          if (post.totalReactions > 0 || post.commentCount > 0)
            Padding(
              padding: const EdgeInsets.fromLTRB(12, 8, 12, 8),
              child: Row(
                children: [
                  if (post.totalReactions > 0) ...[
                    Container(
                      padding: const EdgeInsets.all(2),
                      decoration: const BoxDecoration(
                        color: Colors.blue,
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(Icons.thumb_up, size: 12, color: Colors.white),
                    ),
                    const SizedBox(width: 4),
                    Text(
                      '${post.totalReactions}',
                      style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                    ),
                  ],
                  const Spacer(),
                  if (post.commentCount > 0)
                    Text(
                      '${post.commentCount} comments',
                      style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                    ),
                ],
              ),
            ),

          Divider(height: 1, thickness: 1, color: Colors.grey.shade200),

          // Action buttons
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 4),
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
                    label: const Text('Comment', style: TextStyle(fontSize: 13)),
                    style: TextButton.styleFrom(
                      foregroundColor: Colors.grey[700],
                      padding: const EdgeInsets.symmetric(vertical: 8),
                    ),
                  ),
                ),
                Expanded(
                  child: TextButton.icon(
                    onPressed: onShare,
                    icon: const Icon(Icons.share_outlined, size: 18),
                    label: const Text('Share', style: TextStyle(fontSize: 13)),
                    style: TextButton.styleFrom(
                      foregroundColor: Colors.grey[700],
                      padding: const EdgeInsets.symmetric(vertical: 8),
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

  Widget _buildPollWidget(BuildContext context, PollData pollData) {
    return Consumer<PostProvider>(
      builder: (context, provider, _) {
        final hasVoted = provider.hasUserVotedOnPoll(post.id);
        
        // Get the latest post data
        final latestPost = provider.posts.firstWhere(
          (p) => p.id == post.id,
          orElse: () => post,
        );
        final latestPollData = latestPost.pollData ?? pollData;
        
        return Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            border: Border.all(color: Colors.grey.shade300),
            borderRadius: BorderRadius.circular(8),
            color: Colors.grey[50],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  const Icon(Icons.poll, color: Colors.purple, size: 16),
                  const SizedBox(width: 6),
                  Text(
                    'Poll • ${latestPollData.totalVotes} votes',
                    style: const TextStyle(
                      fontWeight: FontWeight.w600,
                      fontSize: 12,
                      color: Colors.grey,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              ...latestPollData.options.asMap().entries.map((entry) {
                final index = entry.key;
                final option = entry.value;
                final percentage = latestPollData.totalVotes > 0
                    ? (option.votes / latestPollData.totalVotes * 100).round()
                    : 0;
                final userVoted = option.voters.contains(provider.currentUserId);

                return Container(
                  margin: const EdgeInsets.only(bottom: 8),
                  child: InkWell(
                    onTap: hasVoted ? null : () async {
                      final success = await provider.voteOnPoll(post.id, index);
                      if (!success && context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text(provider.error ?? 'Failed to vote'),
                            backgroundColor: Colors.red,
                            duration: const Duration(seconds: 2),
                          ),
                        );
                      }
                    },
                    borderRadius: BorderRadius.circular(6),
                    child: Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        border: Border.all(
                          color: userVoted ? Colors.purple : Colors.grey.shade300,
                          width: userVoted ? 2 : 1,
                        ),
                        borderRadius: BorderRadius.circular(6),
                        color: userVoted ? Colors.purple.shade50 : Colors.white,
                      ),
                      child: Row(
                        children: [
                          Icon(
                            userVoted ? Icons.check_circle : Icons.radio_button_unchecked,
                            color: Colors.purple,
                            size: 18,
                          ),
                          const SizedBox(width: 10),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  option.text,
                                  style: TextStyle(
                                    fontSize: 13,
                                    fontWeight: userVoted ? FontWeight.w600 : FontWeight.w500,
                                  ),
                                ),
                                if (hasVoted) ...[
                                  const SizedBox(height: 6),
                                  Row(
                                    children: [
                                      Expanded(
                                        child: LinearProgressIndicator(
                                          value: latestPollData.totalVotes > 0
                                              ? option.votes / latestPollData.totalVotes
                                              : 0,
                                          backgroundColor: Colors.grey[200],
                                          valueColor: const AlwaysStoppedAnimation<Color>(
                                            Colors.purple,
                                          ),
                                          minHeight: 4,
                                        ),
                                      ),
                                      const SizedBox(width: 8),
                                      Text(
                                        '$percentage%',
                                        style: const TextStyle(
                                          fontSize: 11,
                                          color: Colors.grey,
                                          fontWeight: FontWeight.w600,
                                        ),
                                      ),
                                    ],
                                  ),
                                ],
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
      },
    );
  }

  void _handleMenuAction(BuildContext context, String action) {
    switch (action) {
      case 'delete':
        _showDeleteConfirmation(context);
        break;
      case 'save':
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: const Text('Post saved!'),
            backgroundColor: AppColors.success,
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
        content: const Text('Are you sure you want to delete this post?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () async {
              Navigator.pop(context);
              final success = await context.read<PostProvider>().deletePost(post.id);
              if (success && context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: const Text('Post deleted'),
                    backgroundColor: AppColors.success,
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

    return TextButton.icon(
      onPressed: () => provider.toggleReaction(post.id, type),
      icon: Icon(
        isActive ? activeIcon : icon,
        size: 18,
        color: isActive ? Colors.blue : Colors.grey[700],
      ),
      label: Text(
        label,
        style: TextStyle(
          color: isActive ? Colors.blue : Colors.grey[700],
          fontSize: 13,
        ),
      ),
      style: TextButton.styleFrom(
        padding: const EdgeInsets.symmetric(vertical: 8),
      ),
    );
  }
}
