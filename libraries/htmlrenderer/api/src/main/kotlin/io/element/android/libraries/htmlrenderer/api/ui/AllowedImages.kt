/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.impl.renderer

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The set of inline-image identifiers (their URLs) that are allowed to be displayed while images are
 * hidden by default (see the `hideImages` parameter of [HtmlMessageContent]).
 *
 * It is internally a `Set<String>` backed by observable snapshot state, so revealing an image
 * recomposes only the inline content that shows it. [allow] adds an identifier; [contains] reads it.
 */
@Stable
class AllowedImages(initial: Set<String> = emptySet()) {
    private val allowed = mutableStateSetOf<String>().apply { addAll(initial) }

    /** Whether the image identified by [id] (its URL) is allowed to be displayed. */
    operator fun contains(id: String): Boolean = id in allowed

    /** Reveals the image identified by [id] (its URL). */
    fun allow(id: String) {
        allowed.add(id)
    }
}

/**
 * Provides the [AllowedImages] used to reveal individually-tapped images while images are hidden by
 * default. [HtmlMessageContent] provides a per-message instance; a host may provide its own above it
 * to share or persist the revealed set.
 */
val LocalAllowedImages = staticCompositionLocalOf { AllowedImages() }
