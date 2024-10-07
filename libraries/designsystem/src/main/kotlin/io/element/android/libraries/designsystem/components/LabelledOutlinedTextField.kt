/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.OutlinedTextField
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun LabelledOutlinedTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = MaterialTheme.colorScheme.primary,
            text = label
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            placeholder = placeholder?.let { { Text(placeholder) } },
            onValueChange = onValueChange,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun LabelledOutlinedTextFieldPreview() = ElementPreview {
    Column {
        LabelledOutlinedTextField(
            label = "Room name",
            value = "",
            onValueChange = {},
            placeholder = "e.g. Product Sprint",
        )
        LabelledOutlinedTextField(
            label = "Room name",
            value = "a room name",
            onValueChange = {},
            placeholder = "e.g. Product Sprint",
        )
    }
}
