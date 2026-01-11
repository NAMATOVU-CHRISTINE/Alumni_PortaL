import 'package:flutter/material.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:alumni_portal/models/news_model.dart';
import 'package:alumni_portal/config/theme.dart';

class ArticleDetailsScreen extends StatelessWidget {
  final String articleId;
  const ArticleDetailsScreen({super.key, required this.articleId});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Article')),
      body: FutureBuilder<DocumentSnapshot>(
        future: FirebaseFirestore.instance
            .collection('news')
            .doc(articleId)
            .get(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (!snapshot.hasData || !snapshot.data!.exists) {
            return const Center(child: Text('Article not found'));
          }
          final article = NewsModel.fromFirestore(snapshot.data!);
          return SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (article.imageUrl != null)
                  CachedNetworkImage(
                    imageUrl: article.imageUrl!,
                    height: 250,
                    width: double.infinity,
                    fit: BoxFit.cover,
                  ),
                Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (article.category != null)
                        Text(
                          article.category!.displayName,
                          style: const TextStyle(
                            color: AppColors.primary,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      const SizedBox(height: 8),
                      Text(
                        article.title ?? 'Article',
                        style: const TextStyle(
                          fontSize: 24,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          if (article.author != null)
                            Text(
                              'By ${article.author}',
                              style: const TextStyle(
                                color: AppColors.textSecondary,
                              ),
                            ),
                          const Spacer(),
                          Text(
                            article.formattedPublishDate,
                            style: const TextStyle(
                              color: AppColors.textSecondary,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 24),
                      Text(article.content ?? article.summary ?? 'No content'),
                      if (article.sourceUrl != null) ...[
                        const SizedBox(height: 24),
                        ElevatedButton(
                          onPressed: () =>
                              launchUrl(Uri.parse(article.sourceUrl!)),
                          child: const Text('Read Full Article'),
                        ),
                      ],
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
