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

    // Check for query parameters
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final uri = Uri.parse(GoRouterState.of(context).uri.toString());
      final typeParam = uri.queryParameters['type'];
      if (typeParam != null && typeParam == 'poll') {
        setState(() {
          _postType = 'poll';
        });
      }
    });
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

    // Validate poll options if it's a poll
    if (_postType == 'poll') {
      final validOptions = _pollOptions
          .where((controller) => controller.text.trim().isNotEmpty)
          .toList();

      if (validOptions.length < 2) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
              content: Text('Please provide at least 2 poll options')),
        );
        return;
      }
    }

    setState(() => _isSubmitting = true);

    // Prepare poll data if it's a poll
    Map<String, dynamic>? pollData;
    if (_postType == 'poll') {
      final options = _pollOptions
          .where((controller) => controller.text.trim().isNotEmpty)
          .map((controller) => {
                'text': controller.text.trim(),
                'voters': <String>[],
                'votes': 0,
              })
          .toList();

      pollData = {
        'options': options,
        'expiresAt': null, // Could add expiration later
        'allowMultipleChoices': false,
        'totalVotes': 0,
      };
    }

    final success = await context.read<PostProvider>().createPost(
          content: _contentController.text.trim(),
          imageFile: _selectedImage,
          postType: _postType,
          pollData: pollData,
        );

    setState(() => _isSubmitting = false);

    if (success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(_postType == 'poll'
              ? 'Poll created successfully!'
              : 'Post shared successfully!'),
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
                    minLines: _postType == 'poll' ? 2 : 5,
                    decoration: InputDecoration(
                      hintText: _getHintText(),
                      hintStyle: TextStyle(color: Colors.grey[600]),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: BorderSide(color: Colors.grey[300]!),
                      ),
                      enabledBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: BorderSide(color: Colors.grey[300]!),
                      ),
                      focusedBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide:
                            BorderSide(color: AppColors.primary, width: 2),
                      ),
                      filled: true,
                      fillColor: Colors.white,
                      contentPadding: const EdgeInsets.all(16),
                    ),
                    style: const TextStyle(fontSize: 16, color: Colors.black87),
                  ),

                  // Poll options (only show when poll type is selected)
                  if (_postType == 'poll') ...[
                    const SizedBox(height: 16),
                    _buildPollOptions(),
                  ],

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
                  color: Colors.black.withValues(alpha: 0.05),
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
      case 'poll':
        return 'Ask a question to get community input...';
      default:
        return 'What\'s on your mind?';
    }
  }

  Widget _buildPollOptions() {
    return Container(
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
              Icon(Icons.poll, color: AppColors.primary, size: 20),
              const SizedBox(width: 8),
              const Text(
                'Poll Options',
                style: TextStyle(
                  fontWeight: FontWeight.w600,
                  fontSize: 16,
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          ..._pollOptions.asMap().entries.map((entry) {
            final index = entry.key;
            final controller = entry.value;
            return Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: controller,
                      decoration: InputDecoration(
                        hintText: 'Option ${index + 1}',
                        prefixIcon: Icon(
                          Icons.radio_button_unchecked,
                          color: AppColors.primary,
                          size: 20,
                        ),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide(color: Colors.grey[300]!),
                        ),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide(color: Colors.grey[300]!),
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide(color: AppColors.primary),
                        ),
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 12,
                          vertical: 12,
                        ),
                        filled: true,
                        fillColor: Colors.white,
                      ),
                    ),
                  ),
                  if (_pollOptions.length > 2)
                    IconButton(
                      onPressed: () => _removePollOption(index),
                      icon: const Icon(Icons.remove_circle_outline),
                      color: Colors.red,
                    ),
                ],
              ),
            );
          }).toList(),
          if (_pollOptions.length < 6)
            TextButton.icon(
              onPressed: _addPollOption,
              icon: const Icon(Icons.add),
              label: const Text('Add Option'),
              style: TextButton.styleFrom(
                foregroundColor: AppColors.primary,
              ),
            ),
        ],
      ),
    );
  }

  void _addPollOption() {
    if (_pollOptions.length < 6) {
      setState(() {
        _pollOptions.add(TextEditingController());
      });
    }
  }

  void _removePollOption(int index) {
    if (_pollOptions.length > 2) {
      setState(() {
        _pollOptions[index].dispose();
        _pollOptions.removeAt(index);
      });
    }
  }
}
