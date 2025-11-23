package com.namatovu.alumniportal.repo;

/**
 * Backwards-compatible wrapper for the Firestore helper.
 *
 * Consumers that previously referenced `com.namatovu.alumniportal.repo.FirestoreRepository`
 * can continue calling {@link #getInstance()} which delegates to the implementation in
 * `com.namatovu.alumniportal.db.FirestoreRepository`.
 */
public final class FirestoreRepository {
    private FirestoreRepository() { /* no-op */ }

    /**
     * Return the underlying DB repository singleton.
     */
    public static com.namatovu.alumniportal.db.FirestoreRepository getInstance() {
        return com.namatovu.alumniportal.db.FirestoreRepository.getInstance();
    }
}
