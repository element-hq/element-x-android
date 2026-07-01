/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.user

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
sealed interface DisplayedStatus : Parcelable {
    /** Status set manually by the user via m.status. */
    @Parcelize
    data class UserSet(val status: UserStatus) : DisplayedStatus

    /** Status set automatically when the user is in a call via m.call. */
    @Parcelize
    data class InCall(
        /** Unix timestamp in seconds when the call was joined (from m.call.call_joined_ts). */
        val callJoinedTs: Long,
    ) : DisplayedStatus
}
