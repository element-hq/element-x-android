/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.crash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.rageshake.api.crash.CrashDataStore
import io.element.android.features.rageshake.api.crash.CrashDetectionEvents
import io.element.android.features.rageshake.api.crash.CrashDetectionPresenter
import io.element.android.features.rageshake.api.crash.CrashDetectionState
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultCrashDetectionPresenter @Inject constructor(
    private val buildMeta: BuildMeta,
    private val crashDataStore: CrashDataStore,
) :
    CrashDetectionPresenter {
    @Composable
    override fun present(): CrashDetectionState {
        val localCoroutineScope = rememberCoroutineScope()
        val crashDetected = crashDataStore.appHasCrashed().collectAsState(initial = false)

        fun handleEvents(event: CrashDetectionEvents) {
            when (event) {
                CrashDetectionEvents.ResetAllCrashData -> localCoroutineScope.resetAll()
                CrashDetectionEvents.ResetAppHasCrashed -> localCoroutineScope.resetAppHasCrashed()
            }
        }

        return CrashDetectionState(
            appName = buildMeta.applicationName,
            crashDetected = crashDetected.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.resetAppHasCrashed() = launch {
        crashDataStore.resetAppHasCrashed()
    }

    private fun CoroutineScope.resetAll() = launch {
        crashDataStore.reset()
    }
}
