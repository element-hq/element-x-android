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

package io.element.android.features.messages.impl.timeline.components.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.theme.ElementTheme

private val CORNER_RADIUS = 8.dp

@Composable
fun GroupHeaderView(
    text: String,
    isExpanded: Boolean,
    @Suppress("UNUSED_PARAMETER") isHighlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Ignore isHighlighted for now, we need a design decision on it.
    val backgroundColor = Color.Companion.Transparent
    val shape = RoundedCornerShape(CORNER_RADIUS)

    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .clip(shape)
                .clickable(onClick = onClick),
            color = backgroundColor,
            shape = shape,
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.secondary,
                    style = ElementTheme.typography.fontBodyMdRegular,
                )
                Icon(
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                    resourceId = CommonDrawables.ic_compound_chevron_down,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun GroupHeaderViewPreview() = ElementPreview {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        GroupHeaderView(
            text = "8 room changes (expanded)",
            isExpanded = true,
            isHighlighted = false,
            onClick = {}
        )
        GroupHeaderView(
            text = "8 room changes (not expanded)",
            isExpanded = false,
            isHighlighted = false,
            onClick = {}
        )
        GroupHeaderView(
            text = "8 room changes (expanded/h)",
            isExpanded = true,
            isHighlighted = true,
            onClick = {}
        )
        GroupHeaderView(
            text = "8 room changes (not expanded/h)",
            isExpanded = false,
            isHighlighted = true,
            onClick = {}
        )
    }
}
