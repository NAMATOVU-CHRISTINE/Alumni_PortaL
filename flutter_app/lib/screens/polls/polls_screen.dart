import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:alumni_portal/providers/poll_provider.dart';
import 'package:alumni_portal/models/poll_model.dart';
import 'package:alumni_portal/config/theme.dart';

class PollsScreen extends StatefulWidget {
  const PollsScreen({super.key});

  @override
  State<PollsScreen> createState() => _PollsScreenState();
}

class _PollsScreenState extends State<PollsScreen> {
  @override
  void initState() {
    super.initState();
    context.read<PollProvider>().loadPolls();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Polls & Surveys'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => context.read<PollProvider>().loadPolls(),
          ),
        ],
      ),
      body: Consumer<PollProvider>(
        builder: (context, provider, _) {
          if (provider.isLoading && provider.polls.isEmpty) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.polls.isEmpty) {
            return _buildEmptyState();
          }

          return RefreshIndicator(
            onRefresh: () => provider.loadPolls(),
            child: ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: provider.polls.length,
              itemBuilder: (context, index) {
                return PollCard(poll: provider.polls[index]);
              },
            ),
          );
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/create-post', extra: {'type': 'poll'}),
        icon: const Icon(Icons.add),
        label: const Text('Create Poll'),
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.poll, size: 80, color: Colors.grey[300]),
          const SizedBox(height: 16),
          const Text(
            'No polls yet',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
          ),
          const SizedBox(height: 8),
          Text(
            'Be the first to create a poll!',
            style: TextStyle(color: Colors.grey[600]),
          ),
          const SizedBox(height: 24),
          ElevatedButton.icon(
            onPressed: () =>
                context.push('/create-post', extra: {'type': 'poll'}),
            icon: const Icon(Icons.add),
            label: const Text('Create Poll'),
          ),
        ],
      ),
    );
  }
}

class PollCard extends StatelessWidget {
  final PollModel poll;

  const PollCard({super.key, required this.poll});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<PollProvider>();
    final hasVoted = provider.hasVoted(poll);
    final isExpired = poll.isExpired;

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Author info
            Row(
              children: [
                CircleAvatar(
                  radius: 20,
                  backgroundImage: poll.authorImageUrl != null
                      ? CachedNetworkImageProvider(poll.authorImageUrl!)
                      : null,
                  child: poll.authorImageUrl == null
                      ? Text(poll.authorName.substring(0, 1).toUpperCase())
                      : null,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        poll.authorName,
                        style: const TextStyle(fontWeight: FontWeight.w600),
                      ),
                      Text(
                        timeago.format(poll.createdAt),
                        style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                      ),
                    ],
                  ),
                ),
                if (isExpired)
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: Colors.grey[200],
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Text(
                      'Ended',
                      style: TextStyle(fontSize: 11, color: Colors.grey),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 16),

            // Question
            Text(
              poll.question,
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 16),

            // Options
            ...poll.options.map((option) => _buildOption(
                  context,
                  option,
                  hasVoted || isExpired,
                  poll.totalVotes,
                )),

            const SizedBox(height: 12),

            // Footer
            Row(
              children: [
                Icon(Icons.people, size: 16, color: Colors.grey[500]),
                const SizedBox(width: 4),
                Text(
                  '${poll.totalVotes} votes',
                  style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                ),
                const Spacer(),
                if (!isExpired)
                  Text(
                    'Ends ${timeago.format(poll.expiresAt, allowFromNow: true)}',
                    style: TextStyle(fontSize: 12, color: Colors.grey[500]),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildOption(
    BuildContext context,
    PollOption option,
    bool showResults,
    int totalVotes,
  ) {
    final percentage = totalVotes > 0 ? (option.votes / totalVotes * 100) : 0.0;
    final provider = context.read<PollProvider>();

    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: InkWell(
        onTap: showResults ? null : () => provider.vote(poll.id, option.id),
        borderRadius: BorderRadius.circular(8),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
          decoration: BoxDecoration(
            border: Border.all(color: Colors.grey[300]!),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Stack(
            children: [
              if (showResults)
                Positioned.fill(
                  child: FractionallySizedBox(
                    alignment: Alignment.centerLeft,
                    widthFactor: percentage / 100,
                    child: Container(
                      decoration: BoxDecoration(
                        color: AppColors.primary.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(6),
                      ),
                    ),
                  ),
                ),
              Row(
                children: [
                  Expanded(
                    child: Text(
                      option.text,
                      style: const TextStyle(fontWeight: FontWeight.w500),
                    ),
                  ),
                  if (showResults)
                    Text(
                      '${percentage.toStringAsFixed(0)}%',
                      style: TextStyle(
                        fontWeight: FontWeight.w600,
                        color: Colors.grey[700],
                      ),
                    ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
