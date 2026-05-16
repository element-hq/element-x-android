/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
val SemanticColors.roomListRoomName
    get() = textPrimary

val SemanticColors.roomListRoomMessage
    get() = textSecondary

val SemanticColors.roomListRoomMessageDate
    get() = textSecondary

val SemanticColors.unreadIndicator
    get() = iconAccentTertiary

val SemanticColors.placeholderBackground
    get() = bgSubtleSecondary

// Alpha demo: WeChat-style bubble colours. Own messages get the classic
// light-green WeChat bubble; messages from others go on a near-white bubble in
// light mode and a deep grey in dark mode, the way WeChat handles dark theme.
val SemanticColors.messageFromMeBackground
    get() = if (isLight) Color(0xff95ec69) else Color(0xff5da34c)

val SemanticColors.messageFromOtherBackground
    get() = if (isLight) Color(0xffffffff) else Color(0xff2a2a2a)

// This color is not present in Semantic color, so put hard-coded value for now
@OptIn(CoreColorToken::class)
val SemanticColors.progressIndicatorTrackColor
    get() = if (isLight) LightColorTokens.colorAlphaGray500 else DarkColorTokens.colorAlphaGray500

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
val SemanticColors.pinnedMessageBannerIndicator
    get() = if (isLight) LightColorTokens.colorAlphaGray600 else DarkColorTokens.colorAlphaGray600

@OptIn(CoreColorToken::class)
val SemanticColors.pinnedMessageBannerBorder
    get() = if (isLight) LightColorTokens.colorAlphaGray400 else DarkColorTokens.colorAlphaGray400

val SemanticColors.floatingDateBadgeBackground
    get() = if (isLight) bgCanvasDefault else bgSubtlePrimary

@PreviewsDayNight
@Composable
internal fun ColorAliasesPreview() = ElementPreview {
    ColorListPreview(
        backgroundColor = Color.Black,
        foregroundColor = Color.White,
        colors = persistentMapOf(
            "roomListRoomName" to ElementTheme.colors.roomListRoomName,
            "roomListRoomMessage" to ElementTheme.colors.roomListRoomMessage,
            "roomListRoomMessageDate" to ElementTheme.colors.roomListRoomMessageDate,
            "unreadIndicator" to ElementTheme.colors.unreadIndicator,
            "placeholderBackground" to ElementTheme.colors.placeholderBackground,
            "messageFromMeBackground" to ElementTheme.colors.messageFromMeBackground,
            "messageFromOtherBackground" to ElementTheme.colors.messageFromOtherBackground,
            "progressIndicatorTrackColor" to ElementTheme.colors.progressIndicatorTrackColor,
            "temporaryColorBgSpecial" to ElementTheme.colors.temporaryColorBgSpecial,
        )
    )
}
