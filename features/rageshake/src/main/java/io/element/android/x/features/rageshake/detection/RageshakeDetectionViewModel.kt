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

package io.element.android.x.features.rageshake.detection

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesViewModel
import io.element.android.x.architecture.viewmodel.daggerMavericksViewModelFactory
import io.element.android.x.core.screenshot.ImageResult
import io.element.android.x.di.AppScope
import io.element.android.x.features.rageshake.rageshake.RageShake
import io.element.android.x.features.rageshake.rageshake.RageshakeDataStore
import io.element.android.x.features.rageshake.screenshot.ScreenshotHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesViewModel(AppScope::class)
class RageshakeDetectionViewModel @AssistedInject constructor(
    @Assisted initialState: RageshakeDetectionViewState,
    private val rageshakeDataStore: RageshakeDataStore,
    private val screenshotHolder: ScreenshotHolder,
    private val rageShake: RageShake,
) : MavericksViewModel<RageshakeDetectionViewState>(initialState) {

    companion object :
        MavericksViewModelFactory<RageshakeDetectionViewModel, RageshakeDetectionViewState> by daggerMavericksViewModelFactory()

    init {
        setState {
            copy(
                isSupported = rageShake.isAvailable()
            )
        }
        observeDataStore()
        observeState()
    }

    private fun observeDataStore() {
        viewModelScope.launch {
            rageshakeDataStore.isEnabled().collect { isEnabled ->
                setState {
                    copy(
                        isEnabled = isEnabled
                    )
                }
            }
        }
        viewModelScope.launch {
            rageshakeDataStore.sensitivity().collect { sensitivity ->
                setState {
                    copy(
                        sensitivity = sensitivity
                    )
                }
            }
        }
    }

    private fun observeState() {
        viewModelScope.launch {
            stateFlow
                .map {
                    it.isSupported &&
                        it.isEnabled &&
                        it.isStarted &&
                        !it.takeScreenshot &&
                        !it.showDialog
                }
                .distinctUntilChanged()
                .collect(::handleRageShake)
        }
        viewModelScope.launch {
            stateFlow
                .map {
                    it.sensitivity
                }
                .distinctUntilChanged()
                .collect {
                    rageShake.setSensitivity(it)
                }
        }
    }

    private fun handleRageShake(shouldStart: Boolean) {
        if (shouldStart) {
            withState {
                rageShake.start(it.sensitivity)
            }
            rageShake.interceptor = {
                setState {
                    copy(
                        takeScreenshot = true
                    )
                }
            }
        } else {
            rageShake.stop()
            rageShake.interceptor = null
        }
    }

    fun onScreenshotTaken(imageResult: ImageResult) {
        viewModelScope.launch(Dispatchers.IO) {
            screenshotHolder.reset()
            when (imageResult) {
                is ImageResult.Error -> {
                    Timber.e(imageResult.exception, "Unable to write screenshot")
                }
                is ImageResult.Success -> {
                    screenshotHolder.writeBitmap(imageResult.data)
                }
            }
            setState {
                copy(
                    takeScreenshot = false,
                    showDialog = true,
                )
            }
        }
    }

    fun start() {
        setState {
            copy(isStarted = true)
        }
    }

    private fun onPopupDismissed() {
        setState {
            copy(
                showDialog = false
            )
        }
    }

    fun onNo() {
        onPopupDismissed()
    }

    fun onYes() {
        onPopupDismissed()
    }

    fun onEnableClicked(enabled: Boolean) {
        viewModelScope.launch {
            rageshakeDataStore.setIsEnabled(enabled)
        }
        if (!enabled) {
            onPopupDismissed()
        }
    }

    fun onSensitivityChange(sensitivity: Float) {
        viewModelScope.launch {
            rageshakeDataStore.setSensitivity(sensitivity)
        }
        rageShake.setSensitivity(sensitivity)
    }

    fun stop() {
        setState {
            copy(isStarted = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
        handleRageShake(false)
    }
}
