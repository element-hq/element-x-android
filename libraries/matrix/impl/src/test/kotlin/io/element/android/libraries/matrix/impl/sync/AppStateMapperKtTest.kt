/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
    }
}
