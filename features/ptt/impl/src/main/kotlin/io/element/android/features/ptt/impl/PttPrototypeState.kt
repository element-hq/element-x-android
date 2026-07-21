/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import io.element.android.libraries.permissions.api.PermissionsState

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
    /** Microphone permission state — gates joining and drives the rationale dialog. */
    val permissionsState: PermissionsState,
    /** Whether the "draw over other apps" permission is granted (needed for the floating button). */
    val canDrawOverlays: Boolean = true,
    /** Whether full-screen-intent notifications are allowed (needed for the lock-screen alert). */
    val canUseFullScreenIntent: Boolean = true,
    val eventSink: (PttPrototypeEvent) -> Unit,
)
