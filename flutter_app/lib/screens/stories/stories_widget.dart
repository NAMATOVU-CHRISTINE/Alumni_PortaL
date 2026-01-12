import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/providers/story_provider.dart';
import 'package:alumni_portal/models/story_model.dart';
import 'package:alumni_portal/screens/stories/story_viewer.dart';
import 'package:alumni_portal/config/theme.dart';

class StoriesWidget extends StatefulWidget {
  const StoriesWidget({super.key});

  @override
  State<StoriesWidget> createState() => _StoriesWidgetState();
}

class _StoriesWidgetState extends State<StoriesWidget> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<StoryProvider>().loadStories();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<StoryProvider>(
      builder: (context, provider, _) {
        return SizedBox(
          height: 100,
          child: ListView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 12),
            children: [
              // Add story button
              _buildAddStoryButton(context),
              // User stories
              ...provider.userStories.map((userStory) => _buildStoryAvatar(
                    context,
                    userStory,
                    provider,
                  )),
            ],
          ),
        );
      },
    );
  }

  Widget _buildAddStoryButton(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 4),
      child: GestureDetector(
        onTap: () => context.push('/create-story'),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 64,
              height: 64,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: Colors.grey[100],
                border: Border.all(color: Colors.grey[300]!, width: 2),
              ),
              child: const Icon(Icons.add, color: AppColors.primary, size: 28),
            ),
            const SizedBox(height: 4),
            const Text(
              'Your Story',
              style: TextStyle(fontSize: 11),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStoryAvatar(
    BuildContext context,
    UserStories userStory,
    StoryProvider provider,
  ) {
    final isCurrentUser = userStory.oderId == provider.currentUserId;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 4),
      child: GestureDetector(
        onTap: () => _showStoryViewer(context, userStory, provider),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 68,
              height: 68,
              padding: const EdgeInsets.all(3),
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: userStory.hasUnviewed
                    ? const LinearGradient(
                        colors: [Colors.purple, Colors.orange, Colors.pink],
                      )
                    : null,
                border: userStory.hasUnviewed
                    ? null
                    : Border.all(color: Colors.grey[300]!, width: 2),
              ),
              child: CircleAvatar(
                backgroundColor: Colors.grey[200],
                backgroundImage: userStory.authorImageUrl != null
                    ? CachedNetworkImageProvider(userStory.authorImageUrl!)
                    : null,
                child: userStory.authorImageUrl == null
                    ? Text(
                        userStory.authorName.substring(0, 1).toUpperCase(),
                        style: const TextStyle(fontWeight: FontWeight.bold),
                      )
                    : null,
              ),
            ),
            const SizedBox(height: 4),
            SizedBox(
              width: 64,
              child: Text(
                isCurrentUser ? 'You' : userStory.authorName.split(' ').first,
                style: const TextStyle(fontSize: 11),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                textAlign: TextAlign.center,
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showStoryViewer(
    BuildContext context,
    UserStories userStory,
    StoryProvider provider,
  ) {
    Navigator.of(context).push(
      PageRouteBuilder(
        opaque: false,
        pageBuilder: (context, _, __) => StoryViewer(
          userStory: userStory,
          provider: provider,
        ),
      ),
    );
  }
}
