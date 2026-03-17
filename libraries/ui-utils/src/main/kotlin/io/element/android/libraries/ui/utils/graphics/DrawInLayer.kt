/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.utils.graphics

import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.layer.setOutline

/**
 * Draws the content of [recordBlock] in a separate layer, which can be customized using [composingStrategy], [outline] and [clip].
 */
context(scope: androidx.compose.ui.graphics.drawscope.DrawScope)
fun CacheDrawScope.drawInLayer(
    composingStrategy: CompositingStrategy = CompositingStrategy.Auto,
    outline: Outline? = null,
    clip: Boolean = false,
    recordBlock: ContentDrawScope.() -> Unit,
) {
    val layer = obtainGraphicsLayer().apply {
        this.compositingStrategy = composingStrategy
        this.clip = clip
        outline?.let { this.setOutline(it) }

        record(block = recordBlock)
    }
    scope.drawLayer(layer)
}
