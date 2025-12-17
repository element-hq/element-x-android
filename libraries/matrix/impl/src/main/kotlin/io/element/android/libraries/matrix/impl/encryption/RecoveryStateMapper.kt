/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import io.element.android.libraries.matrix.api.encryption.RecoveryState
import org.matrix.rustcomponents.sdk.RecoveryState as RustRecoveryState

class RecoveryStateMapper {
    fun map(state: RustRecoveryState): RecoveryState {
        return when (state) {
            RustRecoveryState.UNKNOWN -> RecoveryState.UNKNOWN
            RustRecoveryState.ENABLED -> RecoveryState.ENABLED
            RustRecoveryState.DISABLED -> RecoveryState.DISABLED
            RustRecoveryState.INCOMPLETE -> RecoveryState.INCOMPLETE
        }
    }
}
