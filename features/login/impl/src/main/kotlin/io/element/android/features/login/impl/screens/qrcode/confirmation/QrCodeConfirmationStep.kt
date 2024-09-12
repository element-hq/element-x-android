/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.confirmation

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import io.element.android.libraries.architecture.NodeInputs
import kotlinx.parcelize.Parcelize

@Immutable
sealed interface QrCodeConfirmationStep : NodeInputs, Parcelable {
    @Parcelize
    data class DisplayCheckCode(val code: String) : QrCodeConfirmationStep

    @Parcelize
    data class DisplayVerificationCode(val code: String) : QrCodeConfirmationStep
}
