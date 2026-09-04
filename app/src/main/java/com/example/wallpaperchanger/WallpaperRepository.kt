package com.example.wallpaperchanger

import android.content.Context
import android.net.Uri

sealed class WallpaperItem {
    data class Builtin(val drawableRes: Int) : WallpaperItem()
    data class UserPhoto(val uri: Uri) : WallpaperItem()
}

object WallpaperRepository {

    private const val PREFS = "wallpaper_prefs"
    private const val KEY_USER_URIS = "user_uris"
    private const val KEY_INTERVAL_MINUTES = "interval_minutes"
    private const val KEY_AUTO_ROTATE = "auto_rotate"
    private const val KEY_CURRENT_INDEX = "current_index"

    val builtinDrawables = listOf(
        R.drawable.gradient_1,
        R.drawable.gradient_2,
        R.drawable.gradient_3,
        R.drawable.gradient_4,
        R.drawable.gradient_5,
        R.drawable.gradient_6
    )

    fun getAllWallpapers(context: Context): List<WallpaperItem> {
        val builtins = builtinDrawables.map { WallpaperItem.Builtin(it) }
        val userPhotos = getUserUris(context).map { WallpaperItem.UserPhoto(it) }
        return builtins + userPhotos
    }

    fun addUserPhoto(context: Context, uri: Uri) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = getUserUris(context).map { it.toString() }.toMutableSet()
        current.add(uri.toString())
        prefs.edit().putStringSet(KEY_USER_URIS, current).apply()
    }

    fun removeUserPhoto(context: Context, uri: Uri) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = getUserUris(context).map { it.toString() }.toMutableSet()
        current.remove(uri.toString())
        prefs.edit().putStringSet(KEY_USER_URIS, current).apply()
    }

    private fun getUserUris(context: Context): List<Uri> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_USER_URIS, emptySet()) ?: emptySet()
        return set.map { Uri.parse(it) }
    }

    fun setIntervalMinutes(context: Context, minutes: Long) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_INTERVAL_MINUTES, minutes).apply()
    }

    fun getIntervalMinutes(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_INTERVAL_MINUTES, 60L)
    }

    fun setAutoRotateEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_ROTATE, enabled).apply()
    }

    fun isAutoRotateEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_ROTATE, false)
    }

    fun getCurrentIndex(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_CURRENT_INDEX, -1)
    }

    fun setCurrentIndex(context: Context, index: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_CURRENT_INDEX, index).apply()
    }

    fun pickNextIndex(context: Context): Int {
        val all = getAllWallpapers(context)
        if (all.isEmpty()) return -1
        val current = getCurrentIndex(context)
        val next = if (all.size == 1) 0 else (current + 1) % all.size
        setCurrentIndex(context, next)
        return next
    }
}
