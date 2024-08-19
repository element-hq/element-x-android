/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.atomic.molecules

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun TimelineHeaderMolecule(
    text: String,
    textColor: Color,
    modifier: Modifier = Modifier,
    separatorColor: Color = textColor,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = spacedBy(4.dp),
    ) {
        Text(
            text = text.uppercase(),
            style = ElementTheme.typography.fontBodySmMedium,
            color = textColor,
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            color = separatorColor,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineHeaderMoleculePreview() = ElementPreview {
    TimelineHeaderMolecule(
        text = "A title",
        textColor = ElementTheme.colors.textSecondary,
        separatorColor = ElementTheme.colors.borderInteractivePrimary,
    )
}
