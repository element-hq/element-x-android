/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=1985-3223
 */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focusManager = LocalFocusManager.current
    val isFocused by interactionSource.collectIsFocusedAsState()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textFieldStyle(),
        singleLine = true,
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                focusManager.clearFocus()
            }
        ),
        cursorBrush = SolidColor(ElementTheme.colors.textActionAccent),
    ) { innerTextField ->
        DecorationBox(
            isFocused = isFocused,
            placeholder = placeholder,
            isTextEmpty = value.isEmpty(),
            innerTextField = innerTextField,
            onClear = { onValueChange("") },
        )
    }
}

@Composable
fun SearchField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focusManager = LocalFocusManager.current
    val isFocused by interactionSource.collectIsFocusedAsState()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textFieldStyle(),
        singleLine = true,
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                focusManager.clearFocus()
            }
        ),
        cursorBrush = SolidColor(ElementTheme.colors.textActionAccent),
    ) { innerTextField ->
        DecorationBox(
            isFocused = isFocused,
            placeholder = placeholder,
            isTextEmpty = value.text.isEmpty(),
            innerTextField = innerTextField,
            onClear = { TextFieldValue() }
        )
    }
}

@Composable
private fun DecorationBox(
    isFocused: Boolean,
    placeholder: String?,
    isTextEmpty: Boolean,
    onClear: () -> Unit,
    innerTextField: @Composable () -> Unit,
) {
    SearchFieldContainer(
        isFocused = isFocused,
    ) {
        Row(modifier = Modifier.padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
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
            Spacer(modifier = Modifier.width(16.dp))
            val showClearIcon = isFocused && !isTextEmpty
            IconButton(onClick = onClear, enabled = showClearIcon) {
                if (showClearIcon) {
                    Icon(
                        modifier = Modifier.background(ElementTheme.colors.iconSecondary, CircleShape),
                        imageVector = CompoundIcons.Close(),
                        contentDescription = stringResource(CommonStrings.action_clear),
                        tint = ElementTheme.colors.iconOnSolidPrimary,
                    )
                } else {
                    Icon(
                        imageVector = CompoundIcons.Search(),
                        contentDescription = null,
                        tint = ElementTheme.colors.iconTertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchFieldContainer(
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(99.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isFocused) {
                ElementTheme.colors.borderInteractiveHovered
            } else {
                ElementTheme.colors.borderInteractiveSecondary
            }
        ),
        color = ElementTheme.colors.bgSubtleSecondary,
        content = content
    )
}

@Composable
private fun textFieldStyle(): TextStyle {
    return ElementTheme.typography.fontBodyLgRegular.copy(
        color = ElementTheme.colors.textPrimary
    )
}

@Preview(group = PreviewGroup.Search, heightDp = 1000)
@Composable
internal fun SearchFieldsLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview(group = PreviewGroup.Search, heightDp = 1000)
@Composable
internal fun SearchFieldsDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
@ExcludeFromCoverage
private fun ContentToPreview() {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = spacedBy(8.dp)
    ) {
        SearchField(
            onValueChange = {},
            placeholder = "Search",
            value = "",
        )
        SearchField(
            onValueChange = {},
            placeholder = "Search",
            value = "Search term",
        )
    }
}
