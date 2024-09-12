/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.actionlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class ActionListState(
    val target: Target,
    val eventSink: (ActionListEvents) -> Unit,
) {
    @Immutable
    sealed interface Target {
        data object None : Target
        data class Loading(val event: TimelineItem.Event) : Target
        data class Success(
            val event: TimelineItem.Event,
            val displayEmojiReactions: Boolean,
            val verifiedUserSendFailure: VerifiedUserSendFailure,
            val actions: ImmutableList<TimelineItemAction>,
        ) : Target
    }

    @Immutable
    sealed interface VerifiedUserSendFailure {
        data object None : VerifiedUserSendFailure
        data class UnsignedDevice(val displayName: String) : VerifiedUserSendFailure
        data class ChangedIdentity(val displayName: String) : VerifiedUserSendFailure

        @Composable
        fun formatted(): String {
            return when (this) {
                is None -> ""
                is UnsignedDevice -> stringResource(CommonStrings.screen_timeline_item_menu_send_failure_unsigned_device, displayName)
                is ChangedIdentity -> stringResource(CommonStrings.screen_timeline_item_menu_send_failure_changed_identity, displayName)
            }
        }
    }
}
