/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.list.TextFieldListItem
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun TextFieldDialog(
    title: String,
    onSubmit: (String) -> Unit,
    onDismissRequest: () -> Unit,
    value: String?,
    placeholder: String?,
    modifier: Modifier = Modifier,
    validation: (String?) -> Boolean = { true },
    onValidationErrorMessage: String? = null,
    autoSelectOnDisplay: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = minLines,
    content: String? = null,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    submitText: String = stringResource(CommonStrings.action_ok),
    destructiveSubmit: Boolean = false,
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
    var error by rememberSaveable { mutableStateOf(if (!validation(value.orEmpty())) onValidationErrorMessage else null) }
    var canRequestFocus by rememberSaveable { mutableStateOf(false) }
    val canSubmit by remember { derivedStateOf { validation(textFieldContents.text) } }
    ListDialog(
        title = title,
        onSubmit = { onSubmit(textFieldContents.text) },
        onDismissRequest = onDismissRequest,
        enabled = canSubmit,
        submitText = submitText,
        destructiveSubmit = destructiveSubmit,
        modifier = modifier,
    ) {
        if (content != null) {
            item {
                Text(
                    text = content,
                    style = ElementTheme.materialTypography.bodyMedium,
                )
            }
        }
        item {
            TextFieldListItem(
                placeholder = placeholder.orEmpty(),
                label = label,
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
                minLines = minLines,
                maxLines = maxLines,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
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

@PreviewsDayNight
@Composable
internal fun TextFieldDialogPreview() = ElementPreview {
    TextFieldDialog(
        title = "Title",
        value = "",
        placeholder = "Placeholder",
        onSubmit = {},
        onDismissRequest = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TextFieldDialogWithErrorPreview() = ElementPreview {
    TextFieldDialog(
        title = "Title",
        content = "Some content",
        onSubmit = {},
        validation = { false },
        onDismissRequest = {},
        value = "Value",
        placeholder = "Placeholder",
        label = "Label",
        onValidationErrorMessage = "Error message",
    )
}
