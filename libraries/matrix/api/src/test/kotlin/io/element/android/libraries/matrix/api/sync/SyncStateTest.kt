/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.sync

import org.junit.Test

class SyncStateTest {
    @Test
    fun `isConnected should return true for Idle`() {
        assert(SyncState.Idle.isConnected())
    }

    @Test
    fun `isConnected should return true for Running`() {
        assert(SyncState.Running.isConnected())
    }

    @Test
    fun `isConnected should return true for Error`() {
        assert(SyncState.Error.isConnected())
    }

    @Test
    fun `isConnected should return true for Terminated`() {
        assert(SyncState.Terminated.isConnected())
    }

    @Test
    fun `isConnected should return false for Offline`() {
        assert(!SyncState.Offline.isConnected())
    }
}
