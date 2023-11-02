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

package io.element.android.features.messages.impl.timeline.components.customreaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.element.android.emojibasebindings.Emoji
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.theme.ElementTheme

@Composable
fun EmojiItem(
    item: Emoji,
    isSelected: Boolean,
    onEmojiSelected: (Emoji) -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isSelected) {
        ElementTheme.colors.bgActionPrimaryRest
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .background(backgroundColor, CircleShape)
            .clickable(
                enabled = true,
                onClick = { onEmojiSelected(item) },
                indication = rememberRipple(bounded = false, radius = 20.dp),
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.unicode,
            style = ElementTheme.typography.fontHeadingSmRegular,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun EmojiItemPreview() = ElementPreview {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (isSelected in listOf(true, false)) {
            EmojiItem(
                item = Emoji(
                    hexcode = "",
                    label = "",
                    tags = null,
                    shortcodes = emptyList(),
                    unicode = "👍",
                    skins = null
                ),
                isSelected = isSelected,
                onEmojiSelected = {},
            )
        }
    }
}
