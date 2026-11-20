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
import io.element.android.compound.previews.ColorsSchemePreview
import io.element.android.compound.screenshot.utils.createPaparazziRule
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.Theme
import org.junit.Rule
import org.junit.Test

class MaterialYouThemeTest {
    @get:Rule
    val paparazzi = createPaparazziRule()

    @Test
    fun screenshots() {
        paparazzi.snapshot(name = "MaterialYou Theme - Light") {
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
        paparazzi.snapshot(name = "MaterialYou Theme - Dark") {
            ElementTheme(dynamicColor = true, theme = Theme.Dark) {
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
