/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.number

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.linknewdevice.impl.LinkNewMobileHandler
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.linknewdevice.CheckCodeSender
import io.element.android.libraries.matrix.api.linknewdevice.LinkMobileStep
import io.element.android.libraries.matrix.api.logs.LoggerTags
import kotlinx.coroutines.launch
import timber.log.Timber

private val tag = LoggerTag("EnterNumberPresenter", LoggerTags.linkNewDevice)

@AssistedInject
class EnterNumberPresenter(
    @Assisted private val navigator: EnterNumberNavigator,
    private val linkNewMobileHandler: LinkNewMobileHandler,
) : Presenter<EnterNumberState> {
    @AssistedFactory
    interface Factory {
        fun create(navigator: EnterNumberNavigator): EnterNumberPresenter
    }

    @Composable
    override fun present(): EnterNumberState {
        val coroutineScope = rememberCoroutineScope()
        var number by remember { mutableStateOf("") }
        var sendingCode by remember<MutableState<AsyncAction<Unit>>> { mutableStateOf(AsyncAction.Uninitialized) }

        // Observe the flow to react on ErrorType.InvalidCheckCode
        val linkMobileStep by linkNewMobileHandler.stepFlow.collectAsState()

        var checkCodeSender: CheckCodeSender? by remember { mutableStateOf(null) }

        LaunchedEffect(linkMobileStep) {
            when (val step = linkMobileStep) {
                is LinkMobileStep.QrScanned -> {
                    checkCodeSender = step.checkCodeSender
                }
                else -> Unit
            }
        }

        fun handleEvent(event: EnterNumberEvent) {
            when (event) {
                is EnterNumberEvent.UpdateNumber -> {
                    sendingCode = AsyncAction.Uninitialized
                    // Keep only digits as a safety measure
                    number = event.number.filter { it.isDigit() }
                }
                EnterNumberEvent.Continue -> coroutineScope.launch {
                    // Get the current code sender
                    val sender = checkCodeSender
                    if (sender == null) {
                        Timber.tag(tag.value).e("No check code sender available")
                        sendingCode = AsyncAction.Failure(IllegalStateException("No check code sender available"))
                    } else {
                        sendingCode = AsyncAction.Loading
                        val uByte = number.toUByte()
                        val isValid = sender.validate(uByte)
                        if (isValid) {
                            sender.send(uByte)
                                .fold(
                                    onSuccess = {
                                        Timber.tag(tag.value).d("Code sent successfully")
                                        // Keep loading, do not set sendingCode to AsyncAction.Success(Unit)
                                    },
                                    onFailure = {
                                        Timber.tag(tag.value).e(it, "Failed to send number code")
                                        sendingCode = AsyncAction.Failure(it)
                                    }
                                )
                        } else {
                            // Navigate to the error state
                            navigator.navigateToWrongNumberError()
                        }
                    }
                }
            }
        }

        return EnterNumberState(
            number = number,
            sendingCode = sendingCode,
            eventSink = ::handleEvent,
        )
    }
}
