package com.namatovu.alumniportal.repo;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight Firestore helper for core social features used by the Alumni Portal.
 * Methods return Tasks so callers can add listeners on success/failure.
 */
public class FirestoreRepository {
    private final FirebaseFirestore db;

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // --- Profiles ---
    public Task<Void> updateUserProfile(String uid, Map<String, Object> profileData) {
        if (uid == null) return Tasks.forException(new IllegalArgumentException("uid required"));
        return db.collection("users").document(uid).set(profileData);
    }

    public Query searchUsers(Map<String, Object> filters) {
        CollectionReference users = db.collection("users");
        Query q = users;
        if (filters != null) {
            for (Map.Entry<String, Object> e : filters.entrySet()) {
                q = q.whereEqualTo(e.getKey(), e.getValue());
            }
        }
        return q;
    }

    // --- Connections ---
    public Task<DocumentReference> sendConnectionRequest(String fromUid, String toUid, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fromUid", fromUid);
        payload.put("toUid", toUid);
        payload.put("message", message);
        payload.put("status", "pending");
        payload.put("createdAt", new Date());
        return db.collection("connectionRequests").add(payload);
    }

    public Task<Void> updateConnectionRequestStatus(String requestId, String status) {
        return db.collection("connectionRequests").document(requestId)
                .update("status", status);
    }

    // --- Messaging ---
    public Task<DocumentReference> sendMessage(String fromUid, String toUid, String text) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fromUid", fromUid);
        payload.put("toUid", toUid);
        payload.put("text", text);
        payload.put("sentAt", new Date());
        return db.collection("messages").add(payload);
    }

    public Query messagesBetween(String uidA, String uidB) {
        // Simple query to fetch messages where (from==A and to==B) OR (from==B and to==A)
        // Firestore does not support OR on different fields easily; callers may need to run two queries client-side.
        return db.collection("messages").whereEqualTo("fromUid", uidA).whereEqualTo("toUid", uidB);
    }

    // --- Mentorship ---
    public Task<DocumentReference> createMentorshipRequest(String requesterUid, String mentorUid, String topic, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("requesterUid", requesterUid);
        payload.put("mentorUid", mentorUid);
        payload.put("topic", topic);
        payload.put("message", message);
        payload.put("status", "pending");
        payload.put("createdAt", new Date());
        return db.collection("mentorshipRequests").add(payload);
    }

    public Task<Void> updateMentorshipStatus(String requestId, String status) {
        return db.collection("mentorshipRequests").document(requestId)
                .update("status", status);
    }
}
