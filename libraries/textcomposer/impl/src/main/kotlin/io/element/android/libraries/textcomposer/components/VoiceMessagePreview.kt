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

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.theme.ElementTheme

@Composable
internal fun VoiceMessagePreview(
    isInteractive: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = ElementTheme.colors.bgSubtleSecondary,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(start = 12.dp, end = 20.dp, top = 8.dp, bottom = 8.dp)
            .heightIn(26.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // TODO Replace with recording preview UI
        Text(
            text = "Finished recording", // Not localized because it is a placeholder
            color = if (isInteractive) {
                ElementTheme.colors.textSecondary
            } else {
                ElementTheme.colors.textDisabled
            },
            style = ElementTheme.typography.fontBodySmMedium
        )
    }
}

@PreviewsDayNight
@Composable
internal fun VoiceMessagePreviewPreview() = ElementPreview {
    Column {
        VoiceMessagePreview(isInteractive = true)
        VoiceMessagePreview(isInteractive = false)
    }
}
