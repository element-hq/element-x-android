/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

/** Size in sp of the message text in the timeline, which the user can adjust. */
object MessageTextSize {
    const val MIN = 12
    const val DEFAULT = 14
    const val MAX = 22

    val range = MIN..MAX

    /** Multiplier to apply to the timeline text to render it at [sizeSp]. */
    fun scaleOf(sizeSp: Int): Float = sizeSp.coerceIn(range) / DEFAULT.toFloat()
}
