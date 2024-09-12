/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.encryption.BackupState
import org.junit.Test
import org.matrix.rustcomponents.sdk.BackupState as RustBackupState

class BackupStateMapperTest {
    @Test
    fun `Ensure that mapping is correct`() {
        assertThat(BackupStateMapper().map(RustBackupState.UNKNOWN)).isEqualTo(BackupState.UNKNOWN)
        assertThat(BackupStateMapper().map(RustBackupState.CREATING)).isEqualTo(BackupState.CREATING)
        assertThat(BackupStateMapper().map(RustBackupState.ENABLING)).isEqualTo(BackupState.ENABLING)
        assertThat(BackupStateMapper().map(RustBackupState.RESUMING)).isEqualTo(BackupState.RESUMING)
        assertThat(BackupStateMapper().map(RustBackupState.ENABLED)).isEqualTo(BackupState.ENABLED)
        assertThat(BackupStateMapper().map(RustBackupState.DOWNLOADING)).isEqualTo(BackupState.DOWNLOADING)
        assertThat(BackupStateMapper().map(RustBackupState.DISABLING)).isEqualTo(BackupState.DISABLING)
    }
}
