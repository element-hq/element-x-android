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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.element.android.compound.previews.ColorPreview
import io.element.android.compound.screenshot.utils.createPaparazziRule
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.LinkColor
import io.element.android.compound.theme.SnackBarLabelColorDark
import io.element.android.compound.theme.SnackBarLabelColorLight
import org.junit.Rule
import org.junit.Test

class LegacyColorsTest {
    @get:Rule
    val paparazzi = createPaparazziRule()

    @Test
    fun screenshots() {
        paparazzi.snapshot(name = "Legacy Colors") {
            ElementTheme {
                Surface {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Legacy Colors")
                        Spacer(modifier = Modifier.height(10.dp))
                        LegacyColorPreview(
                            color = LinkColor,
                            name = "Link"
                        )
                        LegacyColorPreview(
                            color = SnackBarLabelColorLight,
                            name = "SnackBar Label - Light"
                        )
                        LegacyColorPreview(
                            color = SnackBarLabelColorDark,
                            name = "SnackBar Label - Dark"
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun LegacyColorPreview(color: Color, name: String) {
        ColorPreview(
            backgroundColor = Color.White,
            foregroundColor = Color.Black,
            name = name,
            color = color
        )
    }
}
