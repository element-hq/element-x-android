/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned.banner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.text.toAnnotatedString
import io.element.android.libraries.ui.strings.CommonStrings

@Immutable
sealed interface PinnedMessagesBannerState {
    data object Hidden : PinnedMessagesBannerState
    @Immutable
    sealed interface Visible : PinnedMessagesBannerState {
        fun pinnedMessagesCount() = when (this) {
            is Loading -> expectedPinnedMessagesCount
            is Loaded -> loadedPinnedMessagesCount
        }

        fun currentPinnedMessageIndex() = when (this) {
            is Loading -> expectedPinnedMessagesCount - 1
            is Loaded -> currentPinnedMessageIndex
        }

        @Composable
        fun formattedMessage() = when (this) {
            is Loading -> stringResource(id = CommonStrings.screen_room_pinned_banner_loading_description).toAnnotatedString()
            is Loaded -> currentPinnedMessage.formatted
        }
    }

    data class Loading(val expectedPinnedMessagesCount: Int) : Visible
    data class Loaded(
        val currentPinnedMessage: PinnedMessagesBannerItem,
        val currentPinnedMessageIndex: Int,
        val loadedPinnedMessagesCount: Int,
        val eventSink: (PinnedMessagesBannerEvents) -> Unit
    ) : Visible
}
