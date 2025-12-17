/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.identity

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.ui.room.IdentityRoomMember
import io.element.android.libraries.matrix.ui.room.RoomMemberIdentityStateChange
import kotlinx.collections.immutable.toImmutableList

class IdentityChangeStateProvider : PreviewParameterProvider<IdentityChangeState> {
    override val values: Sequence<IdentityChangeState>
        get() = sequenceOf(
            anIdentityChangeState(),
            anIdentityChangeState(
                roomMemberIdentityStateChanges = listOf(
                    aRoomMemberIdentityStateChange(
                        identityRoomMember = anIdentityRoomMember(),
                        identityState = IdentityState.PinViolation,
                    ),
                ),
            ),
            anIdentityChangeState(
                roomMemberIdentityStateChanges = listOf(
                    aRoomMemberIdentityStateChange(
                        identityRoomMember = anIdentityRoomMember(displayNameOrDefault = "Alice"),
                        identityState = IdentityState.VerificationViolation,
                    ),
                ),
            ),
        )
}

internal fun aRoomMemberIdentityStateChange(
    identityRoomMember: IdentityRoomMember = anIdentityRoomMember(),
    identityState: IdentityState = IdentityState.PinViolation,
) = RoomMemberIdentityStateChange(
    identityRoomMember = identityRoomMember,
    identityState = identityState,
)

internal fun anIdentityChangeState(
    roomMemberIdentityStateChanges: List<RoomMemberIdentityStateChange> = emptyList(),
    eventSink: (IdentityChangeEvent) -> Unit = {},
) = IdentityChangeState(
    roomMemberIdentityStateChanges = roomMemberIdentityStateChanges.toImmutableList(),
    eventSink = eventSink,
)

internal fun anIdentityRoomMember(
    userId: UserId = UserId("@alice:example.com"),
    displayNameOrDefault: String = userId.extractedDisplayName,
    avatarData: AvatarData = AvatarData(
        id = userId.value,
        name = null,
        url = null,
        size = AvatarSize.ComposerAlert,
    ),
) = IdentityRoomMember(
    userId = userId,
    displayNameOrDefault = displayNameOrDefault,
    avatarData = avatarData,
)
