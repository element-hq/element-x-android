/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.api

import android.os.Parcelable
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.parcelize.Parcelize

sealed interface CallType : NodeInputs, Parcelable {
    @Parcelize
    data class ExternalUrl(val url: String) : CallType

    @Parcelize
    data class RoomCall(
        val sessionId: SessionId,
        val roomId: RoomId,
    ) : CallType
}
