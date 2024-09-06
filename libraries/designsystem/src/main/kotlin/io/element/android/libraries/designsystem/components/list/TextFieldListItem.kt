/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.list

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.OutlinedTextField
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun TextFieldListItem(
    placeholder: String?,
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val textFieldStyle = ElementTheme.materialTypography.bodyLarge

    OutlinedTextField(
        value = text,
        onValueChange = { onTextChange(it) },
        placeholder = placeholder?.let { @Composable { Text(it) } },
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
        ),
        isError = error != null,
        supportingText = error?.let { @Composable { Text(it) } },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = textFieldStyle,
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
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val textFieldStyle = ElementTheme.materialTypography.bodyLarge

    OutlinedTextField(
        value = text,
        onValueChange = { onTextChange(it) },
        placeholder = placeholder?.let { @Composable { Text(it) } },
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
        ),
        isError = error != null,
        supportingText = error?.let { @Composable { Text(it) } },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = textFieldStyle,
        maxLines = maxLines,
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
