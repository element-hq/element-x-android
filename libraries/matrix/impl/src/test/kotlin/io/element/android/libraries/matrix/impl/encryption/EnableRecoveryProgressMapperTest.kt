/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.encryption.EnableRecoveryProgress
import org.junit.Test
import org.matrix.rustcomponents.sdk.EnableRecoveryProgress as RustEnableRecoveryProgress

class EnableRecoveryProgressMapperTest {
    @Test
    fun `Ensure that mapping is correct`() {
        val sut = EnableRecoveryProgressMapper()
        assertThat(sut.map(RustEnableRecoveryProgress.CreatingRecoveryKey))
            .isEqualTo(EnableRecoveryProgress.CreatingRecoveryKey)
        assertThat(sut.map(RustEnableRecoveryProgress.CreatingBackup))
            .isEqualTo(EnableRecoveryProgress.CreatingBackup)
        assertThat(sut.map(RustEnableRecoveryProgress.Starting))
            .isEqualTo(EnableRecoveryProgress.Starting)
        assertThat(sut.map(RustEnableRecoveryProgress.BackingUp(1.toUInt(), 2.toUInt())))
            .isEqualTo(EnableRecoveryProgress.BackingUp(1, 2))
        assertThat(sut.map(RustEnableRecoveryProgress.RoomKeyUploadError))
            .isEqualTo(EnableRecoveryProgress.RoomKeyUploadError)
        assertThat(sut.map(RustEnableRecoveryProgress.Done("recoveryKey")))
            .isEqualTo(EnableRecoveryProgress.Done("recoveryKey"))
    }
}
