/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api.ui

import androidx.collection.LruCache
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

private const val MAX_CACHED_PILL_SIZES = 1000

/**
 * Caches the measured size of the mention pill texts, shared by all the rendered messages.
 *
 * The same mentions tend to appear in many messages of a timeline, so this avoids measuring the same pill text again
 * every time a message containing it is composed, i.e. when it's scrolled into view.
 *
 * It must only be used from the main thread.
 */
internal object PillTextSizeCache {
    private data class Key(
        val text: String,
        val style: TextStyle,
        val density: Float,
        val fontScale: Float,
        val layoutDirection: LayoutDirection,
    )

    private val cache = LruCache<Key, IntSize>(MAX_CACHED_PILL_SIZES)

    /**
     * Returns the size of [text] laid out with [style], measuring it with [textMeasurer] only if it's not cached yet.
     */
    fun getOrMeasure(
        text: String,
        style: TextStyle,
        density: Density,
        layoutDirection: LayoutDirection,
        textMeasurer: TextMeasurer,
    ): IntSize {
        val key = Key(text, style, density.density, density.fontScale, layoutDirection)
        return cache[key] ?: textMeasurer.measure(
            text = text,
            style = style,
            density = density,
            layoutDirection = layoutDirection,
        ).size.also { cache.put(key, it) }
    }

    /** Clears the cache, for tests. */
    fun clear() = cache.evictAll()
}
