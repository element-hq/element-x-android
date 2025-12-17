/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.list

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TextFieldValidity

@Composable
fun TextFieldListItem(
    placeholder: String?,
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    minLines: Int = 1,
    maxLines: Int = minLines,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    TextField(
        value = text,
        onValueChange = { onTextChange(it) },
        placeholder = placeholder,
        label = label,
        validity = if (error != null) TextFieldValidity.Invalid else TextFieldValidity.None,
        supportingText = error,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        maxLines = maxLines,
        singleLine = maxLines == 1,
        modifier = modifier,
    )
}

@Composable
fun TextFieldListItem(
    placeholder: String?,
    text: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    minLines: Int = 1,
    maxLines: Int = minLines,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    TextField(
        value = text,
        onValueChange = { onTextChange(it) },
        placeholder = placeholder,
        label = label,
        validity = if (error != null) TextFieldValidity.Invalid else TextFieldValidity.None,
        supportingText = error,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        maxLines = maxLines,
        minLines = minLines,
        singleLine = maxLines == 1,
        modifier = modifier,
    )
}

@Preview("Text field List item - empty", group = PreviewGroup.ListItems)
@Composable
internal fun TextFieldListItemEmptyPreview() {
    ElementThemedPreview {
        TextFieldListItem(
            placeholder = "Placeholder",
            text = "",
            onTextChange = {},
        )
    }
}

@Preview("Text field List item - text", group = PreviewGroup.ListItems)
@Composable
internal fun TextFieldListItemPreview() {
    ElementThemedPreview {
        TextFieldListItem(
            placeholder = "Placeholder",
            text = "Text",
            onTextChange = {},
        )
    }
}

@Preview("Text field List item - textfieldvalue", group = PreviewGroup.ListItems)
@Composable
internal fun TextFieldListItemTextFieldValuePreview() {
    ElementThemedPreview {
        TextFieldListItem(
            placeholder = "Placeholder",
            text = TextFieldValue("Text field value"),
            onTextChange = {},
        )
    }
}
