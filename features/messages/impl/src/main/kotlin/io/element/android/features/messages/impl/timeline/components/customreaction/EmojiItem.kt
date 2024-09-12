/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.ElementTheme
import io.element.android.emojibasebindings.Emoji
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun EmojiItem(
    item: Emoji,
    isSelected: Boolean,
    onSelectEmoji: (Emoji) -> Unit,
    modifier: Modifier = Modifier,
    emojiSize: TextUnit = 20.sp,
) {
    val backgroundColor = if (isSelected) {
        ElementTheme.colors.bgActionPrimaryRest
    } else {
        Color.Transparent
    }
    val description = if (isSelected) {
        stringResource(id = CommonStrings.a11y_remove_reaction_with, item.unicode)
    } else {
        stringResource(id = CommonStrings.a11y_react_with, item.unicode)
    }
    Box(
        modifier = modifier
            .sizeIn(minWidth = 40.dp, minHeight = 40.dp)
            .background(backgroundColor, CircleShape)
            .clickable(
                enabled = true,
                onClick = { onSelectEmoji(item) },
                indication = ripple(bounded = false, radius = emojiSize.toDp() / 2 + 10.dp),
                interactionSource = remember { MutableInteractionSource() }
            )
            .clearAndSetSemantics {
                contentDescription = description
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.unicode,
            style = LocalTextStyle.current.copy(fontSize = emojiSize),
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
                onSelectEmoji = {},
            )
        }
    }
}
