/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned.banner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import io.element.android.libraries.designsystem.text.toAnnotatedString
import io.element.android.libraries.ui.strings.CommonStrings

@Immutable
sealed interface PinnedMessagesBannerState {
    data object Hidden : PinnedMessagesBannerState
    sealed interface Visible : PinnedMessagesBannerState
    data class Loading(val expectedPinnedMessagesCount: Int) : Visible
    data class Loaded(
        val currentPinnedMessage: PinnedMessagesBannerItem,
        val currentPinnedMessageIndex: Int,
        val loadedPinnedMessagesCount: Int,
        val eventSink: (PinnedMessagesBannerEvents) -> Unit
    ) : Visible

    fun pinnedMessagesCount() = when (this) {
        is Hidden -> 0
        is Loading -> expectedPinnedMessagesCount
        is Loaded -> loadedPinnedMessagesCount
    }

    fun currentPinnedMessageIndex() = when (this) {
        is Hidden -> 0
        is Loading -> expectedPinnedMessagesCount - 1
        is Loaded -> currentPinnedMessageIndex
    }

    @Composable
    fun formattedMessage() = when (this) {
        is Hidden -> AnnotatedString("")
        is Loading -> stringResource(id = CommonStrings.screen_room_pinned_banner_loading_description).toAnnotatedString()
        is Loaded -> currentPinnedMessage.formatted
    }
}
