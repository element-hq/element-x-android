/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.detection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.rageshake.api.detection.RageshakeDetectionEvents
import io.element.android.features.rageshake.api.detection.RageshakeDetectionPresenter
import io.element.android.features.rageshake.api.detection.RageshakeDetectionState
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesEvents
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesPresenter
import io.element.android.features.rageshake.api.screenshot.ImageResult
import io.element.android.features.rageshake.impl.rageshake.RageShake
import io.element.android.features.rageshake.impl.screenshot.ScreenshotHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesBinding(AppScope::class)
class DefaultRageshakeDetectionPresenter(
    private val screenshotHolder: ScreenshotHolder,
    private val rageShake: RageShake,
    private val preferencesPresenter: RageshakePreferencesPresenter,
) : RageshakeDetectionPresenter {
    @Composable
    override fun present(): RageshakeDetectionState {
        val localCoroutineScope = rememberCoroutineScope()
        val preferencesState = preferencesPresenter.present()
        val isStarted = rememberSaveable {
            mutableStateOf(false)
        }
        val takeScreenshot = rememberSaveable {
            mutableStateOf(false)
        }
        val showDialog = rememberSaveable {
            mutableStateOf(false)
        }

        fun handleEvent(event: RageshakeDetectionEvents) {
            when (event) {
                RageshakeDetectionEvents.Disable -> {
                    preferencesState.eventSink(RageshakePreferencesEvents.SetIsEnabled(false))
                    showDialog.value = false
                }
                RageshakeDetectionEvents.StartDetection -> isStarted.value = true
                RageshakeDetectionEvents.StopDetection -> isStarted.value = false
                is RageshakeDetectionEvents.ProcessScreenshot -> localCoroutineScope.processScreenshot(takeScreenshot, showDialog, event.imageResult)
                RageshakeDetectionEvents.Dismiss -> showDialog.value = false
            }
        }

        val state = remember(preferencesState, isStarted.value, takeScreenshot.value, showDialog.value) {
            RageshakeDetectionState(
                isStarted = isStarted.value,
                takeScreenshot = takeScreenshot.value,
                showDialog = showDialog.value,
                preferenceState = preferencesState,
                eventSink = ::handleEvent,
            )
        }

        LaunchedEffect(preferencesState.sensitivity) {
            rageShake.setSensitivity(preferencesState.sensitivity)
        }
        val shouldStart = preferencesState.isFeatureEnabled &&
            preferencesState.isEnabled &&
            preferencesState.isSupported &&
            isStarted.value &&
            !takeScreenshot.value &&
            !showDialog.value

        LaunchedEffect(shouldStart) {
            handleRageShake(shouldStart, state, takeScreenshot)
        }
        return state
    }

    private fun handleRageShake(start: Boolean, state: RageshakeDetectionState, takeScreenshot: MutableState<Boolean>) {
        if (start) {
            rageShake.start(state.preferenceState.sensitivity)
            rageShake.setInterceptor {
                takeScreenshot.value = true
            }
        } else {
            rageShake.stop()
            rageShake.setInterceptor(null)
        }
    }

    private fun CoroutineScope.processScreenshot(takeScreenshot: MutableState<Boolean>, showDialog: MutableState<Boolean>, imageResult: ImageResult) = launch {
        screenshotHolder.reset()
        when (imageResult) {
            is ImageResult.Error -> {
                Timber.e(imageResult.exception, "Unable to write screenshot")
            }
            is ImageResult.Success -> {
                screenshotHolder.writeBitmap(imageResult.data)
            }
        }
        takeScreenshot.value = false
        showDialog.value = true
    }
}
