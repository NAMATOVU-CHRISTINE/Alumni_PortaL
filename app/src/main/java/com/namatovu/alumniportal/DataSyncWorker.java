package com.namatovu.alumniportal;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Simple WorkManager worker that can be expanded to sync remote data (jobs, events, etc.).
 */
public class DataSyncWorker extends Worker {

    private static final String TAG = "DataSyncWorker";

    public DataSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Running data sync");
        try {
            // Get Firestore instance
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

            // Sync jobs
            com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> jobsTask = db.collection("jobs").get();
            com.google.android.gms.tasks.Tasks.await(jobsTask);
            if (jobsTask.isSuccessful() && jobsTask.getResult() != null) {
                java.util.List<com.google.firebase.firestore.DocumentSnapshot> docs = jobsTask.getResult().getDocuments();
                java.util.List<com.namatovu.alumniportal.db.JobEntity> entities = new java.util.ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot doc : docs) {
                    com.namatovu.alumniportal.db.JobEntity e = new com.namatovu.alumniportal.db.JobEntity();
                    e.id = doc.getId();
                    e.title = doc.getString("title");
                    e.company = doc.getString("company");
                    e.location = doc.getString("location");
                    e.description = doc.getString("description");
                    e.applyUrl = doc.getString("applyUrl");
                    entities.add(e);
                }
                if (!entities.isEmpty()) {
                    com.namatovu.alumniportal.db.AppDatabase dbLocal = com.namatovu.alumniportal.db.AppDatabase.getInstance(getApplicationContext());
                    dbLocal.jobDao().insertAll(entities);
                }
            }

            // Sync events
            com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> eventsTask = db.collection("events").get();
            com.google.android.gms.tasks.Tasks.await(eventsTask);
            if (eventsTask.isSuccessful() && eventsTask.getResult() != null) {
                java.util.List<com.google.firebase.firestore.DocumentSnapshot> docs = eventsTask.getResult().getDocuments();
                java.util.List<com.namatovu.alumniportal.db.EventEntity> entities = new java.util.ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot doc : docs) {
                    com.namatovu.alumniportal.db.EventEntity e = new com.namatovu.alumniportal.db.EventEntity();
                    e.id = doc.getId();
                    e.title = doc.getString("title");
                    e.link = doc.getString("link");
                    e.pubDate = doc.getString("pubDate");
                    e.description = doc.getString("description");
                    entities.add(e);
                }
                if (!entities.isEmpty()) {
                    com.namatovu.alumniportal.db.AppDatabase dbLocal = com.namatovu.alumniportal.db.AppDatabase.getInstance(getApplicationContext());
                    dbLocal.eventDao().insertAll(entities);
                }
            }

            return Result.success();
        } catch (Exception e) {
            Log.w(TAG, "Data sync failed", e);
            return Result.retry();
        }
    }
}
