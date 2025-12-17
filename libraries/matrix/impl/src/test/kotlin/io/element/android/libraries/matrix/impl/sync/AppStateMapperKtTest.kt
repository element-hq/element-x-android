/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.sync

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.sync.SyncState
import org.junit.Test
import org.matrix.rustcomponents.sdk.SyncServiceState

class AppStateMapperKtTest {
    @Test
    fun toSyncState() {
        assertThat(SyncServiceState.IDLE.toSyncState()).isEqualTo(SyncState.Idle)
        assertThat(SyncServiceState.RUNNING.toSyncState()).isEqualTo(SyncState.Running)
        assertThat(SyncServiceState.TERMINATED.toSyncState()).isEqualTo(SyncState.Terminated)
        assertThat(SyncServiceState.ERROR.toSyncState()).isEqualTo(SyncState.Error)
        assertThat(SyncServiceState.OFFLINE.toSyncState()).isEqualTo(SyncState.Offline)
    }
}
