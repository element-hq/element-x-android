/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl

import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerState
import io.element.android.features.messages.impl.actionlist.ActionListState
import io.element.android.features.messages.impl.crypto.identity.IdentityChangeState
import io.element.android.features.messages.impl.link.LinkState
import io.element.android.features.messages.impl.messagecomposer.MessageComposerState
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerState
import io.element.android.features.messages.impl.timeline.TimelineState
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionState
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryState
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetState
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.room.tombstone.SuccessorRoom
import kotlinx.collections.immutable.ImmutableList

data class MessagesState(
    val roomId: RoomId,
    val roomName: String?,
    val roomAvatar: AvatarData,
    val heroes: ImmutableList<AvatarData>,
    val userEventPermissions: UserEventPermissions,
    val composerState: MessageComposerState,
    val voiceMessageComposerState: VoiceMessageComposerState,
    val timelineState: TimelineState,
    val timelineProtectionState: TimelineProtectionState,
    val identityChangeState: IdentityChangeState,
    val linkState: LinkState,
    val actionListState: ActionListState,
    val customReactionState: CustomReactionState,
    val reactionSummaryState: ReactionSummaryState,
    val readReceiptBottomSheetState: ReadReceiptBottomSheetState,
    val snackbarMessage: SnackbarMessage?,
    val inviteProgress: AsyncData<Unit>,
    val showReinvitePrompt: Boolean,
    val enableTextFormatting: Boolean,
    val roomCallState: RoomCallState,
    val appName: String,
    val pinnedMessagesBannerState: PinnedMessagesBannerState,
    val dmUserVerificationState: IdentityState?,
    val roomMemberModerationState: RoomMemberModerationState,
    val successorRoom: SuccessorRoom?,
    val eventSink: (MessagesEvents) -> Unit
) {
    val isTombstoned = successorRoom != null
}
