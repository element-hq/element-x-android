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
import io.element.android.compound.previews.CompoundSemanticColorsDark
import io.element.android.compound.previews.CompoundSemanticColorsDarkHc
import io.element.android.compound.previews.CompoundSemanticColorsLight
import io.element.android.compound.previews.CompoundSemanticColorsLightHc
import io.element.android.compound.screenshot.utils.screenshotFile
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SemanticColorsTest {
    @Config(sdk = [35], qualifiers = "h2000dp-xhdpi")
    @Test
    fun screenshots() {
        captureRoboImage(file = screenshotFile("Compound Semantic Colors - Light.png")) {
            CompoundSemanticColorsLight()
        }

        captureRoboImage(file = screenshotFile("Compound Semantic Colors - Light HC.png")) {
            CompoundSemanticColorsLightHc()
        }

        captureRoboImage(file = screenshotFile("Compound Semantic Colors - Dark.png")) {
            CompoundSemanticColorsDark()
        }

        captureRoboImage(file = screenshotFile("Compound Semantic Colors - Dark HC.png")) {
            CompoundSemanticColorsDarkHc()
        }
    }
}
