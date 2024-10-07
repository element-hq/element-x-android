/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

sealed interface JoinRoomEvents {
    data object RetryFetchingContent : JoinRoomEvents
    data object JoinRoom : JoinRoomEvents
    data object KnockRoom : JoinRoomEvents
    data object ClearError : JoinRoomEvents
    data object AcceptInvite : JoinRoomEvents
    data object DeclineInvite : JoinRoomEvents
}
