/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.compound.screenshot.utils.createPaparazziRule
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.ForcedDarkElementTheme
import org.junit.Rule
import org.junit.Test

class ForcedDarkElementThemeTest {
    // The preview uses `fillMaxSize` to fill the inner Surface, so it needs a bounded frame.
    @get:Rule
    val paparazzi = createPaparazziRule(widthDp = 420, heightDp = 640)

    @Test
    fun screenshots() {
        paparazzi.snapshot(name = "ForcedDarkElementTheme") {
            ElementTheme {
                Surface {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "Outside")
                        ForcedDarkElementTheme(
                            colors = SemanticColorsLightDark.default,
                        ) {
                            Surface {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Text(text = "Inside ForcedDarkElementTheme", modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
