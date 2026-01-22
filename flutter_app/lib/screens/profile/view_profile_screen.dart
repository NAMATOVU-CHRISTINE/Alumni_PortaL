import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/providers/chat_provider.dart';
import 'package:alumni_portal/models/user_model.dart';
import 'package:alumni_portal/config/theme.dart';

class ViewProfileScreen extends StatefulWidget {
  final String userId;

  const ViewProfileScreen({super.key, required this.userId});

  @override
  State<ViewProfileScreen> createState() => _ViewProfileScreenState();
}

class _ViewProfileScreenState extends State<ViewProfileScreen> {
  UserModel? _user;
  bool _isLoading = true;
  bool _isConnected = false;
  bool _isFollowing = false;
  bool _connectionLoading = false;

  @override
  void initState() {
    super.initState();
    _loadUser();
    _checkConnectionStatus();
  }

  Future<void> _loadUser() async {
    final user = await context.read<UserProvider>().getUserById(widget.userId);
    if (mounted) {
      setState(() {
        _user = user;
        _isLoading = false;
      });
    }
  }

  Future<void> _checkConnectionStatus() async {
    final currentUserId = FirebaseAuth.instance.currentUser?.uid;
    if (currentUserId == null) return;

    // Check if connected (type: connection)
    final connectionDoc = await FirebaseFirestore.instance
        .collection('connections')
        .where('userId', isEqualTo: currentUserId)
        .where('connectedUserId', isEqualTo: widget.userId)
        .where('type', isEqualTo: 'connection')
        .get();

    // Check if following (type: follow)
    final followDoc = await FirebaseFirestore.instance
        .collection('connections')
        .where('userId', isEqualTo: currentUserId)
        .where('connectedUserId', isEqualTo: widget.userId)
        .where('type', isEqualTo: 'follow')
        .get();

    if (mounted) {
      setState(() {
        _isConnected = connectionDoc.docs.isNotEmpty;
        _isFollowing = followDoc.docs.isNotEmpty;
      });
    }
  }

