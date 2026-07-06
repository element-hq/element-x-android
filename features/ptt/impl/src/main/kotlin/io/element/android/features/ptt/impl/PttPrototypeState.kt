/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

data class PttPrototypeState(
    /** Whether Element Call (LiveKit) is available on this server — PTT rides on it. */
    val isPttAvailable: Boolean,
    /** Whether PTT is enabled in this room (drives the in-room header/banner/composer UI). */
    val isPttEnabled: Boolean,
    /** Whether there is a live Element Call session in this room (the "channel" is live). */
    val hasLiveChannel: Boolean,
    /** Number of participants currently live in the channel. */
    val participantCount: Int,
    /** Whether the current user is one of the live participants. */
    val isUserInChannel: Boolean,
    val eventSink: (PttPrototypeEvent) -> Unit,
)
