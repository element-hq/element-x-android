/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.core

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.ProgressCallback
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.TransmissionProgress
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProgressWatcherWrapperKtTest {
    @Test
    fun testToProgressWatcher() = runTest {
        suspendCoroutine { continuation ->
            val callback = object : ProgressCallback {
                override fun onProgress(current: Long, total: Long) {
                    assertThat(current).isEqualTo(1)
                    assertThat(total).isEqualTo(2)
                    continuation.resume(Unit)
                }
            }
            val result = callback.toProgressWatcher()
            result.transmissionProgress(TransmissionProgress(1.toULong(), 2.toULong()))
        }
    }
}
