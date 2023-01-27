/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.ElementGreen
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementOutlinedTextField(
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
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.outlinedShape,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = ElementTheme.colors.primary,
        disabledTextColor = ElementTheme.colors.primary.copy(alpha = 0.38f),
        containerColor = Color.Transparent,
        cursorColor = ElementTheme.colors.primary,
        errorCursorColor = ElementTheme.colors.error,
        selectionColors = TextSelectionColors(
            handleColor = ElementGreen,
            backgroundColor = ElementGreen.copy(alpha = 0.4f)
        ),
        focusedBorderColor = ElementTheme.colors.primary,
        unfocusedBorderColor = ElementTheme.colors.secondary,
        disabledBorderColor = ElementTheme.colors.secondary.copy(alpha = 0.12f),
        errorBorderColor = ElementTheme.colors.error,
        focusedLeadingIconColor = ElementTheme.colors.primary,
        unfocusedLeadingIconColor = ElementTheme.colors.secondary,
        disabledLeadingIconColor = ElementTheme.colors.secondary.copy(0.12f),
        errorLeadingIconColor = ElementTheme.colors.error,
        focusedTrailingIconColor = ElementTheme.colors.primary,
        unfocusedTrailingIconColor = ElementTheme.colors.secondary,
        disabledTrailingIconColor = ElementTheme.colors.secondary.copy(alpha = 0.12f),
        errorTrailingIconColor = ElementTheme.colors.error,
        focusedLabelColor = ElementTheme.colors.primary,
        unfocusedLabelColor = ElementTheme.colors.secondary,
        disabledLabelColor = ElementTheme.colors.secondary.copy(alpha = 0.12f),
        errorLabelColor = ElementTheme.colors.error,
        placeholderColor = ElementTheme.colors.secondary,
        disabledPlaceholderColor = ElementTheme.colors.secondary.copy(alpha = 0.12f),
        focusedSupportingTextColor = ElementTheme.colors.primary,
        unfocusedSupportingTextColor = ElementTheme.colors.secondary,
        disabledSupportingTextColor = ElementTheme.colors.primary.copy(alpha = 0.12f),
        errorSupportingTextColor = ElementTheme.colors.error,
    )
) {
    OutlinedTextField(
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

@Preview
@Composable
fun ElementOutlinedTextFieldsLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
fun ElementOutlinedTextFieldsDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Column {
        ElementOutlinedTextField(onValueChange = {}, value = "Content", isError = false, enabled = true, readOnly = true)
        ElementOutlinedTextField(onValueChange = {}, value = "Content", isError = false, enabled = true, readOnly = false)
        ElementOutlinedTextField(onValueChange = {}, value = "Content", isError = false, enabled = false, readOnly = true)
        ElementOutlinedTextField(onValueChange = {}, value = "Content", isError = false, enabled = false, readOnly = false)
        ElementOutlinedTextField(onValueChange = {}, value = "Content", isError = true, enabled = true, readOnly = true)
        ElementOutlinedTextField(onValueChange = {}, value = "Content", isError = true, enabled = true, readOnly = false)
        ElementOutlinedTextField(onValueChange = {}, value = "Content", isError = true, enabled = false, readOnly = true)
        ElementOutlinedTextField(onValueChange = {}, value = "Content", isError = true, enabled = false, readOnly = false)
    }
}
