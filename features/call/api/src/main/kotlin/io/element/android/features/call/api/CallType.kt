/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.api

import android.os.Parcelable
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.parcelize.Parcelize

sealed interface CallType : NodeInputs, Parcelable {
    @Parcelize
    data class ExternalUrl(val url: String) : CallType {
        override fun toString(): String {
            return "ExternalUrl"
        }
    }

    @Parcelize
    data class RoomCall(
        val sessionId: SessionId,
        val roomId: RoomId,
    ) : CallType {
        override fun toString(): String {
            return "RoomCall(sessionId=$sessionId, roomId=$roomId)"
        }
    }
}
