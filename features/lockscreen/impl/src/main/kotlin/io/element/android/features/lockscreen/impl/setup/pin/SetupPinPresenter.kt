/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.setup.pin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.lockscreen.impl.LockScreenConfig
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.features.lockscreen.impl.setup.pin.validation.PinValidator
import io.element.android.features.lockscreen.impl.setup.pin.validation.SetupPinFailure
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import kotlinx.coroutines.delay

/**
 * Some time for the ui to refresh before showing confirmation step.
 */
private const val DELAY_BEFORE_CONFIRMATION_STEP_IN_MILLIS = 100L

@Inject
class SetupPinPresenter(
    private val lockScreenConfig: LockScreenConfig,
    private val pinValidator: PinValidator,
    private val buildMeta: BuildMeta,
    private val pinCodeManager: PinCodeManager,
) : Presenter<SetupPinState> {
    @Composable
    override fun present(): SetupPinState {
        var choosePinEntry by remember {
            mutableStateOf(PinEntry.createEmpty(lockScreenConfig.pinSize))
        }
        var confirmPinEntry by remember {
            mutableStateOf(PinEntry.createEmpty(lockScreenConfig.pinSize))
        }
        var isConfirmationStep by remember {
            mutableStateOf(false)
        }
        var setupPinFailure by remember {
            mutableStateOf<SetupPinFailure?>(null)
        }
        LaunchedEffect(choosePinEntry) {
            if (choosePinEntry.isComplete()) {
                when (val pinValidationResult = pinValidator.isPinValid(choosePinEntry)) {
                    is PinValidator.Result.Invalid -> {
                        setupPinFailure = pinValidationResult.failure
                    }
                    PinValidator.Result.Valid -> {
                        delay(DELAY_BEFORE_CONFIRMATION_STEP_IN_MILLIS)
                        isConfirmationStep = true
                    }
                }
            }
        }

        LaunchedEffect(confirmPinEntry) {
            if (confirmPinEntry.isComplete()) {
                if (confirmPinEntry == choosePinEntry) {
                    pinCodeManager.createPinCode(confirmPinEntry.toText())
                } else {
                    setupPinFailure = SetupPinFailure.PinsDoNotMatch
                }
            }
        }

        fun handleEvent(event: SetupPinEvents) {
            when (event) {
                is SetupPinEvents.OnPinEntryChanged -> {
                    // Use the fromConfirmationStep flag from ui to avoid race condition.
                    if (event.fromConfirmationStep) {
                        confirmPinEntry = confirmPinEntry.fillWith(event.entryAsText)
                    } else {
                        choosePinEntry = choosePinEntry.fillWith(event.entryAsText)
                    }
                }
                SetupPinEvents.ClearFailure -> {
                    when (setupPinFailure) {
                        is SetupPinFailure.PinsDoNotMatch -> {
                            choosePinEntry = choosePinEntry.clear()
                            confirmPinEntry = confirmPinEntry.clear()
                        }
                        is SetupPinFailure.ForbiddenPin -> {
                            choosePinEntry = choosePinEntry.clear()
                        }
                        null -> Unit
                    }
                    isConfirmationStep = false
                    setupPinFailure = null
                }
            }
        }

        return SetupPinState(
            choosePinEntry = choosePinEntry,
            confirmPinEntry = confirmPinEntry,
            isConfirmationStep = isConfirmationStep,
            setupPinFailure = setupPinFailure,
            appName = buildMeta.applicationName,
            eventSink = ::handleEvent,
        )
    }
}
