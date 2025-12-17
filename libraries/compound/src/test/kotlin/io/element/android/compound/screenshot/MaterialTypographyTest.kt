/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.screenshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.element.android.compound.previews.TypographyPreview
import io.element.android.compound.screenshot.utils.screenshotFile
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MaterialTypographyTest {
    @Test
    @Config(sdk = [35], qualifiers = "h2048dp-xxhdpi")
    fun screenshots() {
        captureRoboImage(file = screenshotFile("Material Typography.png")) {
            TypographyPreview()
        }
    }
}
