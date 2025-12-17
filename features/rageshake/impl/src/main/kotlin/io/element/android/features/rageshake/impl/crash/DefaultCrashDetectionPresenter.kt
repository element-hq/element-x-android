/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.rageshake.impl.crash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.rageshake.api.RageshakeFeatureAvailability
import io.element.android.features.rageshake.api.crash.CrashDetectionEvents
import io.element.android.features.rageshake.api.crash.CrashDetectionPresenter
import io.element.android.features.rageshake.api.crash.CrashDetectionState
import io.element.android.libraries.core.meta.BuildMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@ContributesBinding(AppScope::class)
class DefaultCrashDetectionPresenter(
    private val buildMeta: BuildMeta,
    private val crashDataStore: CrashDataStore,
    private val rageshakeFeatureAvailability: RageshakeFeatureAvailability,
) : CrashDetectionPresenter {
    @Composable
    override fun present(): CrashDetectionState {
        val localCoroutineScope = rememberCoroutineScope()
        val crashDetected by remember {
            rageshakeFeatureAvailability.isAvailable()
                .flatMapLatest { isAvailable ->
                    if (isAvailable) {
                        crashDataStore.appHasCrashed()
                    } else {
                        flowOf(false)
                    }
                }
        }.collectAsState(false)

        fun handleEvent(event: CrashDetectionEvents) {
            when (event) {
                CrashDetectionEvents.ResetAllCrashData -> localCoroutineScope.resetAll()
                CrashDetectionEvents.ResetAppHasCrashed -> localCoroutineScope.resetAppHasCrashed()
            }
        }

        return CrashDetectionState(
            appName = buildMeta.applicationName,
            crashDetected = crashDetected,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.resetAppHasCrashed() = launch {
        crashDataStore.resetAppHasCrashed()
    }

    private fun CoroutineScope.resetAll() = launch {
        crashDataStore.reset()
    }
}
