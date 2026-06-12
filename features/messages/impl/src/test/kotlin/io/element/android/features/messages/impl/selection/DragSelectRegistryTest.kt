/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import org.junit.Test

class DragSelectRegistryTest {
    private val a = EventId("\$A")
    private val b = EventId("\$B")

    @Test
    fun `eventAt returns the row whose vertical span contains the point`() {
        val registry = DragSelectRegistry()
        registry.put(a, Rect(0f, 0f, 100f, 50f)) // y in [0, 50)
        registry.put(b, Rect(0f, 50f, 100f, 100f)) // y in [50, 100)
        assertThat(registry.eventAt(Offset(10f, 25f))).isEqualTo(a)
        assertThat(registry.eventAt(Offset(90f, 75f))).isEqualTo(b)
    }

    @Test
    fun `eventAt ignores the X axis so a gutter point still resolves to its row`() {
        val registry = DragSelectRegistry()
        // Bubble pushed to the right; the row is conceptually full-width.
        registry.put(a, Rect(200f, 0f, 300f, 50f))
        assertThat(registry.eventAt(Offset(0f, 25f))).isEqualTo(a)
    }

    @Test
    fun `eventAt picks the nearest centre when rows momentarily overlap`() {
        val registry = DragSelectRegistry()
        registry.put(a, Rect(0f, 0f, 100f, 60f)) // centre 30
        registry.put(b, Rect(0f, 40f, 100f, 100f)) // centre 70, overlaps [40, 60)
        // y = 45 sits in both; nearer A's centre (30) than B's (70).
        assertThat(registry.eventAt(Offset(0f, 45f))).isEqualTo(a)
        // y = 55 sits in both; nearer B's centre.
        assertThat(registry.eventAt(Offset(0f, 55f))).isEqualTo(b)
    }

    @Test
    fun `eventAt falls back to the nearest row outside the spans, and is null only when empty`() {
        val registry = DragSelectRegistry()
        registry.put(a, Rect(0f, 0f, 100f, 50f))
        // Above / below every span -> nearest row, so a finger held at an edge while
        // auto-scrolling keeps resolving a target and the range keeps growing.
        assertThat(registry.eventAt(Offset(0f, -10f))).isEqualTo(a)
        assertThat(registry.eventAt(Offset(0f, 80f))).isEqualTo(a)
        registry.remove(a)
        assertThat(registry.eventAt(Offset(0f, 25f))).isNull()
    }
}
