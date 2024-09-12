/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.platform.inspectable

/**
 * Applies the [ifTrue] modifier when the [condition] is true, [ifFalse] otherwise.
 */
fun Modifier.applyIf(
    condition: Boolean,
    ifTrue: Modifier.() -> Modifier,
    ifFalse: (Modifier.() -> Modifier)? = null
): Modifier = this then inspectable(
    inspectorInfo = debugInspectorInfo {
        name = "applyIf"
        value = condition
    }
) {
    this then when {
        condition -> ifTrue(Modifier)
        ifFalse != null -> ifFalse(Modifier)
        else -> Modifier
    }
}
