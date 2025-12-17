/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth.qrlogin

sealed class QrLoginException : Exception() {
    data object Cancelled : QrLoginException()
    data object ConnectionInsecure : QrLoginException()
    data object Declined : QrLoginException()
    data object Expired : QrLoginException()
    data object LinkingNotSupported : QrLoginException()
    data object OidcMetadataInvalid : QrLoginException()
    data object SlidingSyncNotAvailable : QrLoginException()
    data object OtherDeviceNotSignedIn : QrLoginException()
    data object CheckCodeAlreadySent : QrLoginException()
    data object CheckCodeCannotBeSent : QrLoginException()
    data object Unknown : QrLoginException()
    data object NotFound : QrLoginException()
}
