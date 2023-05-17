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

package io.element.android.features.createroom.impl.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.features.createroom.impl.R
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField

@Composable
fun LabelledTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    maxLines: Int = 1,
    onValueChange: (String) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = label
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            placeholder = { Text(placeholder) },
            onValueChange = onValueChange,
            singleLine = maxLines == 1,
            maxLines = maxLines,
        )
    }
}

@Preview
@Composable
fun LabelledTextFieldLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
fun LabelledTextFieldDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Column {
        LabelledTextField(
            label = stringResource(R.string.screen_create_room_room_name_label),
            value = "",
            placeholder = stringResource(R.string.screen_create_room_room_name_placeholder),
        )
        LabelledTextField(
            label = stringResource(R.string.screen_create_room_room_name_label),
            value = "a room name",
            placeholder = stringResource(R.string.screen_create_room_room_name_placeholder),
        )
    }
}
