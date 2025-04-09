/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.element.android.compound.annotations.CoreColorToken
import io.element.android.compound.previews.ColorListPreview
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.compound.tokens.generated.internal.DarkColorTokens
import io.element.android.compound.tokens.generated.internal.LightColorTokens
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import kotlinx.collections.immutable.persistentMapOf

/**
 * Room list.
 */
@Composable
fun ElementTheme.roomListRoomName() = colors.textPrimary

@Composable
fun ElementTheme.roomListRoomMessage() = colors.textSecondary

@Composable
fun ElementTheme.roomListRoomMessageDate() = colors.textSecondary

val SemanticColors.unreadIndicator
    get() = iconAccentTertiary

val SemanticColors.placeholderBackground
    get() = bgSubtleSecondary

// This color is not present in Semantic color, so put hard-coded value for now
@OptIn(CoreColorToken::class)
val SemanticColors.messageFromMeBackground
    get() = if (isLight) LightColorTokens.colorGray400 else DarkColorTokens.colorGray500

// This color is not present in Semantic color, so put hard-coded value for now
@OptIn(CoreColorToken::class)
val SemanticColors.messageFromOtherBackground
    get() = if (isLight) LightColorTokens.colorGray300 else DarkColorTokens.colorGray400

// This color is not present in Semantic color, so put hard-coded value for now
@OptIn(CoreColorToken::class)
val SemanticColors.progressIndicatorTrackColor
    get() = if (isLight) LightColorTokens.colorAlphaGray500 else DarkColorTokens.colorAlphaGray500

// This color is not present in Semantic color, so put hard-coded value for now
@OptIn(CoreColorToken::class)
val SemanticColors.iconSuccessPrimaryBackground
    get() = if (isLight) LightColorTokens.colorGreen300 else DarkColorTokens.colorGreen300

// This color is not present in Semantic color, so put hard-coded value for now
@OptIn(CoreColorToken::class)
val SemanticColors.bgSubtleTertiary
    get() = if (isLight) LightColorTokens.colorGray100 else DarkColorTokens.colorGray100

// Temporary color, which is not in the token right now
val SemanticColors.temporaryColorBgSpecial
    get() = if (isLight) Color(0xFFE4E8F0) else Color(0xFF3A4048)

// This color is not present in Semantic color, so put hard-coded value for now
@OptIn(CoreColorToken::class)
val SemanticColors.pinDigitBg
    get() = if (isLight) LightColorTokens.colorGray300 else DarkColorTokens.colorGray400

@OptIn(CoreColorToken::class)
val SemanticColors.currentUserMentionPillText
    get() = if (isLight) LightColorTokens.colorGreen1100 else DarkColorTokens.colorGreen1100

val SemanticColors.currentUserMentionPillBackground
    get() = if (isLight) {
        // We want LightDesignTokens.colorGreenAlpha400
        Color(0x3b07b661)
    } else {
        // We want DarkDesignTokens.colorGreenAlpha500
        Color(0xff003d29)
    }

val SemanticColors.mentionPillText
    get() = textPrimary

val SemanticColors.mentionPillBackground
    get() = if (isLight) {
        // We want LightDesignTokens.colorGray400
        Color(0x1f052e61)
    } else {
        // We want DarkDesignTokens.colorGray500
        Color(0x26f4f7fa)
    }

@OptIn(CoreColorToken::class)
val SemanticColors.bigCheckmarkBorderColor
    get() = if (isLight) LightColorTokens.colorGray400 else DarkColorTokens.colorGray400

@OptIn(CoreColorToken::class)
val SemanticColors.highlightedMessageBackgroundColor
    get() = if (isLight) LightColorTokens.colorGreen300 else DarkColorTokens.colorGreen300

// Badge colors

@OptIn(CoreColorToken::class)
val SemanticColors.badgeNeutralBackgroundColor
    get() = if (isLight) LightColorTokens.colorAlphaGray300 else DarkColorTokens.colorAlphaGray300

@OptIn(CoreColorToken::class)
val SemanticColors.badgeNeutralContentColor
    get() = if (isLight) LightColorTokens.colorGray1100 else DarkColorTokens.colorGray1100

@OptIn(CoreColorToken::class)
val SemanticColors.badgeNegativeBackgroundColor
    get() = if (isLight) LightColorTokens.colorAlphaRed300 else DarkColorTokens.colorAlphaRed300

@OptIn(CoreColorToken::class)
val SemanticColors.badgeNegativeContentColor
    get() = if (isLight) LightColorTokens.colorRed1100 else DarkColorTokens.colorRed1100

@OptIn(CoreColorToken::class)
val SemanticColors.pinnedMessageBannerIndicator
    get() = if (isLight) LightColorTokens.colorAlphaGray600 else DarkColorTokens.colorAlphaGray600

@OptIn(CoreColorToken::class)
val SemanticColors.pinnedMessageBannerBorder
    get() = if (isLight) LightColorTokens.colorAlphaGray400 else DarkColorTokens.colorAlphaGray400

@PreviewsDayNight
@Composable
internal fun ColorAliasesPreview() = ElementPreview {
    ColorListPreview(
        backgroundColor = Color.Black,
        foregroundColor = Color.White,
        colors = persistentMapOf(
            "roomListRoomName" to ElementTheme.roomListRoomName(),
            "roomListRoomMessage" to ElementTheme.roomListRoomMessage(),
            "roomListRoomMessageDate" to ElementTheme.roomListRoomMessageDate(),
            "unreadIndicator" to ElementTheme.colors.unreadIndicator,
            "placeholderBackground" to ElementTheme.colors.placeholderBackground,
            "messageFromMeBackground" to ElementTheme.colors.messageFromMeBackground,
            "messageFromOtherBackground" to ElementTheme.colors.messageFromOtherBackground,
            "progressIndicatorTrackColor" to ElementTheme.colors.progressIndicatorTrackColor,
            "temporaryColorBgSpecial" to ElementTheme.colors.temporaryColorBgSpecial,
            "iconSuccessPrimaryBackground" to ElementTheme.colors.iconSuccessPrimaryBackground,
            "bigCheckmarkBorderColor" to ElementTheme.colors.bigCheckmarkBorderColor,
            "highlightedMessageBackgroundColor" to ElementTheme.colors.highlightedMessageBackgroundColor,
        )
    )
}
