import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:alumni_portal/models/post_model.dart';
import 'package:alumni_portal/config/theme.dart';

class LinkedInPostCard extends StatelessWidget {
  final PostModel post;
  final VoidCallback onLike;
  final VoidCallback onComment;
  final VoidCallback onRepost;
  final VoidCallback onSend;
  final VoidCallback onProfileTap;
  final bool isLiked;

  const LinkedInPostCard({
    super.key,
    required this.post,
    required this.onLike,
    required this.onComment,
    required this.onRepost,
    required this.onSend,
    required this.onProfileTap,
    this.isLiked = false,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(0),
        side: BorderSide(color: Colors.grey[300]!),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header with user info
          InkWell(
            onTap: onProfileTap,
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Row(
                children: [
                  CircleAvatar(
                    radius: 24,
                    backgroundImage: post.authorImageUrl != null
                        ? CachedNetworkImageProvider(post.authorImageUrl!)
                        : null,
                    child: post.authorImageUrl == null
                        ? Text(
                            post.authorName.substring(0, 1).toUpperCase(),
                            style: const TextStyle(
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                            ),
                          )
                        : null,
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          post.authorName,
                          style: const TextStyle(
                            fontWeight: FontWeight.w600,
                            fontSize: 15,
                          ),
                        ),
                        if (post.authorTitle != null) ...[
                          const SizedBox(height: 2),
                          Text(
                            post.authorTitle!,
                            style: TextStyle(
                              fontSize: 13,
                              color: Colors.grey[700],
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                        const SizedBox(height: 2),
                        Row(
                          children: [
                            Text(
                              timeago.format(post.createdAt),
                              style: TextStyle(
                                fontSize: 12,
                                color: Colors.grey[600],
                              ),
                            ),
                            const SizedBox(width: 4),
                            Icon(Icons.public,
                                size: 12, color: Colors.grey[600]),
                          ],
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.more_horiz),
                    onPressed: () {},
                    iconSize: 20,
                  ),
                ],
              ),
            ),
          ),

          // Post content
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12),
            child: Text(
              post.content,
              style: const TextStyle(fontSize: 14, height: 1.4),
            ),
          ),
          const SizedBox(height: 8),

          // Post image if available
          if (post.imageUrl != null)
            CachedNetworkImage(
              imageUrl: post.imageUrl!,
              width: double.infinity,
              fit: BoxFit.cover,
              placeholder: (_, __) => Container(
                height: 200,
                color: Colors.grey[200],
                child: const Center(child: CircularProgressIndicator()),
              ),
            ),

          // Reaction counts
          if (post.totalReactions > 0 || post.commentCount > 0)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              child: Row(
                children: [
                  if (post.totalReactions > 0) ...[
                    Row(
                      children: [
                        Container(
                          padding: const EdgeInsets.all(2),
                          decoration: const BoxDecoration(
                            color: AppColors.primary,
                            shape: BoxShape.circle,
                          ),
                          child: const Icon(
                            Icons.thumb_up,
                            size: 12,
                            color: Colors.white,
                          ),
                        ),
                        const SizedBox(width: 4),
                        Text(
                          '${post.totalReactions}',
                          style: TextStyle(
                            fontSize: 13,
                            color: Colors.grey[700],
                          ),
                        ),
                      ],
                    ),
                  ],
                  const Spacer(),
                  if (post.commentCount > 0)
                    Text(
                      '${post.commentCount} comments',
                      style: TextStyle(
                        fontSize: 13,
                        color: Colors.grey[700],
                      ),
                    ),
                ],
              ),
            ),

          const Divider(height: 1),

          // Action buttons
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 4),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _buildActionButton(
                  icon: isLiked ? Icons.thumb_up : Icons.thumb_up_outlined,
                  label: 'Like',
                  onTap: onLike,
                  color: isLiked ? AppColors.primary : null,
                ),
                _buildActionButton(
                  icon: Icons.comment_outlined,
                  label: 'Comment',
                  onTap: onComment,
                ),
                _buildActionButton(
                  icon: Icons.repeat,
                  label: 'Repost',
                  onTap: onRepost,
                ),
                _buildActionButton(
                  icon: Icons.send_outlined,
                  label: 'Send',
                  onTap: onSend,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildActionButton({
    required IconData icon,
    required String label,
    required VoidCallback onTap,
    Color? color,
  }) {
    return Expanded(
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 12),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                icon,
                size: 20,
                color: color ?? Colors.grey[700],
              ),
              const SizedBox(width: 4),
              Flexible(
                child: Text(
                  label,
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                    color: color ?? Colors.grey[700],
                  ),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
