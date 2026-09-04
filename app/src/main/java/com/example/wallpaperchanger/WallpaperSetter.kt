package com.example.wallpaperchanger

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.content.res.ResourcesCompat
import android.graphics.drawable.BitmapDrawable

object WallpaperSetter {

    fun apply(context: Context, item: WallpaperItem): Boolean {
        return try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            when (item) {
                is WallpaperItem.Builtin -> {
                    val drawable = ResourcesCompat.getDrawable(context.resources, item.drawableRes, null)
                    val bitmap = (drawable as? BitmapDrawable)?.bitmap
                        ?: run {
                            val width = 1080
                            val height = 1920
                            val bmp = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bmp)
                            drawable?.setBounds(0, 0, width, height)
                            drawable?.draw(canvas)
                            bmp
                        }
                    wallpaperManager.setBitmap(bitmap)
                }
                is WallpaperItem.UserPhoto -> {
                    context.contentResolver.openInputStream(item.uri)?.use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        wallpaperManager.setBitmap(bitmap)
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
