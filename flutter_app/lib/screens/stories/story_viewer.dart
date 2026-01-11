import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:alumni_portal/providers/story_provider.dart';
import 'package:alumni_portal/models/story_model.dart';

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
                child: Row(
                  children: [
                    const Icon(Icons.visibility,
                        color: Colors.white70, size: 18),
                    const SizedBox(width: 4),
                    Text(
                      '${story.viewCount} views',
                      style: const TextStyle(color: Colors.white70),
                    ),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
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
