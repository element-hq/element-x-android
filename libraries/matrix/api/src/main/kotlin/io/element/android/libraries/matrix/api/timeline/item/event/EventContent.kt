/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.timeline.item.EventThreadInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

@Immutable
sealed interface EventContent

data class MessageContent(
    val body: String,
    val inReplyTo: InReplyTo?,
    val isEdited: Boolean,
    val threadInfo: EventThreadInfo?,
    val type: MessageType
) : EventContent

data object RedactedContent : EventContent

data class StickerContent(
    val filename: String,
    val body: String?,
    val info: ImageInfo,
    val source: MediaSource,
) : EventContent {
    val bestDescription: String
        get() = body ?: filename
}

data class PollContent(
    val question: String,
    val kind: PollKind,
    val maxSelections: ULong,
    val answers: ImmutableList<PollAnswer>,
    val votes: ImmutableMap<String, ImmutableList<UserId>>,
    val endTime: ULong?,
    val isEdited: Boolean,
) : EventContent

data class UnableToDecryptContent(
    val data: Data
) : EventContent {
    @Immutable
    sealed interface Data {
        data class OlmV1Curve25519AesSha2(
            val senderKey: String
        ) : Data

        data class MegolmV1AesSha2(
            val sessionId: String,
            val utdCause: UtdCause
        ) : Data

        data object Unknown : Data
    }
}

data class RoomMembershipContent(
    val userId: UserId,
    val userDisplayName: String?,
    val change: MembershipChange?,
    val reason: String?,
) : EventContent

data class ProfileChangeContent(
    val displayName: String?,
    val prevDisplayName: String?,
    val avatarUrl: String?,
    val prevAvatarUrl: String?
) : EventContent

data class StateContent(
    val stateKey: String,
    val content: OtherState
) : EventContent

data class FailedToParseMessageLikeContent(
    val eventType: String,
    val error: String
) : EventContent

data class FailedToParseStateContent(
    val eventType: String,
    val stateKey: String,
    val error: String
) : EventContent

data object LegacyCallInviteContent : EventContent

data object CallNotifyContent : EventContent

data object UnknownContent : EventContent
