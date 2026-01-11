import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:alumni_portal/providers/poll_provider.dart';
import 'package:alumni_portal/models/poll_model.dart';
import 'package:alumni_portal/config/theme.dart';

class ActivePollsWidget extends StatelessWidget {
  const ActivePollsWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<PollProvider>(
      builder: (context, provider, _) {
        final activePolls = provider.activePolls.take(2).toList();
        if (activePolls.isEmpty) return const SizedBox.shrink();

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Row(
                    children: [
                      Icon(Icons.poll, color: AppColors.primary, size: 20),
                      SizedBox(width: 8),
                      Text(
                        'Active Polls',
                        style: TextStyle(
                            fontSize: 18, fontWeight: FontWeight.bold),
                      ),
                    ],
                  ),
                  TextButton(
                    onPressed: () => context.push('/polls'),
                    child: const Text('See all'),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 8),
            ...activePolls
                .map((poll) => _buildPollCard(context, poll, provider)),
          ],
        );
      },
    );
  }

  Widget _buildPollCard(
      BuildContext context, PollModel poll, PollProvider provider) {
    final hasVoted = provider.hasVoted(poll);

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              poll.question,
              style: const TextStyle(fontWeight: FontWeight.w600),
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: 12),
            if (hasVoted)
              // Show results
              ...poll.options.take(2).map((opt) {
                final percentage = poll.totalVotes > 0
                    ? (opt.votes / poll.totalVotes * 100)
                    : 0.0;
                return Padding(
                  padding: const EdgeInsets.only(bottom: 8),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(opt.text, style: const TextStyle(fontSize: 13)),
                          Text('${percentage.toStringAsFixed(0)}%',
                              style: const TextStyle(
                                  fontSize: 13, fontWeight: FontWeight.w600)),
                        ],
                      ),
                      const SizedBox(height: 4),
                      LinearProgressIndicator(
                        value: percentage / 100,
                        backgroundColor: Colors.grey[200],
                        valueColor: AlwaysStoppedAnimation(AppColors.primary),
                      ),
                    ],
                  ),
                );
              })
            else
              // Show vote buttons
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: poll.options.take(3).map((opt) {
                  return OutlinedButton(
                    onPressed: () => provider.vote(poll.id, opt.id),
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 12, vertical: 8),
                      visualDensity: VisualDensity.compact,
                    ),
                    child: Text(opt.text, style: const TextStyle(fontSize: 12)),
                  );
                }).toList(),
              ),
            const SizedBox(height: 8),
            Row(
              children: [
                Icon(Icons.people, size: 14, color: Colors.grey[500]),
                const SizedBox(width: 4),
                Text(
                  '${poll.totalVotes} votes',
                  style: TextStyle(fontSize: 11, color: Colors.grey[600]),
                ),
                const Spacer(),
                TextButton(
                  onPressed: () => context.push('/polls'),
                  style: TextButton.styleFrom(
                    padding: EdgeInsets.zero,
                    visualDensity: VisualDensity.compact,
                  ),
                  child:
                      const Text('View Poll', style: TextStyle(fontSize: 12)),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
