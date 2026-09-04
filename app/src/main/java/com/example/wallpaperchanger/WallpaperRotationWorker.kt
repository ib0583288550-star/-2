package com.example.wallpaperchanger

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WallpaperRotationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val all = WallpaperRepository.getAllWallpapers(applicationContext)
        if (all.isEmpty()) return Result.success()

        val nextIndex = WallpaperRepository.pickNextIndex(applicationContext)
        val item = all.getOrNull(nextIndex) ?: return Result.success()

        val success = WallpaperSetter.apply(applicationContext, item)
        return if (success) Result.success() else Result.retry()
    }
}
