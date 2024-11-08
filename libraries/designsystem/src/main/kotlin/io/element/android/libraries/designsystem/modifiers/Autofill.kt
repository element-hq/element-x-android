/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.modifiers

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree

@Suppress("ModifierComposed")
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.autofill(autofillTypes: List<AutofillType>, onFill: (String) -> Unit) = composed {
    val autofillNode = AutofillNode(autofillTypes, onFill = onFill)
    LocalAutofillTree.current += autofillNode

    val autofill = LocalAutofill.current

    this
        .onGloballyPositioned {
            // Inform autofill framework of where our composable is so it can show the popup in the right place
            autofillNode.boundingBox = it.boundsInWindow()
        }
        .onFocusChanged {
            autofill?.run {
                if (it.isFocused) {
                    requestAutofillForNode(autofillNode)
                } else {
                    cancelAutofillForNode(autofillNode)
                }
            }
        }
}
