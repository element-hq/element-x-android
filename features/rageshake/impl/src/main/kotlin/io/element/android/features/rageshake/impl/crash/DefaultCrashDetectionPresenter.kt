/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import io.element.android.libraries.di.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultCrashDetectionPresenter @Inject constructor(private val crashDataStore: CrashDataStore) :
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
