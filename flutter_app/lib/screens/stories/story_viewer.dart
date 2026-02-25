import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:provider/provider.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:alumni_portal/providers/story_provider.dart';
import 'package:alumni_portal/providers/chat_provider.dart';
import 'package:alumni_portal/models/story_model.dart';
import 'package:alumni_portal/screens/stories/story_viewers_screen.dart';

class StoryViewer extends StatefulWidget {
  final UserStories userStory;
  final StoryProvider provider;

  const StoryViewer({
    super.key,
    required this.userStory,
    required this.provider,
  });

  @override
  State<StoryViewer> createState() => _StoryViewerState();
}

class _StoryViewerState extends State<StoryViewer>
    with SingleTickerProviderStateMixin {
  late PageController _pageController;
  late AnimationController _progressController;
  int _currentIndex = 0;
  final TextEditingController _replyController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _pageController = PageController();
    _progressController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 5),
    )..addStatusListener((status) {
        if (status == AnimationStatus.completed) {
          _nextStory();
        }
      });
    _startProgress();
    _markAsViewed();
  }

  void _startProgress() {
    _progressController.forward(from: 0);
  }

  void _markAsViewed() {
    final story = widget.userStory.stories[_currentIndex];
    widget.provider.markAsViewed(story.id);
  }

  void _nextStory() {
    if (_currentIndex < widget.userStory.stories.length - 1) {
      setState(() => _currentIndex++);
      _pageController.nextPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
      _startProgress();
      _markAsViewed();
    } else {
      Navigator.pop(context);
    }
  }

  void _previousStory() {
    if (_currentIndex > 0) {
      setState(() => _currentIndex--);
      _pageController.previousPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
      _startProgress();
    }
  }

  @override
  void dispose() {
    _pageController.dispose();
    _progressController.dispose();
    _replyController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final story = widget.userStory.stories[_currentIndex];

    return Scaffold(
      backgroundColor: Colors.black,
      body: GestureDetector(
        onTapUp: (details) {
          final screenWidth = MediaQuery.of(context).size.width;
          if (details.globalPosition.dx < screenWidth / 3) {
            _previousStory();
          } else {
            _nextStory();
          }
        },
        onVerticalDragEnd: (details) {
          if (details.primaryVelocity! > 300) {
            Navigator.pop(context);
          }
        },
        child: Stack(
          fit: StackFit.expand,
          children: [
            // Story content
            _buildStoryContent(story),
            // Progress bars
            Positioned(
              top: MediaQuery.of(context).padding.top + 8,
              left: 8,
              right: 8,
              child: Row(
                children: List.generate(
                  widget.userStory.stories.length,
                  (index) => Expanded(
                    child: Container(
                      margin: const EdgeInsets.symmetric(horizontal: 2),
                      height: 3,
                      child: index == _currentIndex
                          ? AnimatedBuilder(
                              animation: _progressController,
                              builder: (context, child) =>
                                  LinearProgressIndicator(
                                value: _progressController.value,
                                backgroundColor: Colors.white30,
                                valueColor:
                                    const AlwaysStoppedAnimation(Colors.white),
                              ),
                            )
                          : Container(
                              color: index < _currentIndex
                                  ? Colors.white
                                  : Colors.white30,
                            ),
                    ),
                  ),
                ),
              ),
            ),
            // Header
            Positioned(
              top: MediaQuery.of(context).padding.top + 20,
              left: 16,
              right: 16,
              child: Row(
                children: [
                  CircleAvatar(
                    radius: 18,
                    backgroundImage: widget.userStory.authorImageUrl != null
                        ? CachedNetworkImageProvider(
                            widget.userStory.authorImageUrl!)
                        : null,
                    child: widget.userStory.authorImageUrl == null
                        ? Text(widget.userStory.authorName.substring(0, 1))
                        : null,
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          widget.userStory.authorName,
                          style: const TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        Text(
                          timeago.format(story.createdAt),
                          style: const TextStyle(
                            color: Colors.white70,
                            fontSize: 12,
                          ),
                        ),
                      ],
                    ),
                  ),
                  if (widget.userStory.oderId == widget.provider.currentUserId)
                    IconButton(
                      icon: const Icon(Icons.delete, color: Colors.white),
                      onPressed: () => _deleteStory(story),
                    ),
                  IconButton(
                    icon: const Icon(Icons.close, color: Colors.white),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],
              ),
            ),
            // View count (for own stories)
            if (widget.userStory.oderId == widget.provider.currentUserId)
              Positioned(
                bottom: MediaQuery.of(context).padding.bottom + 16,
                left: 16,
                child: GestureDetector(
                  onTap: () => _showViewers(story),
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 8,
                    ),
                    decoration: BoxDecoration(
                      color: Colors.black.withValues(alpha: 0.5),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const Icon(Icons.visibility,
                            color: Colors.white, size: 18),
                        const SizedBox(width: 6),
                        Text(
                          '${story.viewCount} ${story.viewCount == 1 ? 'view' : 'views'}',
                          style: const TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        const SizedBox(width: 4),
                        const Icon(Icons.chevron_right,
                            color: Colors.white, size: 18),
                      ],
                    ),
                  ),
                ),
              ),
            // Reactions and Reply (for other users' stories)
            if (widget.userStory.oderId != widget.provider.currentUserId)
              Positioned(
                bottom: 0,
                left: 0,
                right: 0,
                child: _buildInteractionBar(story),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildInteractionBar(StoryModel story) {
    return Container(
      padding: EdgeInsets.fromLTRB(
        16,
        16,
        16,
        MediaQuery.of(context).padding.bottom + 16,
      ),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Colors.transparent, Colors.black87],
        ),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Quick reactions
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              _buildReactionButton('❤️'),
              _buildReactionButton('😂'),
              _buildReactionButton('😮'),
              _buildReactionButton('😢'),
              _buildReactionButton('👏'),
              _buildReactionButton('🔥'),
            ],
          ),
          const SizedBox(height: 12),
          // Reply input
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _replyController,
                  style: const TextStyle(color: Colors.white),
                  decoration: InputDecoration(
                    hintText: 'Send a message...',
                    hintStyle: const TextStyle(color: Colors.white60),
                    filled: true,
                    fillColor: Colors.white.withValues(alpha: 0.2),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(25),
                      borderSide: BorderSide.none,
                    ),
                    contentPadding: const EdgeInsets.symmetric(
                      horizontal: 20,
                      vertical: 12,
                    ),
                  ),
                  onTap: () {
                    _progressController.stop();
                  },
                  onSubmitted: (text) => _sendReply(story, text),
                ),
              ),
              const SizedBox(width: 8),
              IconButton(
                onPressed: () {
                  if (_replyController.text.trim().isNotEmpty) {
                    _sendReply(story, _replyController.text.trim());
                  }
                },
                icon: const Icon(Icons.send, color: Colors.white),
                style: IconButton.styleFrom(
                  backgroundColor: Colors.white.withValues(alpha: 0.2),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildReactionButton(String emoji) {
    return GestureDetector(
      onTap: () => _sendReaction(emoji),
      child: Container(
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          color: Colors.white.withValues(alpha: 0.2),
          shape: BoxShape.circle,
        ),
        child: Text(
          emoji,
          style: const TextStyle(fontSize: 24),
        ),
      ),
    );
  }

  void _sendReaction(String emoji) async {
    try {
      final currentUserId = widget.provider.currentUserId;
      if (currentUserId == null) return;

      final story = widget.userStory.stories[_currentIndex];
      
      // Update reaction in Firestore
      await FirebaseFirestore.instance
          .collection('stories')
          .doc(story.id)
          .update({
        'reactions.$currentUserId': emoji,
      });

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Sent $emoji reaction'),
            duration: const Duration(seconds: 1),
            backgroundColor: Colors.white.withOpacity(0.2),
          ),
        );
      }
    } catch (e) {
      print('Error sending reaction: $e');
    }
  }

  void _sendReply(StoryModel story, String message) async {
    if (message.isEmpty) return;

    try {
      final chatProvider = context.read<ChatProvider>();
      
      // Create or get chat with story author
      final chatId = await chatProvider.createOrGetChat(
        story.authorId,
        story.authorName,
      );

      if (chatId != null) {
        // Send message with story reply indicator
        await chatProvider.sendMessage(
          chatId: chatId,
          receiverId: story.authorId,
          messageText: '📖 Replied to story: $message',
        );

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Reply sent!'),
              duration: Duration(seconds: 1),
              backgroundColor: Colors.green,
            ),
          );
        }

        _replyController.clear();
        _progressController.forward();
      }
    } catch (e) {
      print('Error sending reply: $e');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Failed to send reply'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  void _deleteStory(StoryModel story) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Story'),
        content: const Text('Are you sure you want to delete this story?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () async {
              Navigator.pop(context); // Close dialog
              await widget.provider.deleteStory(story.id);
              if (mounted) {
                Navigator.pop(context); // Close story viewer
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Story deleted'),
                    backgroundColor: Colors.green,
                  ),
                );
              }
            },
            child: const Text('Delete', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
  }

  void _showViewers(StoryModel story) {
    _progressController.stop();
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => StoryViewersScreen(
          storyId: story.id,
          viewerIds: story.viewedBy,
          ownerId: story.authorId,
          reactions: story.reactions,
        ),
      ),
    ).then((_) {
      _progressController.forward();
    });
  }

  Widget _buildStoryContent(StoryModel story) {
    if (story.imageUrl != null) {
      return CachedNetworkImage(
        imageUrl: story.imageUrl!,
        fit: BoxFit.contain,
        placeholder: (_, __) => const Center(
          child: CircularProgressIndicator(color: Colors.white),
        ),
      );
    }

    // Text story with background color
    final bgColor = _parseColor(story.backgroundColor ?? '#1E88E5');
    return Container(
      color: bgColor,
      padding: const EdgeInsets.all(32),
      child: Center(
        child: Text(
          story.content ?? '',
          style: const TextStyle(
            color: Colors.white,
            fontSize: 24,
            fontWeight: FontWeight.w600,
          ),
          textAlign: TextAlign.center,
        ),
      ),
    );
  }

  Color _parseColor(String hex) {
    hex = hex.replaceFirst('#', '');
    if (hex.length == 6) hex = 'FF$hex';
    return Color(int.parse(hex, radix: 16));
  }
}
