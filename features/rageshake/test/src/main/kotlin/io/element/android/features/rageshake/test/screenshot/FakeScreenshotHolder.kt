/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.test.screenshot

import android.graphics.Bitmap
import io.element.android.features.rageshake.api.screenshot.ScreenshotHolder

const val A_SCREENSHOT_URI = "file://content/uri"

class FakeScreenshotHolder(private val screenshotUri: String? = null) : ScreenshotHolder {
    override fun writeBitmap(data: Bitmap) = Unit

    override fun getFileUri() = screenshotUri

    override fun reset() = Unit
}
