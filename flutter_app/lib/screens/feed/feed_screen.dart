import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:alumni_portal/providers/post_provider.dart';
import 'package:alumni_portal/models/post_model.dart';
import 'package:alumni_portal/config/theme.dart';

class FeedScreen extends StatefulWidget {
  const FeedScreen({super.key});

  @override
  State<FeedScreen> createState() => _FeedScreenState();
}

class _FeedScreenState extends State<FeedScreen> {
  @override
  void initState() {
    super.initState();
    _loadFeed();
  }

  void _loadFeed() {
    context.read<PostProvider>().loadPosts();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Feed'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadFeed,
          ),
        ],
      ),
      body: Consumer<PostProvider>(
        builder: (context, provider, _) {
          if (provider.isLoading && provider.posts.isEmpty) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.posts.isEmpty) {
            return _buildEmptyState();
          }

          return RefreshIndicator(
            onRefresh: () async => _loadFeed(),
            child: ListView.builder(
              padding: const EdgeInsets.only(top: 8),
              itemCount: provider.posts.length,
              itemBuilder: (context, index) =>
                  PostCard(post: provider.posts[index]),
            ),
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.push('/create-post'),
        child: const Icon(Icons.edit),
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.dynamic_feed, size: 80, color: Colors.grey[300]),
          const SizedBox(height: 16),
          const Text(
            'No posts yet',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
          ),
          const SizedBox(height: 8),
          const Text(
            'Be the first to share something!',
            style: TextStyle(color: AppColors.textSecondary),
          ),
          const SizedBox(height: 24),
          ElevatedButton.icon(
            onPressed: () => context.push('/create-post'),
            icon: const Icon(Icons.add),
            label: const Text('Create Post'),
          ),
        ],
      ),
    );
  }
}

class PostCard extends StatelessWidget {
  final PostModel post;

