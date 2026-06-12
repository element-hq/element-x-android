/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.exception

import io.element.android.libraries.matrix.api.exception.ContentScannerErrorReason
import uniffi.matrix_sdk_contentscanner.ErrorReason
import uniffi.matrix_sdk_contentscanner.ErrorReason.MCS_BAD_DECRYPTION
import uniffi.matrix_sdk_contentscanner.ErrorReason.MCS_MALFORMED_JSON
import uniffi.matrix_sdk_contentscanner.ErrorReason.MCS_MEDIA_FAILED_TO_DECRYPT
import uniffi.matrix_sdk_contentscanner.ErrorReason.MCS_MEDIA_NOT_CLEAN
import uniffi.matrix_sdk_contentscanner.ErrorReason.MCS_MEDIA_REQUEST_FAILED
import uniffi.matrix_sdk_contentscanner.ErrorReason.MCS_MIME_TYPE_FORBIDDEN
import uniffi.matrix_sdk_contentscanner.ErrorReason.M_MISSING_TOKEN
import uniffi.matrix_sdk_contentscanner.ErrorReason.M_NOT_FOUND
import uniffi.matrix_sdk_contentscanner.ErrorReason.M_UNKNOWN
import uniffi.matrix_sdk_contentscanner.ErrorReason.M_UNKNOWN_TOKEN

fun ContentScannerErrorReason.Companion.fromRust(reason: ErrorReason) = when (reason) {
    M_UNKNOWN -> ContentScannerErrorReason.M_UNKNOWN
    M_MISSING_TOKEN -> ContentScannerErrorReason.M_MISSING_TOKEN
    M_UNKNOWN_TOKEN -> ContentScannerErrorReason.M_UNKNOWN_TOKEN
    MCS_MALFORMED_JSON -> ContentScannerErrorReason.MCS_MALFORMED_JSON
    MCS_MEDIA_FAILED_TO_DECRYPT -> ContentScannerErrorReason.MCS_MEDIA_FAILED_TO_DECRYPT
    M_NOT_FOUND -> ContentScannerErrorReason.M_NOT_FOUND
    MCS_MEDIA_NOT_CLEAN -> ContentScannerErrorReason.MCS_MEDIA_NOT_CLEAN
    MCS_MIME_TYPE_FORBIDDEN -> ContentScannerErrorReason.MCS_MIME_TYPE_FORBIDDEN
    MCS_BAD_DECRYPTION -> ContentScannerErrorReason.MCS_BAD_DECRYPTION
    MCS_MEDIA_REQUEST_FAILED -> ContentScannerErrorReason.MCS_MEDIA_REQUEST_FAILED
}
