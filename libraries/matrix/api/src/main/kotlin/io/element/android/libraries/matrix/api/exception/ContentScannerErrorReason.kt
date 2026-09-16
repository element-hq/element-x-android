/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.exception

enum class ContentScannerErrorReason {
    MCS_MALFORMED_JSON,
    MCS_MEDIA_FAILED_TO_DECRYPT,
    M_MISSING_TOKEN,
    M_UNKNOWN_TOKEN,
    M_NOT_FOUND,
    MCS_MEDIA_NOT_CLEAN,
    MCS_MIME_TYPE_FORBIDDEN,
    MCS_BAD_DECRYPTION,
    M_UNKNOWN,
    MCS_MEDIA_REQUEST_FAILED;

    companion object;

    fun isDangerous(): Boolean = when (this) {
        MCS_MEDIA_NOT_CLEAN, MCS_MIME_TYPE_FORBIDDEN -> true
        else -> false
    }
}
