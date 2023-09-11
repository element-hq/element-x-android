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

package io.element.android.libraries.designsystem.components.list

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.theme.ElementTheme

@Composable
fun TextFieldListItem(
    placeholder: String,
    text: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textFieldStyle = ElementTheme.materialTypography.bodyLarge

    TextField(
        value = text,
        onValueChange = onTextChanged,
        placeholder = { Text(placeholder) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        textStyle = textFieldStyle,
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
            onTextChanged = {},
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
            onTextChanged = {},
        )
    }
}
