import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:alumni_portal/providers/post_provider.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/models/post_model.dart';
import 'package:alumni_portal/config/theme.dart';
import 'package:alumni_portal/widgets/post_card_compact.dart';
import 'package:alumni_portal/widgets/app_bar_decoration.dart';
import 'package:alumni_portal/widgets/empty_state_widget.dart';

class PostSearchDelegate extends SearchDelegate<String> {
  @override
  String get searchFieldLabel => 'Search posts...';

  @override
  List<Widget> buildActions(BuildContext context) {
    return [
      IconButton(
        icon: const Icon(Icons.clear),
        onPressed: () {
          query = '';
        },
      ),
    ];
  }

  @override
  Widget buildLeading(BuildContext context) {
    return IconButton(
      icon: const Icon(Icons.arrow_back),
      onPressed: () {
        close(context, '');
      },
    );
  }

  @override
  Widget buildResults(BuildContext context) {
    return _buildSearchResults(context);
  }

  @override
  Widget buildSuggestions(BuildContext context) {
    return _buildSearchResults(context);
  }

  Widget _buildSearchResults(BuildContext context) {
    if (query.isEmpty) {
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.search, size: 64, color: Colors.grey),
            SizedBox(height: 16),
            Text(
              'Search for posts',
              style: TextStyle(fontSize: 16, color: Colors.grey),
            ),
          ],
        ),
      );
    }

    return Consumer<PostProvider>(
      builder: (context, provider, _) {
        final searchResults = provider.posts.where((post) {
          final searchLower = query.toLowerCase();
          return post.content.toLowerCase().contains(searchLower) ||
              post.authorName.toLowerCase().contains(searchLower);
        }).toList();

        if (searchResults.isEmpty) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.search_off, size: 64, color: Colors.grey[400]),
                const SizedBox(height: 16),
                Text(
                  'No posts found for "$query"',
                  style: const TextStyle(fontSize: 16, color: Colors.grey),
                ),
              ],
            ),
          );
        }

        return ListView.builder(
          padding: const EdgeInsets.symmetric(vertical: 8),
          itemCount: searchResults.length,
          itemBuilder: (context, index) {
            final post = searchResults[index];
            return SharedPostCard(
              post: post,
              onComment: () {
                close(context, '');
                // Navigate to post details or show comments
              },
              onShare: () {
                close(context, '');
                // Handle share
              },
            );
          },
        );
      },
    );
  }
}

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
      appBar: MustAppBar(
        title: const Text('What\'s happening around?'),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () {
              // TODO: Implement search functionality
              showSearch(
                context: context,
                delegate: PostSearchDelegate(),
              );
            },
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
              padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 0),
              itemCount: provider.posts.length,
              itemBuilder: (context, index) {
                final post = provider.posts[index];
                return SharedPostCard(
                  post: post,
                  onComment: () => _showComments(context, post),
                  onShare: () => _sharePost(context, post),
                );
              },
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

  void _showComments(BuildContext context, PostModel post) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (context) => CommentsSheet(post: post),
    );
  }

  void _sharePost(BuildContext context, PostModel post) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (context) => SharePostSheet(post: post),
    );
  }

  Widget _buildEmptyState() {
    return EmptyStates.noPosts(
      onCreatePost: () => context.push('/create-post'),
    );
  }
}

class PostCard extends StatelessWidget {
  final PostModel post;

