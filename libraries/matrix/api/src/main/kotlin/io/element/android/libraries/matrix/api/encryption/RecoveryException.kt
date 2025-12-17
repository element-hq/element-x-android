/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.encryption

import io.element.android.libraries.matrix.api.exception.ClientException

sealed class RecoveryException(message: String) : Exception(message) {
    class SecretStorage(message: String) : RecoveryException(message)
    class Import(message: String) : RecoveryException(message)
    data object BackupExistsOnServer : RecoveryException("BackupExistsOnServer")
    data class Client(val exception: ClientException) : RecoveryException(exception.message ?: "Unknown error")
}
