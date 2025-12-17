/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.utils.allBooleans
import io.element.android.libraries.designsystem.utils.asInt

@Composable
fun FilledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(
        unfocusedContainerColor = ElementTheme.colors.bgSubtleSecondary,
        focusedContainerColor = ElementTheme.colors.bgSubtleSecondary,
        disabledContainerColor = ElementTheme.colors.bgSubtleSecondary,
        errorContainerColor = ElementTheme.colors.bgSubtleSecondary,
    )
) {
    androidx.compose.material3.TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
    )
}

@Composable
fun FilledTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) {
    androidx.compose.material3.TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
    )
}

@Preview(group = PreviewGroup.TextFields)
@Composable
internal fun FilledTextFieldLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview(group = PreviewGroup.TextFields)
@Composable
internal fun FilledTextFieldDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@ExcludeFromCoverage
@Composable
private fun ContentToPreview() {
    Column(modifier = Modifier.padding(4.dp)) {
        allBooleans.forEach { isError ->
            allBooleans.forEach { enabled ->
                allBooleans.forEach { readonly ->
                    FilledTextField(
                        value = "Hello er=${isError.asInt()}, en=${enabled.asInt()}, ro=${readonly.asInt()}",
                        onValueChange = {},
                        label = { Text(text = "label") },
                        isError = isError,
                        enabled = enabled,
                        readOnly = readonly,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Preview(group = PreviewGroup.TextFields)
@Composable
internal fun FilledTextFieldValueLightPreview() =
    ElementPreviewLight { FilledTextFieldValueContentToPreview() }

@Preview(group = PreviewGroup.TextFields)
@Composable
internal fun FilledTextFieldValueTextFieldDarkPreview() =
    ElementPreviewDark { FilledTextFieldValueContentToPreview() }

@ExcludeFromCoverage
@Composable
private fun FilledTextFieldValueContentToPreview() {
    Column(modifier = Modifier.padding(4.dp)) {
        allBooleans.forEach { isError ->
            allBooleans.forEach { enabled ->
                allBooleans.forEach { readonly ->
                    FilledTextField(
                        value = TextFieldValue(
                            text = "Hello er=${isError.asInt()}, en=${enabled.asInt()}, ro=${readonly.asInt()}",
                            selection = TextRange(0, "Hello".length),
                        ),
                        onValueChange = {},
                        label = { Text(text = "label") },
                        isError = isError,
                        enabled = enabled,
                        readOnly = readonly,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}
