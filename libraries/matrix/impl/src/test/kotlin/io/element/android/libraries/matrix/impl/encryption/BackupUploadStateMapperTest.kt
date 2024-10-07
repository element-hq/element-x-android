/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import org.junit.Test
import org.matrix.rustcomponents.sdk.BackupUploadState as RustBackupUploadState

class BackupUploadStateMapperTest {
    @Test
    fun `Ensure that mapping is correct`() {
        val sut = BackupUploadStateMapper()
        assertThat(sut.map(RustBackupUploadState.Waiting))
            .isEqualTo(BackupUploadState.Waiting)
        assertThat(sut.map(RustBackupUploadState.Error))
            .isEqualTo(BackupUploadState.Error)
        assertThat(sut.map(RustBackupUploadState.Done))
            .isEqualTo(BackupUploadState.Done)
        assertThat(sut.map(RustBackupUploadState.Uploading(1.toUInt(), 2.toUInt())))
            .isEqualTo(BackupUploadState.Uploading(1, 2))
    }

    @Test
    fun `Ensure that full uploading is mapper to Done`() {
        val sut = BackupUploadStateMapper()
        assertThat(sut.map(RustBackupUploadState.Uploading(2.toUInt(), 2.toUInt())))
            .isEqualTo(BackupUploadState.Done)
    }
}
