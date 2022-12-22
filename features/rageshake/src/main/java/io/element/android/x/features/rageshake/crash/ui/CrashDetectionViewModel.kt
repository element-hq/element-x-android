/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x.features.rageshake.crash.ui

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesViewModel
import io.element.android.x.core.di.daggerMavericksViewModelFactory
import io.element.android.x.di.AppScope
import io.element.android.x.features.rageshake.crash.CrashDataStore
import kotlinx.coroutines.launch

@ContributesViewModel(AppScope::class)
class CrashDetectionViewModel @AssistedInject constructor(
    @Assisted initialState: CrashDetectionViewState,
    private val crashDataStore: CrashDataStore,
) : MavericksViewModel<CrashDetectionViewState>(initialState) {

    companion object :
        MavericksViewModelFactory<CrashDetectionViewModel, CrashDetectionViewState> by daggerMavericksViewModelFactory()

    init {
        observeDataStore()
    }

    private fun observeDataStore() {
        viewModelScope.launch {
            crashDataStore.appHasCrashed().collect { appHasCrashed ->
                setState {
                    copy(
                        crashDetected = appHasCrashed
                    )
                }
            }
        }
    }

    fun onYes() {
        viewModelScope.launch {
            crashDataStore.resetAppHasCrashed()
        }
    }

    fun onPopupDismissed() {
        viewModelScope.launch {
            crashDataStore.reset()
        }
    }
}
