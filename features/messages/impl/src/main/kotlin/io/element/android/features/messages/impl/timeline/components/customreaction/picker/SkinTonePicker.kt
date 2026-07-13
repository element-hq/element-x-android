/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojiSkin
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

val SkinToneSlotSize = 48.dp
val SkinToneSlotSpacing = 2.dp
val SkinTonePadding = 4.dp

@Composable
fun SkinTonePicker(
    emoji: Emoji,
    hoveredIndex: Int,
    selectedUnicodes: ImmutableSet<String>,
    onSelect: (Emoji) -> Unit,
    modifier: Modifier = Modifier,
) {
    val skins = emoji.skins.orEmpty()
    val emojiSize = 32.dp.toSp()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ElementTheme.colors.bgCanvasDefault,
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(SkinTonePadding),
            horizontalArrangement = Arrangement.spacedBy(SkinToneSlotSpacing),
        ) {
            SkinToneSlot(
                unicode = emoji.unicode,
                emojiSize = emojiSize,
                isHovered = hoveredIndex == 0,
                isSelected = emoji.unicode in selectedUnicodes,
                onClick = { onSelect(emoji) },
            )
            skins.forEachIndexed { index, skin ->
                SkinToneSlot(
                    unicode = skin.unicode,
                    emojiSize = emojiSize,
                    isHovered = hoveredIndex == index + 1,
                    isSelected = skin.unicode in selectedUnicodes,
                    onClick = { onSelect(emoji.copy(unicode = skin.unicode)) },
                )
            }
        }
    }
}

@Composable
private fun SkinToneSlot(
    unicode: String,
    emojiSize: TextUnit,
    isHovered: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val background = when {
        isSelected && isHovered -> ElementTheme.colors.bgActionPrimaryPressed
        isSelected -> ElementTheme.colors.bgActionPrimaryRest
        isHovered -> ElementTheme.colors.bgActionPrimaryHovered
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .size(SkinToneSlotSize)
            .clip(CircleShape)
            .background(background, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = unicode,
            style = LocalTextStyle.current.copy(fontSize = emojiSize),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SkinTonePickerPreview() = ElementPreview {
    SkinTonePicker(
        emoji = Emoji(
            hexcode = "1F44D",
            label = "thumbs up",
            tags = null,
            shortcodes = persistentListOf("+1", "thumbsup"),
            unicode = "👍",
            skins = aSkinList(),
        ),
        hoveredIndex = -1,
        selectedUnicodes = persistentSetOf(),
        onSelect = {},
    )
}

internal fun aSkinList() = persistentListOf(
    EmojiSkin("1F44D-1F3FB", "thumbs up: light skin tone", "👍🏻"),
    EmojiSkin("1F44D-1F3FC", "thumbs up: medium-light skin tone", "👍🏼"),
    EmojiSkin("1F44D-1F3FD", "thumbs up: medium skin tone", "👍🏽"),
    EmojiSkin("1F44D-1F3FE", "thumbs up: medium-dark skin tone", "👍🏾"),
    EmojiSkin("1F44D-1F3FF", "thumbs up: dark skin tone", "👍🏿"),
)
