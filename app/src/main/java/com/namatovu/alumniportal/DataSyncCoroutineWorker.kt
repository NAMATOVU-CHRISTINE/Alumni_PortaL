package com.namatovu.alumniportal

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import com.google.android.gms.tasks.Tasks
import com.namatovu.alumniportal.db.AppDatabase
import java.util.concurrent.TimeUnit

/**
 * Coroutine-based WorkManager worker with timeout and upsert behavior.
 */
class DataSyncCoroutineWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "DataSyncCoroutineWorker"
        // 30 seconds timeout for the whole sync operation
        private const val SYNC_TIMEOUT_MS = 30_000L
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Running data sync (coroutine)")
        try {
            withTimeout(SYNC_TIMEOUT_MS) {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                // Sync jobs
                val jobsTask = firestore.collection("jobs").get()
                val jobsSnapshot = Tasks.await(jobsTask, 20, TimeUnit.SECONDS)
                val jobEntities = jobsSnapshot.documents.map { doc ->
                    com.namatovu.alumniportal.db.JobEntity().apply {
                        id = doc.id
                        title = doc.getString("title")
                        company = doc.getString("company")
                        location = doc.getString("location")
                        description = doc.getString("description")
                        applyUrl = doc.getString("applyUrl")
                    }
                }
                if (jobEntities.isNotEmpty()) {
                    val dbLocal = AppDatabase.getInstance(applicationContext)
                    dbLocal.jobDao().insertAll(jobEntities)
                }

                // Sync events
                val eventsTask = firestore.collection("events").get()
                val eventsSnapshot = Tasks.await(eventsTask, 20, TimeUnit.SECONDS)
                val eventEntities = eventsSnapshot.documents.map { doc ->
                    com.namatovu.alumniportal.db.EventEntity().apply {
                        id = doc.id
                        title = doc.getString("title")
                        link = doc.getString("link")
                        pubDate = doc.getString("pubDate")
                        description = doc.getString("description")
                    }
                }
                if (eventEntities.isNotEmpty()) {
                    val dbLocal = AppDatabase.getInstance(applicationContext)
                    dbLocal.eventDao().insertAll(eventEntities)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "Data sync failed", e)
            Result.retry()
        }
    }
}
