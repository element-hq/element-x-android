/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.call.test.FakeElementCallController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallConnectivityTest {
    @Test
    fun `a client online from the start reports nothing`() = runTest {
        val controller = FakeElementCallController()
        val job = launch { controller.followConnectivity(MutableStateFlow(true)) }
        runCurrent()

        assertThat(controller.connectivityReports).isEmpty()
        job.cancel()
    }

    @Test
    fun `going offline and coming back are both reported, in order`() = runTest {
        val controller = FakeElementCallController()
        val isOnline = MutableStateFlow(true)
        val job = launch { controller.followConnectivity(isOnline) }
        runCurrent()

        isOnline.value = false
        runCurrent()
        isOnline.value = true
        runCurrent()

        assertThat(controller.connectivityReports).containsExactly(false, true).inOrder()
        job.cancel()
    }

    @Test
    fun `a client offline from the start is reported as such`() = runTest {
        val controller = FakeElementCallController()
        val job = launch { controller.followConnectivity(MutableStateFlow(false)) }
        runCurrent()

        assertThat(controller.connectivityReports).containsExactly(false)
        job.cancel()
    }
}
