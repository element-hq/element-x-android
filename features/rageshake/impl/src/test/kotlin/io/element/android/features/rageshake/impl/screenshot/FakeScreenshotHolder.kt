/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.screenshot

import android.graphics.Bitmap

const val A_SCREENSHOT_URI = "file://content/uri"

class FakeScreenshotHolder(private val screenshotUri: String? = null) : ScreenshotHolder {
    override fun writeBitmap(data: Bitmap) = Unit

    override fun getFileUri() = screenshotUri

    override fun reset() = Unit
}
