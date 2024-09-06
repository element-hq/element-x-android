/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.scan

sealed interface QrCodeScanEvents {
    data class QrCodeScanned(val code: ByteArray) : QrCodeScanEvents
    data object TryAgain : QrCodeScanEvents
}
