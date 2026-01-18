import 'dart:io';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:image_picker/image_picker.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/providers/post_provider.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/config/theme.dart';

class CreatePostScreen extends StatefulWidget {
  final String postType;

  const CreatePostScreen({super.key, this.postType = 'update'});

  @override
  State<CreatePostScreen> createState() => _CreatePostScreenState();
}

class _CreatePostScreenState extends State<CreatePostScreen> {
  final _contentController = TextEditingController();
  File? _selectedImage;
  late String _postType;
  bool _isSubmitting = false;

  // Poll options
  final List<TextEditingController> _pollOptions = [
    TextEditingController(),
    TextEditingController(),
  ];

  final List<Map<String, dynamic>> _postTypes = [
    {'value': 'update', 'label': 'Update', 'icon': Icons.edit},
    {
      'value': 'achievement',
      'label': 'Achievement',
      'icon': Icons.emoji_events
    },
    {'value': 'job_update', 'label': 'Job Update', 'icon': Icons.work},
    {'value': 'article', 'label': 'Article', 'icon': Icons.article},
    {'value': 'poll', 'label': 'Poll', 'icon': Icons.poll},
    {'value': 'discussion', 'label': 'Discussion', 'icon': Icons.forum},
  ];

  @override
  void initState() {
    super.initState();
    _postType = widget.postType;
  }

  @override
  void dispose() {
    _contentController.dispose();
    for (var controller in _pollOptions) {
      controller.dispose();
    }
    super.dispose();
  }

  Future<void> _pickImage() async {
    // Request permission
    final status = await Permission.photos.request();

    if (!status.isGranted) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Photo permission is required to select images'),
          ),
        );
      }
      return;
    }

    final picker = ImagePicker();
    final image = await picker.pickImage(source: ImageSource.gallery);
    if (image != null) {
      setState(() => _selectedImage = File(image.path));
    }
  }

  Future<void> _submitPost() async {
    if (_contentController.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please write something to post')),
      );
      return;
    }

    setState(() => _isSubmitting = true);

    final success = await context.read<PostProvider>().createPost(
          content: _contentController.text.trim(),
          imageFile: _selectedImage,
          postType: _postType,
        );

    setState(() => _isSubmitting = false);

    if (success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Post shared successfully!'),
          backgroundColor: AppColors.success,
        ),
      );
      context.pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final user = context.watch<UserProvider>().currentUser;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Create Post'),
        actions: [
          TextButton(
            onPressed: _isSubmitting ? null : _submitPost,
            child: _isSubmitting
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Text('Post',
                    style: TextStyle(fontWeight: FontWeight.bold)),
          ),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // User info
                  Row(
                    children: [
                      CircleAvatar(
                        radius: 24,
                        backgroundImage: user?.profileImageUrl != null
                            ? CachedNetworkImageProvider(user!.profileImageUrl!)
                            : null,
                        child: user?.profileImageUrl == null
                            ? Text(
                                user?.fullName?.substring(0, 1).toUpperCase() ??
                                    'A')
                            : null,
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              user?.fullName ?? 'Alumni',
                              style:
                                  const TextStyle(fontWeight: FontWeight.w600),
                              overflow: TextOverflow.ellipsis,
                            ),
                            const SizedBox(height: 4),
                            // Post type selector
                            Container(
                              padding:
                                  const EdgeInsets.symmetric(horizontal: 8),
                              decoration: BoxDecoration(
                                border: Border.all(color: Colors.grey[300]!),
                                borderRadius: BorderRadius.circular(16),
                              ),
                              child: DropdownButtonHideUnderline(
                                child: DropdownButton<String>(
                                  value: _postType,
                                  isDense: true,
                                  isExpanded: true,
                                  items: _postTypes.map((type) {
                                    return DropdownMenuItem(
                                      value: type['value'] as String,
                                      child: Row(
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          Icon(type['icon'] as IconData,
                                              size: 16),
                                          const SizedBox(width: 4),
                                          Text(type['label'] as String,
                                              style: const TextStyle(
                                                  fontSize: 12)),
                                        ],
                                      ),
                                    );
                                  }).toList(),
                                  onChanged: (value) {
                                    if (value != null) {
                                      setState(() => _postType = value);
                                    }
                                  },
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),

                  // Content input
                  TextField(
                    controller: _contentController,
                    maxLines: null,
                    minLines: 5,
                    decoration: InputDecoration(
                      hintText: _getHintText(),
                      border: InputBorder.none,
                    ),
                    style: const TextStyle(fontSize: 16),
                  ),

                  // Selected image preview
                  if (_selectedImage != null) ...[
                    const SizedBox(height: 16),
                    Stack(
                      children: [
                        ClipRRect(
                          borderRadius: BorderRadius.circular(12),
                          child: Image.file(
                            _selectedImage!,
                            width: double.infinity,
                            height: 200,
                            fit: BoxFit.cover,
                          ),
                        ),
                        Positioned(
                          top: 8,
                          right: 8,
                          child: IconButton(
                            onPressed: () =>
                                setState(() => _selectedImage = null),
                            icon: const Icon(Icons.close),
                            style: IconButton.styleFrom(
                              backgroundColor: Colors.black54,
                              foregroundColor: Colors.white,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ],

                  const SizedBox(height: 24),
                  const Divider(),

                  // Action buttons
                  InkWell(
                    onTap: _pickImage,
                    child: Padding(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      child: Row(
                        children: [
                          Icon(Icons.image, color: AppColors.primary, size: 28),
                          const SizedBox(width: 12),
                          const Text(
                            'Add photo to your post',
                            style: TextStyle(
                              color: AppColors.primary,
                              fontSize: 15,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          // Bottom Post Button
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.white,
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.05),
                  blurRadius: 10,
                  offset: const Offset(0, -2),
                ),
              ],
            ),
            child: SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: _isSubmitting ? null : _submitPost,
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
                child: _isSubmitting
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: Colors.white,
                        ),
                      )
                    : const Text(
                        'Post',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  String _getHintText() {
    switch (_postType) {
      case 'achievement':
        return 'Share your achievement with the community...';
      case 'job_update':
        return 'Share your career update...';
      case 'article':
        return 'Share your thoughts or article...';
      default:
        return 'What\'s on your mind?';
    }
  }
}
