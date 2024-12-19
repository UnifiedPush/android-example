package org.unifiedpush.example.utils

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import org.unifiedpush.example.ApplicationServer

class DelayedRequestWorker(context: Context, workerParams: WorkerParameters) :
    Worker(
        context,
        workerParams
    ) {
    override fun doWork(): Result {
        val tag = DelayedRequestWorker::class.java.simpleName
        ApplicationServer(applicationContext).sendNotification { e ->
            e?.let {
                Log.d(tag, "An error occurred. $e")
            } ?: Log.d(tag, "Notification sent.")
        }
        return Result.success()
    }

    companion object {
        fun enqueue(context: Context, delayMs: Long) {
            val worker =
                OneTimeWorkRequest.Builder(DelayedRequestWorker::class.java).apply {
                    setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                }
            WorkManager.getInstance(context)
                .beginUniqueWork("BackgroundTest", ExistingWorkPolicy.REPLACE, worker.build())
                .enqueue()
        }
    }
}
