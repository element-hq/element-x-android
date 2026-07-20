/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

data class PttPrototypeState(
    /** Whether a PTT transport is available in this build for this room. */
    val isPttAvailable: Boolean,
    /** Whether PTT is enabled in this room (drives the in-room header/banner/composer UI). */
    val isPttEnabled: Boolean,
    /** Whether the transport session is joined/live (the "channel" is live). */
    val hasLiveChannel: Boolean,
    /** Number of participants currently in the channel. */
    val participantCount: Int,
    /** Whether the current user is connected to the channel. */
    val isUserInChannel: Boolean,
    /** Whether the local user currently holds the floor and is transmitting. */
    val isTransmitting: Boolean = false,
    val eventSink: (PttPrototypeEvent) -> Unit,
)
