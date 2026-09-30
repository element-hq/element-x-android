/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item

import androidx.compose.runtime.Immutable

/**
 * Represents the target action of a send operation in a timeline.
 */
@Immutable
sealed interface SendTarget {
    /** Send a new event. */
    data object Event : SendTarget
    /** Edit an existing event. */
    data object Edit : SendTarget
    /** Redact an existing event. */
    data object Redaction : SendTarget
    /** React to an existing event with the given key. */
    data class Reaction(val key: String) : SendTarget
}