  Future<void> _toggleConnection() async {
    final currentUserId = FirebaseAuth.instance.currentUser?.uid;
    if (currentUserId == null || _user == null) return;

    setState(() => _connectionLoading = true);

    try {
      if (_isConnected) {
        // Remove connection
        final docs = await FirebaseFirestore.instance
            .collection('connections')
            .where('userId', isEqualTo: currentUserId)
            .where('connectedUserId', isEqualTo: widget.userId)
            .where('type', isEqualTo: 'connection')
            .get();

        for (var doc in docs.docs) {
          await doc.reference.delete();
        }

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Disconnected from ${_user!.fullName}'),
            ),
          );
        }
      } else {
        // Add connection
        await FirebaseFirestore.instance.collection('connections').add({
          'userId': currentUserId,
          'connectedUserId': widget.userId,
          'type': 'connection',
          'createdAt': FieldValue.serverTimestamp(),
        });

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Connected with ${_user!.fullName}'),
              backgroundColor: Colors.green,
            ),
          );
        }
      }

      await _checkConnectionStatus();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Failed to update connection'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _connectionLoading = false);
      }
    }
  }

  Future<void> _toggleFollow() async {
    final currentUserId = FirebaseAuth.instance.currentUser?.uid;
    if (currentUserId == null || _user == null) return;

    setState(() => _connectionLoading = true);

    try {
      if (_isFollowing) {
        // Unfollow
        final docs = await FirebaseFirestore.instance
            .collection('connections')
            .where('userId', isEqualTo: currentUserId)
            .where('connectedUserId', isEqualTo: widget.userId)
            .where('type', isEqualTo: 'follow')
            .get();

        for (var doc in docs.docs) {
          await doc.reference.delete();
        }

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Unfollowed ${_user!.fullName}'),
            ),
          );
        }
      } else {
        // Follow
        await FirebaseFirestore.instance.collection('connections').add({
          'userId': currentUserId,
          'connectedUserId': widget.userId,
          'type': 'follow',
          'createdAt': FieldValue.serverTimestamp(),
        });

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Following ${_user!.fullName}'),
              backgroundColor: Colors.green,
            ),
          );
        }
      }

      await _checkConnectionStatus();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Failed to update follow status'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _connectionLoading = false);
      }
    }
  }

  Future<void> _startChat() async {
    if (_user == null) return;

    final chatProvider = context.read<ChatProvider>();
    final chatId = await chatProvider.createOrGetChat(
      widget.userId,
      _user!.fullName ?? 'User',
    );

    if (chatId != null && mounted) {
      context.push(
        '/chat/$chatId',
        extra: {
          'name': _user!.fullName ?? 'Chat',
          'image': _user!.profileImageUrl,
          'lastSeen': 'Last seen recently',
        },
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Profile')),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _user == null
              ? const Center(child: Text('User not found'))
              : SingleChildScrollView(
                  child: Column(
                    children: [
                      _buildProfileHeader(),
                      _buildProfileInfo(),
                      _buildSkillsSection(),
                      const SizedBox(height: 24),
                      _buildActionButtons(),
                      const SizedBox(height: 32),
                    ],
                  ),
                ),
    );
  }

  Widget _buildProfileHeader() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [AppColors.primary, AppColors.primaryDark],
        ),
      ),
      child: Column(
        children: [
          CircleAvatar(
            radius: 50,
            backgroundColor: Colors.white,
            backgroundImage: _user?.profileImageUrl != null
                ? CachedNetworkImageProvider(_user!.profileImageUrl!)
                : null,
            child: _user?.profileImageUrl == null
                ? Text(
                    _user?.fullName?.substring(0, 1).toUpperCase() ?? 'A',
                    style: const TextStyle(
                      fontSize: 40,
                      color: AppColors.primary,
                    ),
                  )
                : null,
          ),
          const SizedBox(height: 16),
          Text(
            _user?.fullName ?? 'Alumni User',
            style: const TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          if (_user?.username != null)
            Text(
              '@${_user!.username}',
              style: const TextStyle(color: Colors.white70),
            ),
          const SizedBox(height: 8),
          if (_user?.currentJob != null || _user?.company != null)
            Text(
              '${_user?.currentJob ?? ''} ${_user?.company != null ? "at ${_user!.company}" : ""}',
              style: const TextStyle(color: Colors.white70),
              textAlign: TextAlign.center,
            ),
          const SizedBox(height: 16),
          // Connections/Followers Stats
          _buildConnectionsStats(),
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.2),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Text(
              _user?.userType.toUpperCase() ?? 'USER',
              style: const TextStyle(
                color: Colors.white,
                fontSize: 12,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildConnectionsStats() {
    return StreamBuilder<QuerySnapshot>(
      stream: FirebaseFirestore.instance
          .collection('connections')
          .where('userId', isEqualTo: widget.userId)
          .where('type', isEqualTo: 'connection')
          .snapshots(),
      builder: (context, myConnectionsSnapshot) {
        return StreamBuilder<QuerySnapshot>(
          stream: FirebaseFirestore.instance
              .collection('connections')
              .where('connectedUserId', isEqualTo: widget.userId)
              .where('type', isEqualTo: 'connection')
              .snapshots(),
          builder: (context, theirConnectionsSnapshot) {
            return StreamBuilder<QuerySnapshot>(
              stream: FirebaseFirestore.instance
                  .collection('connections')
                  .where('connectedUserId', isEqualTo: widget.userId)
                  .where('type', isEqualTo: 'follow')
                  .snapshots(),
              builder: (context, followersSnapshot) {
                // Count connections where this user is either userId or connectedUserId
                final myConnections = myConnectionsSnapshot.hasData
                    ? myConnectionsSnapshot.data!.docs.length
                    : 0;
                final theirConnections = theirConnectionsSnapshot.hasData
                    ? theirConnectionsSnapshot.data!.docs.length
                    : 0;
                final connectionsCount = myConnections + theirConnections;

                final followersCount = followersSnapshot.hasData
                    ? followersSnapshot.data!.docs.length
                    : 0;

                return Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    InkWell(
                      onTap: () => _showConnectionsList('connections'),
                      child: _buildStatItem('Connections', connectionsCount),
                    ),
                    Container(
                      height: 30,
                      width: 1,
                      color: Colors.white.withValues(alpha: 0.3),
                      margin: const EdgeInsets.symmetric(horizontal: 24),
                    ),
                    InkWell(
                      onTap: () => _showConnectionsList('followers'),
                      child: _buildStatItem('Followers', followersCount),
                    ),
                  ],
                );
              },
            );
          },
        );
      },
    );
  }

  void _showConnectionsList(String type) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (context) => DraggableScrollableSheet(
        initialChildSize: 0.7,
        minChildSize: 0.5,
        maxChildSize: 0.95,
        expand: false,
        builder: (context, scrollController) {
          return Column(
            children: [
              Padding(
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    Text(
                      type == 'connections' ? 'Connections' : 'Followers',
                      style: const TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
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
                child: StreamBuilder<QuerySnapshot>(
                  stream: type == 'connections'
                      ? FirebaseFirestore.instance
                          .collection('connections')
                          .where('userId', isEqualTo: widget.userId)
                          .where('type', isEqualTo: 'connection')
                          .snapshots()
                      : FirebaseFirestore.instance
                          .collection('connections')
                          .where('connectedUserId', isEqualTo: widget.userId)
                          .where('type', isEqualTo: 'follow')
                          .snapshots(),
                  builder: (context, snapshot) {
                    if (snapshot.connectionState == ConnectionState.waiting) {
                      return const Center(child: CircularProgressIndicator());
                    }

                    if (!snapshot.hasData || snapshot.data!.docs.isEmpty) {
                      return Center(
                        child: Text(
                          type == 'connections'
                              ? 'No connections yet'
                              : 'No followers yet',
                          style: TextStyle(color: Colors.grey[600]),
                        ),
                      );
                    }

                    return ListView.builder(
                      controller: scrollController,
                      itemCount: snapshot.data!.docs.length,
                      itemBuilder: (context, index) {
                        final doc = snapshot.data!.docs[index];
                        final userId = type == 'connections'
                            ? doc.get('connectedUserId')
                            : doc.get('userId');
                        return FutureBuilder<UserModel?>(
                          future:
                              context.read<UserProvider>().getUserById(userId),
                          builder: (context, userSnapshot) {
                            if (!userSnapshot.hasData) {
                              return const SizedBox.shrink();
                            }
                            final user = userSnapshot.data!;
                            return ListTile(
                              leading: CircleAvatar(
                                backgroundImage: user.profileImageUrl != null
                                    ? CachedNetworkImageProvider(
                                        user.profileImageUrl!)
                                    : null,
                                child: user.profileImageUrl == null
                                    ? Text(
                                        user.fullName
                                                ?.substring(0, 1)
                                                .toUpperCase() ??
                                            'A',
                                      )
                                    : null,
                              ),
                              title: Text(user.fullName ?? 'User'),
                              subtitle: Text(
                                user.currentJob ?? user.major ?? '',
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                              onTap: () {
                                Navigator.pop(context);
                                context.push('/view-profile/${user.userId}');
                              },
                            );
                          },
                        );
                      },
                    );
                  },
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  Widget _buildStatItem(String label, int count) {
    return Column(
      children: [
        Text(
          count.toString(),
          style: const TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
        Text(
          label,
          style: const TextStyle(
            fontSize: 12,
            color: Colors.white70,
          ),
        ),
      ],
    );
  }

  Widget _buildProfileInfo() {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (_user?.bio != null && _user!.bio!.isNotEmpty) ...[
            const Text(
              'About',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            Text(
              _user!.bio!,
              style: const TextStyle(color: AppColors.textSecondary),
            ),
            const SizedBox(height: 24),
          ],
          const Text(
            'Details',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),
          if (_user?.major != null)
            _buildInfoRow(Icons.school, 'Major', _user!.major!),
          if (_user?.graduationYear != null)
            _buildInfoRow(
              Icons.calendar_today,
              'Graduation Year',
              _user!.graduationYear!,
            ),
          if (_user?.location != null)
            _buildInfoRow(Icons.location_on, 'Location', _user!.location!),
          if (_user?.workStatus != null)
            _buildInfoRow(Icons.work, 'Work Status', _user!.workStatus!),
          if (_user?.industry != null)
            _buildInfoRow(Icons.business, 'Industry', _user!.industry!),
        ],
      ),
    );
  }

  Widget _buildInfoRow(IconData icon, String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          Icon(icon, size: 20, color: AppColors.textSecondary),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: const TextStyle(
                    fontSize: 12,
                    color: AppColors.textSecondary,
                  ),
                ),
                Text(
                  value,
                  style: const TextStyle(fontWeight: FontWeight.w500),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSkillsSection() {
    if (_user?.skills.isEmpty ?? true) return const SizedBox.shrink();

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Skills',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: _user!.skills
                .map(
                  (skill) => Chip(
                    label: Text(skill),
                    backgroundColor: AppColors.primary.withValues(alpha: 0.1),
                  ),
                )
                .toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildActionButtons() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: ElevatedButton.icon(
                  onPressed: _connectionLoading ? null : _toggleConnection,
                  icon: Icon(
                      _isConnected ? Icons.person_remove : Icons.person_add),
                  label: Text(_isConnected ? 'Connected' : 'Connect'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor:
                        _isConnected ? Colors.grey : AppColors.primary,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: _connectionLoading ? null : _toggleFollow,
                  icon: Icon(_isFollowing ? Icons.check : Icons.add),
                  label: Text(_isFollowing ? 'Following' : 'Follow'),
                  style: OutlinedButton.styleFrom(
                    foregroundColor:
                        _isFollowing ? Colors.grey : AppColors.primary,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton.icon(
              onPressed: _startChat,
              icon: const Icon(Icons.chat_bubble_outline),
              label: const Text('Message'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.white,
                foregroundColor: AppColors.primary,
                side: const BorderSide(color: AppColors.primary),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
