/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api

import android.os.Parcelable
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.parcelize.Parcelize

/**
 * Everything a [PttTransport] needs to join a channel, resolved from the room's
 * `io.element.ptt.config` state event.
 *
 * Element Call derives everything from the Matrix room itself, so [transportMetadata] is empty for
 * it. Zello/Mumble carry their "bring your own server" connection details there (e.g. server URL,
 * channel id).
 *
 * Parcelable so it can be carried in the Intent that starts the session host service.
 */
@Parcelize
data class PttChannelConfig(
    val sessionId: SessionId,
    val roomId: RoomId,
    val transport: PttTransportType,
    val transportMetadata: Map<String, String> = emptyMap(),
) : Parcelable
