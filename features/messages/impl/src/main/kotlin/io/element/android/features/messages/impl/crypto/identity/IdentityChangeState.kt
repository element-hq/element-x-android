/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.identity

import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import kotlinx.collections.immutable.ImmutableList

data class IdentityChangeState(
    val roomMemberIdentityStateChanges: ImmutableList<RoomMemberIdentityStateChange>,
    val eventSink: (IdentityChangeEvent) -> Unit,
)

data class RoomMemberIdentityStateChange(
    val identityRoomMember: IdentityRoomMember,
    val identityState: IdentityState,
)
