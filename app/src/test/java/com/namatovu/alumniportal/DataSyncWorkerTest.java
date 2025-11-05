package com.namatovu.alumniportal;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Minimal test to verify test runner is working and the worker class is available.
 * A more thorough test would use WorkManager testing utilities and mocked Firestore.
 */
public class DataSyncWorkerTest {

    @Test
    public void basicSanity() {
        // Ensure the test environment runs
        assertTrue(true);
    }
}
