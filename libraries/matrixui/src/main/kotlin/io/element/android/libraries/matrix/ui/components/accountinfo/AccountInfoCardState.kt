/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components.accountinfo

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.user.MatrixUser

data class AccountInfoCardState(
    val matrixUser: MatrixUser,
    val mode: AccountInfoCardMode,
)

@Immutable
sealed interface AccountInfoCardMode {
    data object Simple : AccountInfoCardMode

    data class UserVerification(
        val otherUser: MatrixUser,
        val hint: String,
    ) : AccountInfoCardMode

    data class DeviceVerification(
        val deviceName: String?,
        val deviceId: DeviceId,
        val signInFormattedTimestamp: String,
    ) : AccountInfoCardMode
}
