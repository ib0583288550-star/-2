package com.example.wallpaperchanger

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object RotationScheduler {

    private const val WORK_NAME = "wallpaper_rotation_work"

    fun schedule(context: Context, intervalMinutes: Long) {
        val safeInterval = if (intervalMinutes < 15) 15 else intervalMinutes
        val request = PeriodicWorkRequestBuilder<WallpaperRotationWorker>(
            safeInterval, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
