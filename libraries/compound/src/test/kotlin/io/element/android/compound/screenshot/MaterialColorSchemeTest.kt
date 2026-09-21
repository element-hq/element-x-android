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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.screenshot.utils.createPaparazziRule
import io.element.android.compound.theme.ColorsSchemeDarkHcPreview
import io.element.android.compound.theme.ColorsSchemeDarkPreview
import io.element.android.compound.theme.ColorsSchemeLightHcPreview
import io.element.android.compound.theme.ColorsSchemeLightPreview
import io.element.android.compound.theme.ElementTheme
import org.junit.Rule
import org.junit.Test

class MaterialColorSchemeTest {
    @get:Rule
    val paparazzi = createPaparazziRule()

    @Test
    fun screenshots() {
        paparazzi.snapshot(name = "Material3 Colors - Light") {
            ElementTheme {
                Surface(Modifier.width(300.dp)) {
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
        paparazzi.snapshot(name = "Material3 Colors - Light HC") {
            ElementTheme {
                Surface(Modifier.width(300.dp)) {
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
        paparazzi.snapshot(name = "Material3 Colors - Dark") {
            ElementTheme {
                Surface(Modifier.width(300.dp)) {
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
        paparazzi.snapshot(name = "Material3 Colors - Dark HC") {
            ElementTheme {
                Surface(Modifier.width(300.dp)) {
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