  const PostCard({super.key, required this.post});

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
              style: const TextStyle(
                fontWeight: FontWeight.w600,
                fontSize: 16,
              ),
            ),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (post.authorTitle != null)
                  Text(
                    post.authorTitle!,
                    style: TextStyle(
                      fontSize: 13,
                      color: Colors.grey[600],
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                const SizedBox(height: 2),
                Row(
                  children: [
                    Text(
                      timeago.format(post.createdAt),
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.grey[500],
                      ),
                    ),
                    const SizedBox(width: 4),
                    Icon(Icons.public, size: 12, color: Colors.grey[500]),
                  ],
                ),
              ],
            ),
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                _buildPostTypeIcon(),
                PopupMenuButton(
                  icon: Icon(Icons.more_horiz, color: Colors.grey[600]),
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
              style: const TextStyle(
                fontSize: 15,
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
                  color: Colors.grey[200],
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
                      style: TextStyle(
                        color: Colors.grey[600],
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                  const Spacer(),
                  if (post.commentCount > 0)
                    Text(
                      '${post.commentCount} comments',
                      style: TextStyle(
                        color: Colors.grey[600],
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                ],
              ),
            ),

          const Divider(height: 24, thickness: 0.5),

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
                    onPressed: () => _showComments(context),
                    icon: const Icon(Icons.comment_outlined, size: 18),
                    label:
                        const Text('Comment', style: TextStyle(fontSize: 12)),
                    style: TextButton.styleFrom(
                      foregroundColor: Colors.grey[600],
                      padding: const EdgeInsets.symmetric(
                          horizontal: 8, vertical: 8),
                    ),
                  ),
                ),
                Expanded(
                  child: TextButton.icon(
                    onPressed: () => _sharePost(context),
                    icon: const Icon(Icons.share_outlined, size: 18),
                    label: const Text('Share', style: TextStyle(fontSize: 12)),
                    style: TextButton.styleFrom(
                      foregroundColor: Colors.grey[600],
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
        border: Border.all(color: Colors.grey[300]!),
        borderRadius: BorderRadius.circular(12),
        color: Colors.grey[50],
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
                style: TextStyle(
                  fontWeight: FontWeight.w600,
                  color: Colors.grey[700],
                  fontSize: 14,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          ...pollData.options.map((option) {
            final percentage = pollData.totalVotes > 0
                ? (option.votes / pollData.totalVotes * 100).round()
                : 0;

            return Container(
              margin: const EdgeInsets.only(bottom: 8),
              child: InkWell(
                onTap: () {
                  // Poll voting functionality - placeholder
                },
                borderRadius: BorderRadius.circular(8),
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    border: Border.all(color: Colors.grey[300]!),
                    borderRadius: BorderRadius.circular(8),
                    color: Colors.white,
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
                              style: const TextStyle(
                                fontSize: 15,
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
                                    backgroundColor: Colors.grey[200],
                                    valueColor: AlwaysStoppedAnimation<Color>(
                                      Colors.purple.withValues(alpha: 0.7),
                                    ),
                                  ),
                                ),
                                const SizedBox(width: 8),
                                Text(
                                  '$percentage%',
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: Colors.grey[600],
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

  void _sharePost(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (context) => SharePostSheet(post: post),
    );
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
      if (!isActive) return Colors.grey[600]!;
      switch (type) {
        case 'likes':
          return Colors.blue;
        default:
          return Colors.grey[600]!;
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

class SharePostSheet extends StatefulWidget {
  final PostModel post;

  const SharePostSheet({super.key, required this.post});

  @override
  State<SharePostSheet> createState() => _SharePostSheetState();
}

class _SharePostSheetState extends State<SharePostSheet> {
  final _commentController = TextEditingController();
  bool _isSharing = false;

  @override
  void dispose() {
    _commentController.dispose();
    super.dispose();
  }

  Future<void> _shareWithComment() async {
    setState(() => _isSharing = true);

    final success = await context.read<PostProvider>().createPost(
          content: _commentController.text.trim().isEmpty
              ? 'Shared a post'
              : _commentController.text.trim(),
          postType: 'shared',
        );

    setState(() => _isSharing = false);

    if (success && mounted) {
      Navigator.pop(context);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Post shared successfully!'),
          backgroundColor: AppColors.success,
        ),
      );
    } else if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Failed to share post'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final user = context.watch<UserProvider>().currentUser;

    return DraggableScrollableSheet(
      initialChildSize: 0.8,
      minChildSize: 0.5,
      maxChildSize: 0.95,
      expand: false,
      builder: (context, scrollController) => Container(
        decoration: BoxDecoration(
          color: Theme.of(context).scaffoldBackgroundColor,
          borderRadius: const BorderRadius.vertical(top: Radius.circular(16)),
        ),
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header
            Row(
              children: [
                Text(
                  'Share Post',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const Spacer(),
                IconButton(
                  icon: const Icon(Icons.close),
                  onPressed: () => Navigator.pop(context),
                ),
              ],
            ),
            Divider(color: Theme.of(context).dividerColor),

            // User info and comment input
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                CircleAvatar(
                  radius: 20,
                  backgroundImage: user?.profileImageUrl != null
                      ? CachedNetworkImageProvider(user!.profileImageUrl!)
                      : null,
                  child: user?.profileImageUrl == null
                      ? Text(
                          user?.fullName?.substring(0, 1).toUpperCase() ?? 'A')
                      : null,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        user?.fullName ?? 'Alumni',
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      const SizedBox(height: 8),
                      TextField(
                        controller: _commentController,
                        maxLines: 3,
                        style: Theme.of(context).textTheme.bodyLarge,
                        decoration: InputDecoration(
                          hintText: 'Add your thoughts about this post...',
                          border: InputBorder.none,
                          hintStyle:
                              Theme.of(context).textTheme.bodyMedium?.copyWith(
                                    color: Theme.of(context).hintColor,
                                  ),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),

            const SizedBox(height: 16),
            Divider(color: Theme.of(context).dividerColor),

            // Original post preview
            Expanded(
              child: SingleChildScrollView(
                controller: scrollController,
                child: Card(
                  child: Padding(
                    padding: const EdgeInsets.all(12),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Original author
                        Row(
                          children: [
                            CircleAvatar(
                              radius: 16,
                              backgroundImage:
                                  widget.post.authorImageUrl != null
                                      ? CachedNetworkImageProvider(
                                          widget.post.authorImageUrl!)
                                      : null,
                              child: widget.post.authorImageUrl == null
                                  ? Text(widget.post.authorName
                                      .substring(0, 1)
                                      .toUpperCase())
                                  : null,
                            ),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    widget.post.authorName,
                                    style:
                                        Theme.of(context).textTheme.titleSmall,
                                  ),
                                  if (widget.post.authorTitle != null)
                                    Text(
                                      widget.post.authorTitle!,
                                      style:
                                          Theme.of(context).textTheme.bodySmall,
                                    ),
                                ],
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 12),

                        // Original content
                        Text(
                          widget.post.content,
                          style: Theme.of(context).textTheme.bodyMedium,
                        ),

                        // Original image
                        if (widget.post.imageUrl != null) ...[
                          const SizedBox(height: 12),
                          ClipRRect(
                            borderRadius: BorderRadius.circular(8),
                            child: CachedNetworkImage(
                              imageUrl: widget.post.imageUrl!,
                              width: double.infinity,
                              height: 150,
                              fit: BoxFit.cover,
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Share options
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () {
                      Navigator.pop(context);
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('Link copied to clipboard!'),
                          backgroundColor: AppColors.success,
                        ),
                      );
                    },
                    icon: const Icon(Icons.link, size: 18),
                    label: const Text('Copy Link'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  flex: 2,
                  child: ElevatedButton(
                    onPressed: _isSharing ? null : _shareWithComment,
                    child: _isSharing
                        ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: Colors.white,
                            ),
                          )
                        : const Text('Share'),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
