/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.root

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import org.junit.Test

class SecureBackupRootStateTest {
    @Test
    fun `isKeyStorageEnabled should be true for all these backup states`() {
        listOf(
            BackupState.CREATING,
            BackupState.ENABLING,
            BackupState.RESUMING,
            BackupState.DOWNLOADING,
            BackupState.ENABLED,
        ).forEach { backupState ->
            assertThat(aSecureBackupRootState(backupState = backupState).isKeyStorageEnabled).isTrue()
        }
    }

    @Test
    fun `isKeyStorageEnabled should be false for all these backup states`() {
        listOf(
            BackupState.WAITING_FOR_SYNC,
            BackupState.DISABLING,
        ).forEach { backupState ->
            assertThat(aSecureBackupRootState(backupState = backupState).isKeyStorageEnabled).isFalse()
        }
    }

    @Test
    fun `isKeyStorageEnabled should have value depending on doesBackupExistOnServer when state is UNKNOWN`() {
        assertThat(
            aSecureBackupRootState(
                backupState = BackupState.UNKNOWN,
                doesBackupExistOnServer = AsyncData.Success(true),
            ).isKeyStorageEnabled
        ).isTrue()

        listOf(
            AsyncData.Uninitialized,
            AsyncData.Loading(),
            AsyncData.Failure(AN_EXCEPTION),
            AsyncData.Success(false),
        ).forEach { doesBackupExistOnServer ->
            assertThat(
                aSecureBackupRootState(
                    backupState = BackupState.UNKNOWN,
                    doesBackupExistOnServer = doesBackupExistOnServer,
                ).isKeyStorageEnabled
            ).isFalse()
        }
    }
}
