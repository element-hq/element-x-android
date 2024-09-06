/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.preferences

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
import io.element.android.libraries.designsystem.components.dialogs.ListDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.list.TextFieldListItem
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun PreferenceTextField(
    headline: String,
    onChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    value: String? = null,
    supportingText: String? = null,
    displayValue: (String?) -> Boolean = { !it.isNullOrBlank() },
    trailingContent: ListItemContent? = null,
    validation: (String?) -> Boolean = { true },
    onValidationErrorMessage: String? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    style: ListItemStyle = ListItemStyle.Default,
) {
    var displayTextFieldDialog by rememberSaveable { mutableStateOf(false) }
    val valueToDisplay = if (displayValue(value)) value else supportingText

    ListItem(
        modifier = modifier,
        headlineContent = { Text(headline) },
        supportingContent = valueToDisplay?.let { @Composable { Text(it) } },
        trailingContent = trailingContent,
        style = style,
        enabled = enabled,
        onClick = { displayTextFieldDialog = true }
    )

    if (displayTextFieldDialog) {
        TextFieldDialog(
            title = headline,
            onSubmit = {
                onChange(it.takeIf { it.isNotBlank() })
                displayTextFieldDialog = false
            },
            onDismissRequest = { displayTextFieldDialog = false },
            placeholder = placeholder.orEmpty(),
            value = value.orEmpty(),
            validation = validation,
            onValidationErrorMessage = onValidationErrorMessage,
            keyboardOptions = keyboardOptions,
        )
    }
}

@Composable
private fun TextFieldDialog(
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
        mutableStateOf(TextFieldValue(value.orEmpty(), selection = TextRange(value.orEmpty().length)))
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
