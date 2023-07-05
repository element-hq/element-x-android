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

package io.element.android.libraries.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.element.android.libraries.designsystem.preview.ElementPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.theme.compound.generated.SemanticColors
import io.element.android.libraries.theme.previews.ColorListPreview
import kotlinx.collections.immutable.persistentMapOf

/**
 * Room list.
 */
@Composable
fun MaterialTheme.roomListRoomName() = colorScheme.primary

@Composable
fun MaterialTheme.roomListRoomMessage() = colorScheme.secondary

@Composable
fun MaterialTheme.roomListRoomMessageDate() = colorScheme.secondary

val SemanticColors.unreadIndicator
    get() = iconAccentTertiary

val SemanticColors.roomListPlaceholder
    get() = bgSubtleSecondary

// This color is not present in Semantic color, so put hard-coded value for now
val SemanticColors.messageFromMeBackground
    get() = if (isLight) {
        // We want LightDesignTokens.colorGray400
        Color(0xFFE1E6EC)
    } else {
        // We want DarkDesignTokens.colorGray500
        Color(0xFF323539)
    }

// This color is not present in Semantic color, so put hard-coded value for now
val SemanticColors.messageFromOtherBackground
    get() = if (isLight) {
        // We want LightDesignTokens.colorGray300
        Color(0xFFF0F2F5)
    } else {
        // We want DarkDesignTokens.colorGray400
        Color(0xFF26282D)
    }

@ElementPreviews
@Composable
internal fun ColorAliasesPreview() {
    ElementPreview { ContentToPreview() }
}

@ElementPreviews
@Composable
internal fun ColorAliasesDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    ColorListPreview(
        backgroundColor = Color.Black,
        foregroundColor = Color.White,
        colors = persistentMapOf(
            "roomListRoomName" to MaterialTheme.roomListRoomName(),
            "roomListRoomMessage" to MaterialTheme.roomListRoomMessage(),
            "roomListRoomMessageDate" to MaterialTheme.roomListRoomMessageDate(),
            "unreadIndicator" to ElementTheme.colors.unreadIndicator,
            "roomListPlaceholder" to ElementTheme.colors.roomListPlaceholder,
            "messageFromMeBackground" to ElementTheme.colors.messageFromMeBackground,
            "messageFromOtherBackground" to ElementTheme.colors.messageFromOtherBackground,
        )
    )
}
