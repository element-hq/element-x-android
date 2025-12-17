/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.preferences

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.element.android.libraries.designsystem.components.dialogs.TextFieldDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
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
