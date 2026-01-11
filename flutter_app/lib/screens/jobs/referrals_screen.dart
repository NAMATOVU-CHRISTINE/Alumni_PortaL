import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:alumni_portal/providers/referral_provider.dart';
import 'package:alumni_portal/models/referral_model.dart';
import 'package:alumni_portal/config/theme.dart';

class ReferralsScreen extends StatefulWidget {
  const ReferralsScreen({super.key});

  @override
  State<ReferralsScreen> createState() => _ReferralsScreenState();
}

class _ReferralsScreenState extends State<ReferralsScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    context.read<ReferralProvider>().loadReferrals();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Job Referrals'),
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: 'Received'),
            Tab(text: 'Sent'),
          ],
        ),
      ),
      body: Consumer<ReferralProvider>(
        builder: (context, provider, _) {
          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          return TabBarView(
            controller: _tabController,
            children: [
              _buildReferralsList(provider.receivedReferrals, isReceived: true),
              _buildReferralsList(provider.sentReferrals, isReceived: false),
            ],
          );
        },
      ),
    );
  }

  Widget _buildReferralsList(List<ReferralModel> referrals,
      {required bool isReceived}) {
    if (referrals.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              isReceived ? Icons.inbox : Icons.send,
              size: 64,
              color: Colors.grey[300],
            ),
            const SizedBox(height: 16),
            Text(
              isReceived
                  ? 'No referrals received yet'
                  : 'No referrals sent yet',
              style: TextStyle(color: Colors.grey[600]),
            ),
            if (!isReceived) ...[
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => context.go('/jobs'),
                child: const Text('Browse Jobs to Refer'),
              ),
            ],
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: () => context.read<ReferralProvider>().loadReferrals(),
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: referrals.length,
        itemBuilder: (context, index) => _buildReferralCard(
          referrals[index],
          isReceived: isReceived,
        ),
      ),
    );
  }

  Widget _buildReferralCard(ReferralModel referral,
      {required bool isReceived}) {
    final statusColor = _getStatusColor(referral.status);

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header
            Row(
              children: [
                CircleAvatar(
                  radius: 20,
                  backgroundImage: (isReceived
                              ? referral.referrerImageUrl
                              : referral.referrerImageUrl) !=
                          null
                      ? CachedNetworkImageProvider(
                          isReceived
                              ? referral.referrerImageUrl!
                              : referral.referrerImageUrl!,
                        )
                      : null,
                  child: (isReceived
                              ? referral.referrerImageUrl
                              : referral.referrerImageUrl) ==
                          null
                      ? Text(
                          (isReceived
                                  ? referral.referrerName
                                  : referral.referredUserName)
                              .substring(0, 1)
                              .toUpperCase(),
                        )
                      : null,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        isReceived
                            ? '${referral.referrerName} referred you'
                            : 'Referred ${referral.referredUserName}',
                        style: const TextStyle(fontWeight: FontWeight.w600),
                      ),
                      Text(
                        timeago.format(referral.createdAt),
                        style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                      ),
                    ],
                  ),
                ),
                Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: statusColor.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    referral.statusLabel,
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.w600,
                      color: statusColor,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            // Job info
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.grey[50],
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                children: [
                  const Icon(Icons.work, color: AppColors.primary),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          referral.jobTitle,
                          style: const TextStyle(fontWeight: FontWeight.w600),
                        ),
                        Text(
                          referral.company,
                          style:
                              TextStyle(fontSize: 12, color: Colors.grey[600]),
                        ),
                      ],
                    ),
                  ),
                  TextButton(
                    onPressed: () =>
                        context.push('/job-details/${referral.jobId}'),
                    child: const Text('View'),
                  ),
                ],
              ),
            ),
            // Note
            if (referral.note != null && referral.note!.isNotEmpty) ...[
              const SizedBox(height: 12),
              Text(
                '"${referral.note}"',
                style: TextStyle(
                  fontStyle: FontStyle.italic,
                  color: Colors.grey[700],
                ),
              ),
            ],
            // Actions for received referrals
            if (isReceived && referral.status == 'pending') ...[
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () => _updateStatus(referral.id, 'declined'),
                      child: const Text('Decline'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: () => _updateStatus(referral.id, 'accepted'),
                      child: const Text('Accept'),
                    ),
                  ),
                ],
              ),
            ],
          ],
        ),
      ),
    );
  }

  Color _getStatusColor(String status) {
    switch (status) {
      case 'pending':
        return Colors.orange;
      case 'accepted':
        return Colors.blue;
      case 'applied':
        return Colors.purple;
      case 'hired':
        return Colors.green;
      case 'declined':
        return Colors.grey;
      default:
        return Colors.grey;
    }
  }

  void _updateStatus(String referralId, String status) {
    context.read<ReferralProvider>().updateReferralStatus(referralId, status);
  }
}
