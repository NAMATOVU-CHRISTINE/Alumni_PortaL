import 'package:cloud_firestore/cloud_firestore.dart';

class EndorsementModel {
  final String id;
  final String endorserId;
  final String endorserName;
  final String? endorserImageUrl;
  final String endorsedUserId;
  final String skill;
  final DateTime createdAt;

  EndorsementModel({
    required this.id,
    required this.endorserId,
    required this.endorserName,
    this.endorserImageUrl,
    required this.endorsedUserId,
    required this.skill,
    required this.createdAt,
  });

  factory EndorsementModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return EndorsementModel(
      id: doc.id,
      endorserId: data['endorserId'] ?? '',
      endorserName: data['endorserName'] ?? 'Unknown',
      endorserImageUrl: data['endorserImageUrl'],
      endorsedUserId: data['endorsedUserId'] ?? '',
      skill: data['skill'] ?? '',
      createdAt: data['createdAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['createdAt'])
          : DateTime.now(),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'endorserId': endorserId,
      'endorserName': endorserName,
      'endorserImageUrl': endorserImageUrl,
      'endorsedUserId': endorsedUserId,
      'skill': skill,
      'createdAt': createdAt.millisecondsSinceEpoch,
    };
  }
}

class SkillEndorsement {
  final String skill;
  final int count;
  final List<EndorsementModel> endorsements;

  SkillEndorsement({
    required this.skill,
    required this.count,
    required this.endorsements,
  });
}

class RecommendationModel {
  final String id;
  final String recommenderId;
  final String recommenderName;
  final String? recommenderImageUrl;
  final String? recommenderTitle;
  final String recommendedUserId;
  final String relationship;
  final String content;
  final DateTime createdAt;

  RecommendationModel({
    required this.id,
    required this.recommenderId,
    required this.recommenderName,
    this.recommenderImageUrl,
    this.recommenderTitle,
    required this.recommendedUserId,
    required this.relationship,
    required this.content,
    required this.createdAt,
  });

  factory RecommendationModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return RecommendationModel(
      id: doc.id,
      recommenderId: data['recommenderId'] ?? '',
      recommenderName: data['recommenderName'] ?? 'Unknown',
      recommenderImageUrl: data['recommenderImageUrl'],
      recommenderTitle: data['recommenderTitle'],
      recommendedUserId: data['recommendedUserId'] ?? '',
      relationship: data['relationship'] ?? '',
      content: data['content'] ?? '',
      createdAt: data['createdAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(data['createdAt'])
          : DateTime.now(),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'recommenderId': recommenderId,
      'recommenderName': recommenderName,
      'recommenderImageUrl': recommenderImageUrl,
      'recommenderTitle': recommenderTitle,
      'recommendedUserId': recommendedUserId,
      'relationship': relationship,
      'content': content,
      'createdAt': createdAt.millisecondsSinceEpoch,
    };
  }
}
