package io.element.android.x.features.rageshake.screenshot

import android.content.Context
import android.graphics.Bitmap
import io.element.android.x.core.bitmap.writeBitmap
import io.element.android.x.di.ApplicationContext
import java.io.File
import javax.inject.Inject

class ScreenshotHolder @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val file = File(context.filesDir, "screenshot.png")

    fun writeBitmap(data: Bitmap) {
        file.writeBitmap(data, Bitmap.CompressFormat.PNG, 85)
    }

    fun getFile() = file.takeIf { it.exists() && it.length() > 0 }

    fun reset() {
        file.delete()
    }
}
