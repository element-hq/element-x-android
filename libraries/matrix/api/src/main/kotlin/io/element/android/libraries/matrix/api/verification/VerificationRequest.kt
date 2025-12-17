/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.verification

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.parcelize.Parcelize

@Immutable
sealed interface VerificationRequest : Parcelable {
    @Immutable
    sealed interface Outgoing : VerificationRequest {
        @Parcelize
        data object CurrentSession : Outgoing

        @Parcelize
        data class User(val userId: UserId) : Outgoing
    }

    sealed class Incoming(open val details: SessionVerificationRequestDetails) : VerificationRequest {
        @Parcelize
        data class OtherSession(override val details: SessionVerificationRequestDetails) : Incoming(details)

        @Parcelize
        data class User(override val details: SessionVerificationRequestDetails) : Incoming(details)
    }
}
