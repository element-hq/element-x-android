/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.screenshot

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import io.element.android.compound.previews.IconsCompoundPreviewDark
import io.element.android.compound.previews.IconsCompoundPreviewLight
import io.element.android.compound.previews.IconsCompoundPreviewRtl
import io.element.android.compound.previews.IconsPreview
import io.element.android.compound.screenshot.utils.createPaparazziRule
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.Theme
import io.element.android.compound.tokens.generated.CompoundIcons
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test

class CompoundIconTest {
    @get:Rule
    val paparazzi = createPaparazziRule()

    @Test
    fun screenshots() {
        paparazzi.snapshot(name = "Compound Icons - Light") {
            IconsCompoundPreviewLight()
        }
        paparazzi.snapshot(name = "Compound Icons - Rtl") {
            IconsCompoundPreviewRtl()
        }
        paparazzi.snapshot(name = "Compound Icons - Dark") {
            IconsCompoundPreviewDark()
        }
        paparazzi.snapshot(name = "Compound Vector Icons - Light") {
            val content: List<@Composable ColumnScope.() -> Unit> = CompoundIcons.all.map {
                @Composable { Icon(imageVector = it, contentDescription = null) }
            }
            ElementTheme {
                IconsPreview(
                    title = "Compound Vector Icons",
                    content = content.toImmutableList()
                )
            }
        }
        paparazzi.snapshot(name = "Compound Vector Icons - Dark") {
            val content: List<@Composable ColumnScope.() -> Unit> = CompoundIcons.all.map {
                @Composable { Icon(imageVector = it, contentDescription = null) }
            }
            ElementTheme(theme = Theme.Dark) {
                IconsPreview(
                    title = "Compound Vector Icons",
                    content = content.toImmutableList()
                )
            }
        }
    }
}
