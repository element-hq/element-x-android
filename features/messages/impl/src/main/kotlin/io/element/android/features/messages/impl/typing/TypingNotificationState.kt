/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.typing

import kotlinx.collections.immutable.ImmutableList

/**
 * State for the typing notification view.
 */
data class TypingNotificationState(
    /** Whether to render the typing notifications based on the user's preferences. */
    val renderTypingNotifications: Boolean,
    /** The room members currently typing. */
    val typingMembers: ImmutableList<TypingRoomMember>,
    /** Whether to reserve space for the typing notifications at the bottom of the timeline. */
    val reserveSpace: Boolean,
)
