/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.test.crash

import io.element.android.features.rageshake.api.crash.CrashDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

const val A_CRASH_DATA = "Some crash data"

class FakeCrashDataStore(
    crashData: String = "",
    appHasCrashed: Boolean = false,
) : CrashDataStore {
    private val appHasCrashedFlow = MutableStateFlow(appHasCrashed)
    private val crashDataFlow = MutableStateFlow(crashData)

    override fun setCrashData(crashData: String) {
        crashDataFlow.value = crashData
    }

    override suspend fun resetAppHasCrashed() {
        appHasCrashedFlow.value = false
    }

    override fun appHasCrashed(): Flow<Boolean> = appHasCrashedFlow

    override fun crashInfo(): Flow<String> = crashDataFlow

    override suspend fun reset() {
        appHasCrashedFlow.value = false
        crashDataFlow.value = ""
    }
}
