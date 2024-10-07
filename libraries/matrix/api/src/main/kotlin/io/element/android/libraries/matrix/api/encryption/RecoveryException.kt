/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.encryption

import io.element.android.libraries.matrix.api.exception.ClientException

sealed class RecoveryException(message: String) : Exception(message) {
    class SecretStorage(message: String) : RecoveryException(message)
    data object BackupExistsOnServer : RecoveryException("BackupExistsOnServer")
    data class Client(val exception: ClientException) : RecoveryException(exception.message ?: "Unknown error")
}
