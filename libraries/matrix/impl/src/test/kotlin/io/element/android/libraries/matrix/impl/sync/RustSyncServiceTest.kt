/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.sync

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiSyncService
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RustSyncServiceTest {
    @Test
    fun `start and stop succeed and lifecycle calls after destroy do nothing`() = runTest {
        val calls = mutableListOf<String>()
        val inner = FakeFfiSyncService(
            start = { calls.add("start") },
            stop = { calls.add("stop") },
            destroy = { calls.add("destroy") },
        )
        val service = RustSyncService(inner, StandardTestDispatcher(testScheduler), backgroundScope)

        assertThat(service.startSync().isSuccess).isTrue()
        assertThat(service.stopSync().isSuccess).isTrue()
        assertThat(calls).containsExactly("start", "stop").inOrder()

        service.destroy()
        assertThat(calls).containsExactly("start", "stop", "stop", "destroy").inOrder()

        assertThat(service.startSync().isSuccess).isTrue()
        assertThat(service.stopSync().isSuccess).isTrue()
        service.destroy()
        assertThat(calls).containsExactly("start", "stop", "stop", "destroy").inOrder()
    }

    @Test
    fun `stop failure returns a failed result and still allows destroy`() = runTest {
        val failure = IllegalStateException("Stop failed")
        var stopCalls = 0
        var destroyCalls = 0
        val inner = FakeFfiSyncService(
            stop = {
                stopCalls++
                if (stopCalls == 1) throw failure
            },
            destroy = { destroyCalls++ },
        )
        val service = RustSyncService(inner, StandardTestDispatcher(testScheduler), backgroundScope)

        val result = service.stopSync()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()).hasMessageThat().isEqualTo(failure.message)
        service.destroy()

        assertThat(stopCalls).isEqualTo(2)
        assertThat(destroyCalls).isEqualTo(1)
    }
}