  const PostCard({super.key, required this.post});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Author header
          ListTile(
            leading: CircleAvatar(
              backgroundImage: post.authorImageUrl != null
                  ? CachedNetworkImageProvider(post.authorImageUrl!)
                  : null,
              child: post.authorImageUrl == null
                  ? Text(post.authorName.substring(0, 1).toUpperCase())
                  : null,
            ),
            title: Text(
              post.authorName,
              style: const TextStyle(fontWeight: FontWeight.w600),
            ),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (post.authorTitle != null)
                  Text(
                    post.authorTitle!,
                    style: const TextStyle(fontSize: 12),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                Text(
                  timeago.format(post.createdAt),
                  style: TextStyle(fontSize: 11, color: Colors.grey[600]),
                ),
              ],
            ),
            trailing: _buildPostTypeIcon(),
            onTap: () => context.push('/view-profile/${post.authorId}'),
          ),

          // Content
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Text(post.content),
          ),

          // Image
          if (post.imageUrl != null) ...[
            const SizedBox(height: 12),
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
          ],

          // Reaction counts
          if (post.totalReactions > 0 || post.commentCount > 0)
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
              child: Row(
                children: [
                  if (post.totalReactions > 0) ...[
                    _buildReactionIcons(),
                    const SizedBox(width: 4),
                    Text(
                      '${post.totalReactions}',
                      style: TextStyle(color: Colors.grey[600], fontSize: 13),
                    ),
                  ],
                  const Spacer(),
                  if (post.commentCount > 0)
                    Text(
                      '${post.commentCount} comments',
                      style: TextStyle(color: Colors.grey[600], fontSize: 13),
                    ),
                ],
              ),
            ),

          const Divider(height: 24),

          // Action buttons
          Padding(
            padding: const EdgeInsets.only(bottom: 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _ReactionButton(
                  post: post,
                  type: 'likes',
                  icon: Icons.thumb_up_outlined,
                  activeIcon: Icons.thumb_up,
                  label: 'Like',
                ),
                _ReactionButton(
                  post: post,
                  type: 'celebrates',
                  icon: Icons.celebration_outlined,
                  activeIcon: Icons.celebration,
                  label: 'Celebrate',
                ),
                _ReactionButton(
                  post: post,
                  type: 'supports',
                  icon: Icons.favorite_outline,
                  activeIcon: Icons.favorite,
                  label: 'Support',
                ),
                TextButton.icon(
                  onPressed: () => _showComments(context),
                  icon: const Icon(Icons.comment_outlined, size: 20),
                  label: const Text('Comment'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPostTypeIcon() {
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
      default:
        return const SizedBox.shrink();
    }
    return Icon(icon, color: color, size: 20);
  }

  Widget _buildReactionIcons() {
    final icons = <Widget>[];
    if (post.likes.isNotEmpty) {
      icons.add(const Icon(Icons.thumb_up, size: 14, color: Colors.blue));
    }
    if (post.celebrates.isNotEmpty) {
      icons.add(const Icon(Icons.celebration, size: 14, color: Colors.amber));
    }
    if (post.supports.isNotEmpty) {
      icons.add(const Icon(Icons.favorite, size: 14, color: Colors.red));
    }
    return Row(mainAxisSize: MainAxisSize.min, children: icons);
  }

  void _showComments(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (context) => CommentsSheet(post: post),
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
      if (!isActive) return Colors.grey[600]!;
      switch (type) {
        case 'likes':
          return Colors.blue;
        case 'celebrates':
          return Colors.amber[700]!;
        case 'supports':
          return Colors.red;
        default:
          return Colors.grey[600]!;
      }
    }

    return TextButton.icon(
      onPressed: () => provider.toggleReaction(post.id, type),
      icon: Icon(isActive ? activeIcon : icon, size: 20, color: getColor()),
      label: Text(label, style: TextStyle(color: getColor())),
    );
  }
}

class CommentsSheet extends StatefulWidget {
  final PostModel post;

  const CommentsSheet({super.key, required this.post});

  @override
  State<CommentsSheet> createState() => _CommentsSheetState();
}

class _CommentsSheetState extends State<CommentsSheet> {
  final _commentController = TextEditingController();
  bool _isSubmitting = false;

  @override
  void initState() {
    super.initState();
    context.read<PostProvider>().loadComments(widget.post.id);
  }

  @override
  void dispose() {
    _commentController.dispose();
    super.dispose();
  }

  Future<void> _submitComment() async {
    if (_commentController.text.trim().isEmpty) return;

    setState(() => _isSubmitting = true);

    final success = await context.read<PostProvider>().addComment(
          widget.post.id,
          _commentController.text.trim(),
        );

    if (success) {
      _commentController.clear();
    }

    setState(() => _isSubmitting = false);
  }

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      initialChildSize: 0.7,
      minChildSize: 0.5,
      maxChildSize: 0.95,
      expand: false,
      builder: (context, scrollController) => Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                const Text(
                  'Comments',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                const Spacer(),
                IconButton(
                  icon: const Icon(Icons.close),
                  onPressed: () => Navigator.pop(context),
                ),
              ],
            ),
          ),
          const Divider(height: 1),
          Expanded(
            child: Consumer<PostProvider>(
              builder: (context, provider, _) {
                if (provider.comments.isEmpty) {
                  return const Center(
                    child: Text('No comments yet. Be the first!'),
                  );
                }
                return ListView.builder(
                  controller: scrollController,
                  padding: const EdgeInsets.all(16),
                  itemCount: provider.comments.length,
                  itemBuilder: (context, index) {
                    final comment = provider.comments[index];
                    return _buildCommentTile(comment);
                  },
                );
              },
            ),
          ),
          const Divider(height: 1),
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(8),
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _commentController,
                      decoration: InputDecoration(
                        hintText: 'Write a comment...',
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(24),
                        ),
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 16,
                          vertical: 8,
                        ),
                      ),
                      maxLines: null,
                    ),
                  ),
                  const SizedBox(width: 8),
                  IconButton(
                    onPressed: _isSubmitting ? null : _submitComment,
                    icon: _isSubmitting
                        ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.send, color: AppColors.primary),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCommentTile(CommentModel comment) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          CircleAvatar(
            radius: 16,
            backgroundImage: comment.authorImageUrl != null
                ? CachedNetworkImageProvider(comment.authorImageUrl!)
                : null,
            child: comment.authorImageUrl == null
                ? Text(comment.authorName.substring(0, 1).toUpperCase())
                : null,
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.grey[100],
                borderRadius: BorderRadius.circular(12),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    comment.authorName,
                    style: const TextStyle(
                        fontWeight: FontWeight.w600, fontSize: 13),
                  ),
                  const SizedBox(height: 4),
                  Text(comment.content),
                  const SizedBox(height: 4),
                  Text(
                    timeago.format(comment.createdAt),
                    style: TextStyle(fontSize: 11, color: Colors.grey[600]),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
