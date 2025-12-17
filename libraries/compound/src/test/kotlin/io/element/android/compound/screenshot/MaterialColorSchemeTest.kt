/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.screenshot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.element.android.compound.screenshot.utils.screenshotFile
import io.element.android.compound.theme.ColorsSchemeDarkHcPreview
import io.element.android.compound.theme.ColorsSchemeDarkPreview
import io.element.android.compound.theme.ColorsSchemeLightHcPreview
import io.element.android.compound.theme.ColorsSchemeLightPreview
import io.element.android.compound.theme.ElementTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MaterialColorSchemeTest {
    @Test
    @Config(sdk = [35], qualifiers = "h2048dp-xhdpi")
    fun screenshots() {
        captureRoboImage(file = screenshotFile("Material3 Colors - Light.png")) {
            ElementTheme {
                Surface {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "M3 Light colors",
                            style = TextStyle.Default.copy(fontSize = 18.sp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ColorsSchemeLightPreview()
                    }
                }
            }
        }
        captureRoboImage(file = screenshotFile("Material3 Colors - Light HC.png")) {
            ElementTheme {
                Surface {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "M3 Light HC colors",
                            style = TextStyle.Default.copy(fontSize = 18.sp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ColorsSchemeLightHcPreview()
                    }
                }
            }
        }
        captureRoboImage(file = screenshotFile("Material3 Colors - Dark.png")) {
            ElementTheme {
                Surface {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "M3 Dark colors",
                            style = TextStyle.Default.copy(fontSize = 18.sp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ColorsSchemeDarkPreview()
                    }
                }
            }
        }
        captureRoboImage(file = screenshotFile("Material3 Colors - Dark HC.png")) {
            ElementTheme {
                Surface {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "M3 Dark HC colors",
                            style = TextStyle.Default.copy(fontSize = 18.sp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ColorsSchemeDarkHcPreview()
                    }
                }
            }
        }
    }
}
