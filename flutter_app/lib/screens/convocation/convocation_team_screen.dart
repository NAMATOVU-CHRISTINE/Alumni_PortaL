import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/convocation_provider.dart';
import '../../models/convocation_team_member.dart';
import 'team_member_detail_screen.dart';

class ConvocationTeamScreen extends StatefulWidget {
  const ConvocationTeamScreen({super.key});

  @override
  State<ConvocationTeamScreen> createState() => _ConvocationTeamScreenState();
}

class _ConvocationTeamScreenState extends State<ConvocationTeamScreen> {
  @override
  void initState() {
    super.initState();
    Future.microtask(
        () => context.read<ConvocationProvider>().fetchTeamMembers());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Convocation Team'),
        elevation: 0,
      ),
      body: Consumer<ConvocationProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.error != null) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.error_outline, size: 64, color: Colors.red),
                  const SizedBox(height: 16),
                  Text(provider.error!),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => provider.fetchTeamMembers(),
                    child: const Text('Retry'),
                  ),
                ],
              ),
            );
          }

          if (provider.activeMembers.isEmpty) {
            return const Center(
              child: Text('No team members found'),
            );
          }

          return SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildHeader(),
                _buildLeadershipSection(provider),
                _buildCommitteesSection(provider),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            Theme.of(context).primaryColor,
            Theme.of(context).primaryColor.withOpacity(0.8),
          ],
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'MUST Convocation Team',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Leading the graduation ceremonies with excellence',
            style: TextStyle(
              fontSize: 14,
              color: Colors.white.withOpacity(0.9),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLeadershipSection(ConvocationProvider provider) {
    final leadership = provider.activeMembers
        .where((m) =>
            ['Chairperson', 'Vice-Chair', 'Secretary'].contains(m.position))
        .toList();

    if (leadership.isEmpty) return const SizedBox.shrink();

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Leadership',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          ...leadership.map((member) => _buildTeamMemberCard(member)),
        ],
      ),
    );
  }

  Widget _buildCommitteesSection(ConvocationProvider provider) {
    final committees = provider.membersByCommittee;

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Committees',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          ...committees.entries.map((entry) {
            return _buildCommitteeSection(entry.key, entry.value);
          }),
        ],
      ),
    );
  }

  Widget _buildCommitteeSection(
      String committee, List<ConvocationTeamMember> members) {
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: ExpansionTile(
        title: Text(
          committee,
          style: const TextStyle(fontWeight: FontWeight.w600),
        ),
        subtitle: Text('${members.length} members'),
        children:
            members.map((member) => _buildTeamMemberListTile(member)).toList(),
      ),
    );
  }

  Widget _buildTeamMemberCard(ConvocationTeamMember member) {
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: InkWell(
        onTap: () => _navigateToDetail(member),
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              CircleAvatar(
                radius: 40,
                backgroundImage: member.photoUrl.isNotEmpty
                    ? NetworkImage(member.photoUrl)
                    : null,
                child: member.photoUrl.isEmpty
                    ? Text(
                        member.fullName[0].toUpperCase(),
                        style: const TextStyle(fontSize: 24),
                      )
                    : null,
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      member.fullName,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      member.position,
                      style: TextStyle(
                        fontSize: 14,
                        color: Theme.of(context).primaryColor,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      member.role,
                      style: const TextStyle(
                        fontSize: 12,
                        color: Colors.grey,
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
              const Icon(Icons.chevron_right),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTeamMemberListTile(ConvocationTeamMember member) {
    return ListTile(
      leading: CircleAvatar(
        backgroundImage:
            member.photoUrl.isNotEmpty ? NetworkImage(member.photoUrl) : null,
        child: member.photoUrl.isEmpty
            ? Text(member.fullName[0].toUpperCase())
            : null,
      ),
      title: Text(member.fullName),
      subtitle: Text(member.position),
      trailing: const Icon(Icons.chevron_right, size: 20),
      onTap: () => _navigateToDetail(member),
    );
  }

  void _navigateToDetail(ConvocationTeamMember member) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => TeamMemberDetailScreen(member: member),
      ),
    );
  }
}
