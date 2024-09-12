/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.encryption.EnableRecoveryProgress
import org.junit.Test
import org.matrix.rustcomponents.sdk.EnableRecoveryProgress as RustEnableRecoveryProgress

class EnableRecoveryProgressMapperTest {
    @Test
    fun `Ensure that mapping is correct`() {
        assertThat(EnableRecoveryProgressMapper().map(RustEnableRecoveryProgress.CreatingRecoveryKey))
            .isEqualTo(EnableRecoveryProgress.CreatingRecoveryKey)
        assertThat(EnableRecoveryProgressMapper().map(RustEnableRecoveryProgress.CreatingBackup))
            .isEqualTo(EnableRecoveryProgress.CreatingBackup)
        assertThat(EnableRecoveryProgressMapper().map(RustEnableRecoveryProgress.Starting))
            .isEqualTo(EnableRecoveryProgress.Starting)
        assertThat(EnableRecoveryProgressMapper().map(RustEnableRecoveryProgress.BackingUp(1.toUInt(), 2.toUInt())))
            .isEqualTo(EnableRecoveryProgress.BackingUp(1, 2))
        assertThat(EnableRecoveryProgressMapper().map(RustEnableRecoveryProgress.RoomKeyUploadError))
            .isEqualTo(EnableRecoveryProgress.RoomKeyUploadError)
        assertThat(EnableRecoveryProgressMapper().map(RustEnableRecoveryProgress.Done("recoveryKey")))
            .isEqualTo(EnableRecoveryProgress.Done("recoveryKey"))
    }
}
