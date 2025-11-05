package com.namatovu.alumniportal.db;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * FirestoreRepository — small, robust helper for Firestore operations used across the app.
 *
 * Improvements made:
 * - Moved to `db` package
 * - Singleton access via {@link #getInstance()}
 * - Centralized collection name constants
 * - Uses server timestamps (FieldValue.serverTimestamp()) for created/sent time
 * - Safer update for profiles with SetOptions.merge() to avoid accidental overwrites
 * - Basic input validation returning failed Tasks for invalid args
 */
public class FirestoreRepository {
    private static final String COL_USERS = "users";
    private static final String COL_CONNECTIONS = "connectionRequests";
    private static final String COL_MESSAGES = "messages";
    private static final String COL_MENTORSHIP = "mentorshipRequests";

    private final FirebaseFirestore db;

    private static FirestoreRepository instance;

    private FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreRepository getInstance() {
        if (instance == null) instance = new FirestoreRepository();
        return instance;
    }

    private CollectionReference col(String name) {
        return db.collection(name);
    }

    // --- Profiles ---
    public Task<Void> updateUserProfile(String uid, Map<String, Object> profileData) {
        if (uid == null || uid.isEmpty()) return Tasks.forException(new IllegalArgumentException("uid required"));
        if (profileData == null) return Tasks.forException(new IllegalArgumentException("profileData required"));
        // Use merge to avoid wiping other fields unintentionally
        return col(COL_USERS).document(uid).set(profileData, SetOptions.merge());
    }

    public Query searchUsers(Map<String, Object> filters) {
        CollectionReference users = col(COL_USERS);
        Query q = users;
        if (filters != null && !filters.isEmpty()) {
            for (Map.Entry<String, Object> e : filters.entrySet()) {
                q = q.whereEqualTo(e.getKey(), e.getValue());
            }
        }
        return q;
    }

    // --- Connections ---
    public Task<DocumentReference> sendConnectionRequest(String fromUid, String toUid, String message) {
        if (fromUid == null || toUid == null) return Tasks.forException(new IllegalArgumentException("fromUid and toUid required"));
        Map<String, Object> payload = new HashMap<>();
        payload.put("fromUid", fromUid);
        payload.put("toUid", toUid);
        payload.put("message", message);
        payload.put("status", "pending");
        payload.put("createdAt", FieldValue.serverTimestamp());
        return col(COL_CONNECTIONS).add(payload);
    }

    public Task<Void> updateConnectionRequestStatus(String requestId, String status) {
        if (requestId == null) return Tasks.forException(new IllegalArgumentException("requestId required"));
        return col(COL_CONNECTIONS).document(requestId)
                .update("status", status);
    }

    // --- Messaging ---
    public Task<DocumentReference> sendMessage(String fromUid, String toUid, String text) {
        if (fromUid == null || toUid == null) return Tasks.forException(new IllegalArgumentException("fromUid and toUid required"));
        Map<String, Object> payload = new HashMap<>();
        payload.put("fromUid", fromUid);
        payload.put("toUid", toUid);
        payload.put("text", text);
        payload.put("sentAt", FieldValue.serverTimestamp());
        return col(COL_MESSAGES).add(payload);
    }

    /**
     * Returns a query for messages sent from uidA to uidB. Callers that need both directions should
     * run two queries (A->B and B->A) or use a different data model (threadId) for efficient single-query reads.
     */
    public Query messagesBetween(String uidA, String uidB) {
        return col(COL_MESSAGES).whereEqualTo("fromUid", uidA).whereEqualTo("toUid", uidB);
    }

    // --- Mentorship ---
    public Task<DocumentReference> createMentorshipRequest(String requesterUid, String mentorUid, String topic, String message) {
        if (requesterUid == null || mentorUid == null) return Tasks.forException(new IllegalArgumentException("requesterUid and mentorUid required"));
        Map<String, Object> payload = new HashMap<>();
        payload.put("requesterUid", requesterUid);
        payload.put("mentorUid", mentorUid);
        payload.put("topic", topic);
        payload.put("message", message);
        payload.put("status", "pending");
        payload.put("createdAt", FieldValue.serverTimestamp());
        return col(COL_MENTORSHIP).add(payload);
    }

    public Task<Void> updateMentorshipStatus(String requestId, String status) {
        if (requestId == null) return Tasks.forException(new IllegalArgumentException("requestId required"));
        return col(COL_MENTORSHIP).document(requestId)
                .update("status", status);
    }
}
