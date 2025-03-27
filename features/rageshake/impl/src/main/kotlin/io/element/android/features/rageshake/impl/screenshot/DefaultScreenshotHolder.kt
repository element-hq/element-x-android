/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.screenshot

import android.content.Context
import android.graphics.Bitmap
import androidx.core.net.toUri
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.bitmap.writeBitmap
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import java.io.File
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultScreenshotHolder @Inject constructor(
    @ApplicationContext private val context: Context,
) : ScreenshotHolder {
    private val file = File(context.filesDir, "screenshot.png")

    override fun writeBitmap(data: Bitmap) {
        file.writeBitmap(data, Bitmap.CompressFormat.PNG, 85)
    }

    override fun getFileUri(): String? {
        return file
            .takeIf { it.exists() && it.length() > 0 }
            ?.toUri()
            ?.toString()
    }

    override fun reset() {
        file.safeDelete()
    }
}
