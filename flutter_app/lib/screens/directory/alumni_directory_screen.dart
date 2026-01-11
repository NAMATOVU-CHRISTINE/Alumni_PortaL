import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:alumni_portal/providers/user_provider.dart';
import 'package:alumni_portal/models/user_model.dart';
import 'package:alumni_portal/config/theme.dart';

class AlumniDirectoryScreen extends StatefulWidget {
  const AlumniDirectoryScreen({super.key});

  @override
  State<AlumniDirectoryScreen> createState() => _AlumniDirectoryScreenState();
}

class _AlumniDirectoryScreenState extends State<AlumniDirectoryScreen> {
  final _searchController = TextEditingController();
  String? _selectedMajor;
  String? _selectedYear;

  @override
  void initState() {
    super.initState();
    _loadDirectory();
  }

  void _loadDirectory() {
    context.read<UserProvider>().loadAlumniDirectory(
      searchQuery: _searchController.text,
      filterByMajor: _selectedMajor,
      filterByYear: _selectedYear,
    );
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Alumni Directory'),
        actions: [
          IconButton(
            icon: const Icon(Icons.filter_list),
            onPressed: _showFilterDialog,
          ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: TextField(
              controller: _searchController,
              decoration: InputDecoration(
                hintText: 'Search alumni...',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: _searchController.text.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear),
                        onPressed: () {
                          _searchController.clear();
                          _loadDirectory();
                        },
                      )
                    : null,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
              onChanged: (_) => _loadDirectory(),
            ),
          ),
          Expanded(
            child: Consumer<UserProvider>(
              builder: (context, provider, _) {
                if (provider.isLoading) {
                  return const Center(child: CircularProgressIndicator());
                }

                final alumni = provider.alumniDirectory;
                if (alumni.isEmpty) {
                  return const Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          Icons.people_outline,
                          size: 64,
                          color: AppColors.textSecondary,
                        ),
                        SizedBox(height: 16),
                        Text(
                          'No alumni found',
                          style: TextStyle(color: AppColors.textSecondary),
                        ),
                      ],
                    ),
                  );
                }

                return RefreshIndicator(
                  onRefresh: () async => _loadDirectory(),
                  child: ListView.builder(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    itemCount: alumni.length,
                    itemBuilder: (context, index) =>
                        _buildAlumniCard(alumni[index]),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAlumniCard(UserModel user) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: AppColors.primary.withOpacity(0.1),
          backgroundImage: user.profileImageUrl != null
              ? CachedNetworkImageProvider(user.profileImageUrl!)
              : null,
          child: user.profileImageUrl == null
              ? Text(
                  user.fullName?.substring(0, 1).toUpperCase() ?? 'A',
                  style: const TextStyle(
                    color: AppColors.primary,
                    fontWeight: FontWeight.bold,
                  ),
                )
              : null,
        ),
        title: Text(
          user.fullName ?? 'Alumni',
          style: const TextStyle(fontWeight: FontWeight.w600),
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (user.currentJob != null || user.company != null)
              Text(
                '${user.currentJob ?? ''} ${user.company != null ? "at ${user.company}" : ""}',
                style: const TextStyle(fontSize: 12),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            if (user.major != null || user.graduationYear != null)
              Text(
                '${user.major ?? ''} ${user.graduationYear != null ? "• ${user.graduationYear}" : ""}',
                style: const TextStyle(
                  fontSize: 12,
                  color: AppColors.textSecondary,
                ),
              ),
          ],
        ),
        trailing: Container(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
          decoration: BoxDecoration(
            color: user.isAlumni
                ? AppColors.primary.withOpacity(0.1)
                : AppColors.secondary.withOpacity(0.1),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Text(
            user.userType.toUpperCase(),
            style: TextStyle(
              fontSize: 10,
              fontWeight: FontWeight.w500,
              color: user.isAlumni ? AppColors.primary : AppColors.secondary,
            ),
          ),
        ),
        onTap: () => context.push('/view-profile/${user.userId}'),
      ),
    );
  }

  void _showFilterDialog() {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (context) => Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Filter Alumni',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 24),
            TextField(
              decoration: const InputDecoration(
                labelText: 'Major/Field',
                prefixIcon: Icon(Icons.school),
              ),
              onChanged: (value) =>
                  _selectedMajor = value.isEmpty ? null : value,
            ),
            const SizedBox(height: 16),
            TextField(
              decoration: const InputDecoration(
                labelText: 'Graduation Year',
                prefixIcon: Icon(Icons.calendar_today),
              ),
              keyboardType: TextInputType.number,
              onChanged: (value) =>
                  _selectedYear = value.isEmpty ? null : value,
            ),
            const SizedBox(height: 24),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () {
                      _selectedMajor = null;
                      _selectedYear = null;
                      _loadDirectory();
                      Navigator.pop(context);
                    },
                    child: const Text('Clear'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () {
                      _loadDirectory();
                      Navigator.pop(context);
                    },
                    child: const Text('Apply'),
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
