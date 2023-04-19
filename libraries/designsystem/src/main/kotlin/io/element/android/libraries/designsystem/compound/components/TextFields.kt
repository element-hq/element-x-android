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

package io.element.android.libraries.designsystem.compound.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.compound.CompoundTheme
import io.element.android.libraries.designsystem.compound.LocalCompoundColors
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompoundOutlinedTextField(
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
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.outlinedShape,
    colors: TextFieldColors = CompoundTextFieldDefaults.outlinedTextFieldColors()
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
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompoundOutlinedTextField(
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
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.outlinedShape,
    colors: TextFieldColors = CompoundTextFieldDefaults.outlinedTextFieldColors()
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
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
    )
}

object CompoundTextFieldDefaults {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun outlinedTextFieldColors(
        containerColor: Color = Color.Transparent,
        textColor: Color = MaterialTheme.colorScheme.primary,
        disabledTextColor: Color = LocalCompoundColors.current.textDisabled.withDisabledAlpha(),
        placeholderColor: Color = MaterialTheme.colorScheme.secondary,
        disabledPlaceholderColor: Color = disabledTextColor,
        unfocusedLeadingIconColor: Color = MaterialTheme.colorScheme.secondary,
        focusedLeadingIconColor: Color = unfocusedLeadingIconColor,
        disabledLeadingIconColor: Color = disabledTextColor,
        errorLeadingIconColor: Color = MaterialTheme.colorScheme.error,
        unfocusedTrailingIconColor: Color = MaterialTheme.colorScheme.secondary,
        focusedTrailingIconColor: Color = unfocusedTrailingIconColor,
        disabledTrailingIconColor: Color = disabledTextColor,
        errorTrailingIconColor: Color = MaterialTheme.colorScheme.error,
        unfocusedBorderColor: Color = MaterialTheme.colorScheme.outline,
        focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
        disabledBorderColor: Color = disabledTextColor,
        errorBorderColor: Color = MaterialTheme.colorScheme.error,
        errorSupportingTextColor: Color = MaterialTheme.colorScheme.error,
    ) = TextFieldDefaults.outlinedTextFieldColors(
        containerColor = containerColor,
        // Input text colors
        textColor = textColor,
        disabledTextColor = disabledTextColor,
        // Placeholder text colors
        placeholderColor = placeholderColor,
        disabledPlaceholderColor = disabledPlaceholderColor,
        // Leading icon colors
        unfocusedLeadingIconColor = unfocusedLeadingIconColor,
        focusedLeadingIconColor = focusedLeadingIconColor,
        disabledLeadingIconColor = disabledLeadingIconColor,
        errorLeadingIconColor = errorLeadingIconColor,
        // Trailing icon colors
        unfocusedTrailingIconColor = unfocusedTrailingIconColor,
        focusedTrailingIconColor = focusedTrailingIconColor,
        disabledTrailingIconColor = disabledTrailingIconColor,
        errorTrailingIconColor = errorTrailingIconColor,
        // Border colors
        unfocusedBorderColor = unfocusedBorderColor,
        focusedBorderColor = focusedBorderColor,
        disabledBorderColor = disabledBorderColor,
        errorBorderColor = errorBorderColor,
        // Supporting text colors
        errorSupportingTextColor = errorSupportingTextColor,
    )
}

fun Color.withDisabledAlpha() = this.copy(alpha = 0.38f)

@Preview
@Composable
internal fun PreviewOutlinedTextFieldLight() {
    CompoundTheme(darkTheme = false) {
        Surface {
            CompoundOutlinedTextField(
                modifier = Modifier.padding(10.dp),
                value = "Some input",
                onValueChange = {},
                supportingText = { Text("A support text") },
                label = { Text("Title") },
                leadingIcon = { Icon(Icons.Outlined.Person, "") },
                trailingIcon = { Icon(Icons.Outlined.Cancel, "") }
            )
        }
    }
}

@Preview
@Composable
internal fun PreviewOutlinedTextFieldDisabledLight() {
    CompoundTheme(darkTheme = false) {
        Surface {
            CompoundOutlinedTextField(
                modifier = Modifier.padding(10.dp),
                value = "Some input",
                enabled = false,
                onValueChange = {},
                supportingText = { Text("A support text") },
                label = { Text("Title") },
                leadingIcon = { Icon(Icons.Outlined.Person, "") },
                trailingIcon = { Icon(Icons.Outlined.Cancel, "") }
            )
        }
    }
}

@Preview
@Composable
internal fun PreviewOutlinedTextFieldErrorLight() {
    CompoundTheme(darkTheme = false) {
        Surface {
            CompoundOutlinedTextField(
                modifier = Modifier.padding(10.dp),
                value = "Some input",
                isError = true,
                onValueChange = {},
                supportingText = { Text("There was an error!") },
                label = { Text("Title") },
                leadingIcon = { Icon(Icons.Outlined.Person, "") },
                trailingIcon = { Icon(Icons.Outlined.Cancel, "") }
            )
        }
    }
}

@Preview
@Composable
internal fun PreviewOutlinedTextFieldPlaceholderLight() {
    CompoundTheme(darkTheme = false) {
        Surface {
            CompoundOutlinedTextField(
                modifier = Modifier.padding(10.dp),
                value = "",
                onValueChange = {},
                supportingText = { Text("A support text") },
                label = { Text("A placeholder") },
                leadingIcon = { Icon(Icons.Outlined.Person, "") },
                trailingIcon = { Icon(Icons.Outlined.Cancel, "") }
            )
        }
    }
}
