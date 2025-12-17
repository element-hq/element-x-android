/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.element.android.compound.previews.ColorsSchemePreview
import io.element.android.compound.screenshot.utils.screenshotFile
import io.element.android.compound.theme.ElementTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MaterialYouThemeTest {
    @Test
    @Config(sdk = [35], qualifiers = "h2048dp-xhdpi")
    fun screenshots() {
        captureRoboImage(file = screenshotFile("MaterialYou Theme - Light.png")) {
            ElementTheme(dynamicColor = true) {
                Surface {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "Material You Theme - Light")
                        Spacer(modifier = Modifier.height(12.dp))
                        ColorsSchemePreview(Color.White, Color.Black, ElementTheme.materialColors)
                    }
                }
            }
        }
        captureRoboImage(file = screenshotFile("MaterialYou Theme - Dark.png")) {
            ElementTheme(dynamicColor = true, darkTheme = true) {
                Surface {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "Material You Theme - Dark")
                        Spacer(modifier = Modifier.height(12.dp))
                        ColorsSchemePreview(Color.White, Color.Black, ElementTheme.materialColors)
                    }
                }
            }
        }
    }
}
