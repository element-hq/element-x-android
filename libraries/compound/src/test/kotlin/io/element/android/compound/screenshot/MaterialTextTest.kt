/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.screenshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.element.android.compound.screenshot.utils.screenshotFile
import io.element.android.compound.theme.MaterialTextPreview
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MaterialTextTest {
    @Test
    @Config(sdk = [35], qualifiers = "w480dp-h1200dp-xxhdpi")
    fun screenshots() {
        captureRoboImage(file = screenshotFile("MaterialText Colors.png")) {
            MaterialTextPreview()
        }
    }
}
