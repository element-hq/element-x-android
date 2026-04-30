/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.error

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import io.element.android.libraries.architecture.NodeInputs
import kotlinx.parcelize.Parcelize

@Immutable
sealed interface ErrorScreenType : NodeInputs, Parcelable {
    @Parcelize
    data object Cancelled : ErrorScreenType

    @Parcelize
    data object Expired : ErrorScreenType

    @Parcelize
    data object OtherDeviceAlreadySignedIn : ErrorScreenType

    @Parcelize
    data object Mismatch2Digits : ErrorScreenType

    @Parcelize
    data object InsecureChannelDetected : ErrorScreenType

    @Parcelize
    data object Declined : ErrorScreenType

    @Parcelize
    data object ProtocolNotSupported : ErrorScreenType

    @Parcelize
    data object SlidingSyncNotAvailable : ErrorScreenType

    @Parcelize
    data object UnknownError : ErrorScreenType
}
