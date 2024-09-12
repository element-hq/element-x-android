/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import io.element.android.libraries.matrix.api.encryption.SteadyStateException
import org.matrix.rustcomponents.sdk.SteadyStateException as RustSteadyStateException

class SteadyStateExceptionMapper {
    fun map(data: RustSteadyStateException): SteadyStateException {
        return when (data) {
            is RustSteadyStateException.BackupDisabled -> SteadyStateException.BackupDisabled(
                message = data.message.orEmpty()
            )
            is RustSteadyStateException.Connection -> SteadyStateException.Connection(
                message = data.message.orEmpty()
            )
            is RustSteadyStateException.Lagged -> SteadyStateException.Lagged(
                message = data.message.orEmpty()
            )
        }
    }
}
