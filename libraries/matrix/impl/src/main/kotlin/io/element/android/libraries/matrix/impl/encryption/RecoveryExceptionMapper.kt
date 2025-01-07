/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import io.element.android.libraries.matrix.api.encryption.RecoveryException
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.impl.exception.mapClientException
import org.matrix.rustcomponents.sdk.RecoveryException as RustRecoveryException

fun Throwable.mapRecoveryException(): RecoveryException {
    return when (this) {
        is RustRecoveryException -> {
            when (this) {
                is RustRecoveryException.SecretStorage -> RecoveryException.SecretStorage(
                    message = errorMessage
                )
                is RustRecoveryException.BackupExistsOnServer -> RecoveryException.BackupExistsOnServer
                is RustRecoveryException.Client -> RecoveryException.Client(
                    source.mapClientException()
                )
            }
        }
        else -> RecoveryException.Client(
            ClientException.Other("Unknown error")
        )
    }
}
