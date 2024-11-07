/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
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
fun TextField2(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    supportingText: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = ElementTheme.typography.fontBodyLgRegular.copy(
            color = if (readOnly) ElementTheme.colors.textSecondary else ElementTheme.colors.textPrimary
        ),
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
        Column {
            if (label != null) {
                Text(
                    text = label,
                    color = ElementTheme.colors.textPrimary,
                    style = ElementTheme.typography.fontBodyMdRegular,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                border = if (readOnly) {
                    null
                } else {
                    BorderStroke(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = when {
                            isError -> ElementTheme.colors.borderCriticalPrimary
                            isFocused -> ElementTheme.colors.borderInteractiveHovered
                            else -> ElementTheme.colors.borderInteractiveSecondary
                        }
                    )
                },
                color = if (readOnly) ElementTheme.colors.bgSubtleSecondary else ElementTheme.colors.bgCanvasDefault,
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    if (leadingIcon != null) {
                        leadingIcon()
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (placeholder != null && value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = ElementTheme.colors.textPlaceholder,
                                style = ElementTheme.typography.fontBodyLgRegular,
                            )
                        }
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        trailingIcon()
                    }
                }
            }
            if (supportingText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(4.dp),
                ) {
                    if (isError) {
                        Icon(
                            imageVector = CompoundIcons.Error(),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ElementTheme.colors.iconCriticalPrimary
                        )
                    }
                    Text(
                        text = supportingText,
                        color = if (isError) ElementTheme.colors.textCriticalPrimary else ElementTheme.colors.textSecondary,
                        style = ElementTheme.typography.fontBodySmRegular,
                    )
                }
            }
        }
    }
}

@Preview(group = PreviewGroup.TextFields)
@Composable
internal fun TextFields2LightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview(group = PreviewGroup.TextFields)
@Composable
internal fun TextFields2DarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
@ExcludeFromCoverage
private fun ContentToPreview() {
    Column(modifier = Modifier.padding(4.dp)) {
        allBooleans.forEach { isError ->
            allBooleans.forEach { enabled ->
                allBooleans.forEach { readonly ->
                    TextField2(
                        onValueChange = {},
                        label = "Label",
                        value = "Hello er=${isError.asInt()}, en=${enabled.asInt()}, ro=${readonly.asInt()}",
                        supportingText = "Supporting text",
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
