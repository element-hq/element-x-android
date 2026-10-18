/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components.accountinfo

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUser

open class AccountInfoCardStatePreviewParam : PreviewParameterProvider<AccountInfoCardState> {
    override val values: Sequence<AccountInfoCardState>
        get() = sequenceOf(
            anAccountInfoCardState(),
            anAccountInfoCardState(aMatrixUser(displayName = null)),
            anAccountInfoCardState(
                mode = AccountInfoCardMode.UserVerification(
                    otherUser = aMatrixUser(displayName = "Other User"),
                    hint = "User request verification hint",
                )
            ),
            anAccountInfoCardState(
                mode = AccountInfoCardMode.DeviceVerification(
                    deviceName = "My Device",
                    deviceId = DeviceId("DEVICEID"),
                    signInFormattedTimestamp = "2024-06-01 12:00:00",
                )
            ),
        )
}

private fun anAccountInfoCardState(
    matrixUser: MatrixUser = aMatrixUser(),
    mode: AccountInfoCardMode = AccountInfoCardMode.Simple,
) = AccountInfoCardState(
    matrixUser = matrixUser,
    mode = mode,
)
