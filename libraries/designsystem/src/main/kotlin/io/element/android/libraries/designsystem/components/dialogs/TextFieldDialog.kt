/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.dialogs

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.element.android.libraries.designsystem.components.list.TextFieldListItem

@Composable
fun TextFieldDialog(
    title: String,
    onSubmit: (String) -> Unit,
    onDismissRequest: () -> Unit,
    value: String?,
    placeholder: String?,
    validation: (String?) -> Boolean = { true },
    onValidationErrorMessage: String? = null,
    autoSelectOnDisplay: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldContents by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                value.orEmpty(),
                selection = TextRange(value.orEmpty().length)
            )
        )
    }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var canRequestFocus by rememberSaveable { mutableStateOf(false) }
    val canSubmit by remember { derivedStateOf { validation(textFieldContents.text) } }
    ListDialog(
        title = title,
        onSubmit = { onSubmit(textFieldContents.text) },
        onDismissRequest = onDismissRequest,
        enabled = canSubmit,
    ) {
        item {
            TextFieldListItem(
                placeholder = placeholder.orEmpty(),
                text = textFieldContents,
                onTextChange = {
                    error = if (!validation(it.text)) onValidationErrorMessage else null
                    textFieldContents = it
                },
                error = error,
                keyboardOptions = keyboardOptions,
                keyboardActions = KeyboardActions(onAny = {
                    if (validation(textFieldContents.text)) {
                        onSubmit(textFieldContents.text)
                    }
                }),
                maxLines = maxLines,
                modifier = Modifier.focusRequester(focusRequester),
            )
            canRequestFocus = true
        }
    }

    if (autoSelectOnDisplay && canRequestFocus) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}
