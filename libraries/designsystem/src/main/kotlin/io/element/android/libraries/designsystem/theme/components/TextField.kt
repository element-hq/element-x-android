/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.utils.allBooleans
import io.element.android.libraries.designsystem.utils.asInt

/**
 * https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=2008-37137
 */
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    supportingText: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    validity: TextFieldValidity = TextFieldValidity.None,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textFieldStyle(enabled),
        interactionSource = interactionSource,
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        readOnly = readOnly,
        cursorBrush = SolidColor(ElementTheme.colors.textPrimary),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        onTextLayout = onTextLayout,
    ) { innerTextField ->
        DecorationBox(
            label = label,
            readOnly = readOnly,
            enabled = enabled,
            isFocused = isFocused,
            validity = validity,
            leadingIcon = leadingIcon,
            placeholder = placeholder,
            isTextEmpty = value.isEmpty(),
            innerTextField = innerTextField,
            trailingIcon = trailingIcon,
            supportingText = supportingText
        )
    }
}

@Composable
fun TextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    supportingText: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    validity: TextFieldValidity? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textFieldStyle(enabled),
        interactionSource = interactionSource,
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        readOnly = readOnly,
        cursorBrush = SolidColor(ElementTheme.colors.textPrimary),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        onTextLayout = onTextLayout,
    ) { innerTextField ->
        DecorationBox(
            label = label,
            readOnly = readOnly,
            enabled = enabled,
            isFocused = isFocused,
            validity = validity,
            leadingIcon = leadingIcon,
            placeholder = placeholder,
            isTextEmpty = value.text.isEmpty(),
            innerTextField = innerTextField,
            trailingIcon = trailingIcon,
            supportingText = supportingText
        )
    }
}

@Composable
private fun DecorationBox(
    label: String?,
    enabled: Boolean,
    readOnly: Boolean,
    isFocused: Boolean,
    validity: TextFieldValidity?,
    placeholder: String?,
    isTextEmpty: Boolean,
    supportingText: String?,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    innerTextField: @Composable () -> Unit,
) {
    Column {
        if (label != null) {
            Text(
                text = label,
                color = ElementTheme.colors.textPrimary,
                style = ElementTheme.typography.fontBodyMdRegular,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        TextFieldContainer(
            enabled = enabled,
            readOnly = readOnly,
            isFocused = isFocused,
            isError = validity == TextFieldValidity.Invalid
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                if (leadingIcon != null) {
                    CompositionLocalProvider(LocalContentColor provides ElementTheme.colors.iconSecondary) {
                        leadingIcon()
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (placeholder != null && isTextEmpty) {
                        Text(
                            text = placeholder,
                            color = ElementTheme.colors.textSecondary,
                            style = ElementTheme.typography.fontBodyLgRegular,
                        )
                    }
                    innerTextField()
                }
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CompositionLocalProvider(LocalContentColor provides ElementTheme.colors.iconSecondary) {
                        trailingIcon()
                    }
                }
            }
        }
        if (supportingText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            SupportingTextLayout(validity, supportingText)
        }
    }
}

@Composable
private fun TextFieldContainer(
    enabled: Boolean,
    readOnly: Boolean,
    isFocused: Boolean,
    isError: Boolean,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        border = if (readOnly) {
            null
        } else {
            BorderStroke(
                width = if (isFocused) 2.dp else 1.dp,
                color = when {
                    !enabled -> ElementTheme.colors.borderDisabled
                    isError -> ElementTheme.colors.borderCriticalPrimary
                    isFocused -> ElementTheme.colors.borderInteractiveHovered
                    else -> ElementTheme.colors.borderInteractiveSecondary
                }
            )
        },
        color = when {
            readOnly -> ElementTheme.colors.bgSubtleSecondary
            !enabled -> ElementTheme.colors.bgCanvasDisabled
            else -> ElementTheme.colors.bgCanvasDefault
        },
        content = content
    )
}

@Composable
private fun SupportingTextLayout(validity: TextFieldValidity?, supportingText: String) {
    Row(horizontalArrangement = spacedBy(4.dp)) {
        when (validity) {
            TextFieldValidity.Invalid -> {
                Icon(
                    imageVector = CompoundIcons.ErrorSolid(),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = ElementTheme.colors.iconCriticalPrimary
                )
            }
            TextFieldValidity.Valid -> {
                Icon(
                    imageVector = CompoundIcons.CheckCircleSolid(),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = ElementTheme.colors.iconSuccessPrimary
                )
            }
            else -> Unit
        }
        Text(
            text = supportingText,
            color = when (validity) {
                TextFieldValidity.Invalid -> ElementTheme.colors.textCriticalPrimary
                TextFieldValidity.Valid -> ElementTheme.colors.textSuccessPrimary
                else -> ElementTheme.colors.textSecondary
            },
            style = ElementTheme.typography.fontBodySmRegular,
        )
    }
}

enum class TextFieldValidity {
    None,
    Invalid,
    Valid
}

@Composable
private fun textFieldStyle(enabled: Boolean): TextStyle {
    return ElementTheme.typography.fontBodyLgRegular.copy(
        color = if (enabled) {
            ElementTheme.colors.textPrimary
        } else {
            ElementTheme.colors.textSecondary
        }
    )
}

@Preview(group = PreviewGroup.TextFields, heightDp = 1000)
@Composable
internal fun TextFieldsLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview(group = PreviewGroup.TextFields, heightDp = 1000)
@Composable
internal fun TextFieldsDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
@ExcludeFromCoverage
private fun ContentToPreview() {
    Column(modifier = Modifier.padding(4.dp)) {
        TextFieldValidity.entries.forEach { validity ->
            allBooleans.forEach { enabled ->
                allBooleans.forEach { readonly ->
                    TextField(
                        onValueChange = {},
                        label = "Label",
                        value = "Hello val=$validity, en=${enabled.asInt()}, ro=${readonly.asInt()}",
                        supportingText = "Supporting text",
                        validity = validity,
                        enabled = enabled,
                        readOnly = readonly,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}
