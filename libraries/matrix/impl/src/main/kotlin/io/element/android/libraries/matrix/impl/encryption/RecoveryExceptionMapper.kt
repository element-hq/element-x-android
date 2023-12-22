/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.encryption

import io.element.android.libraries.matrix.api.encryption.RecoveryException
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.impl.exception.mapClientException
import org.matrix.rustcomponents.sdk.RecoveryException as RustRecoveryException

fun Throwable.mapRecoveryException(): RecoveryException {
    return when (this) {
        is RustRecoveryException.SecretStorage -> RecoveryException.SecretStorage(
            message = errorMessage
        )
        is RustRecoveryException.BackupExistsOnServer -> RecoveryException.BackupExistsOnServer
        is RustRecoveryException.Client -> RecoveryException.Client(
            source.mapClientException()
        )
        else -> RecoveryException.Client(
            ClientException.Other("Unknown error")
        )
    }
}
