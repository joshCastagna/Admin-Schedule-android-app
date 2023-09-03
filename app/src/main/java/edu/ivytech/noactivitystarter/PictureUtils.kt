package edu.ivytech.noactivitystarter

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.os.Build
import android.view.WindowInsets
import androidx.exifinterface.media.ExifInterface

fun getScaledBitmap(path: String?, destWidth: Int, destHeight: Int): Bitmap? {
        // Read in the dimensions of the image on disk

    val exif = ExifInterface(path!!)
    val orientation: Int =
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        val srcWidth = options.outWidth.toFloat()
        val srcHeight = options.outHeight.toFloat()

        // Figure out how much to scale down by
        var inSampleSize = 1
        if (srcHeight > destHeight || srcWidth > destWidth) {
            val heightScale = srcHeight / destHeight
            val widthScale = srcWidth / destWidth
            inSampleSize = Math.round(if (heightScale > widthScale) heightScale else widthScale)
        }
        options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize

        val bitmap = BitmapFactory.decodeFile(path, options)
        return if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            rotateBitmap(bitmap, 90f)
        } else {
            bitmap
        }
        }


fun getScaledBitmap(path: ByteArray?, destWidth: Int, destHeight: Int): Bitmap? {
    // Read in the dimensions of the image on disk
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeByteArray(path, 0, path?.size!!,options)
    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    // Figure out how much to scale down by
    var inSampleSize = 1
    if (srcHeight > destHeight || srcWidth > destWidth) {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth
        inSampleSize = Math.round(if (heightScale > widthScale) heightScale else widthScale)
    }
    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    val bitmap = BitmapFactory.decodeByteArray(path, 0, path?.size!!,options)
    return bitmap
}

    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun cropToSquare(bitmap: Bitmap): Bitmap? {
        val width = bitmap.width
        val height = bitmap.height
        val sqWidth = if (height > width) { width } else { height }
        val sqHeight = if (height > width){ height - (height - width)}  else{ height }
        var cropW = (width - height) / 2
        if(cropW < 0){
            cropW = 0
        }
        var cropH = (height - width) / 2
        if(cropH < 0){
            cropH = 0
        }
        return Bitmap.createBitmap(bitmap, cropW, cropH, sqWidth, sqHeight)
    }

    fun getScaledBitmap(path: String?, activity: Activity): Bitmap? {
        val size = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val windowInsets: WindowInsets = windowMetrics.windowInsets

            val insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
            val insetsWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom

            val b = windowMetrics.bounds
            size.x = b.width() - insetsWidth
            size.y = b.height() - insetsHeight
        } else {
            activity.windowManager.defaultDisplay
                .getSize(size)
        }
        return getScaledBitmap(path, size.y, size.x)
    }

fun getScaledBitmap(path: ByteArray?, activity: Activity): Bitmap? {
    val size = Point()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = activity.windowManager.currentWindowMetrics
        val windowInsets: WindowInsets = windowMetrics.windowInsets

        val insets = windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
        val insetsWidth = insets.right + insets.left
        val insetsHeight = insets.top + insets.bottom

        val b = windowMetrics.bounds
        size.x = b.width() - insetsWidth
        size.y = b.height() - insetsHeight
    } else {
        activity.windowManager.defaultDisplay
            .getSize(size)
    }
    return getScaledBitmap(path, size.y, size.x)
}