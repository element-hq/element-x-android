/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.features.login.impl.qrcode.QrCodeLoginManager
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginDataFactory
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.auth.qrlogin.QrLoginException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class QrCodeScanPresenter @Inject constructor(
    private val qrCodeLoginDataFactory: MatrixQrCodeLoginDataFactory,
    private val qrCodeLoginManager: QrCodeLoginManager,
    private val coroutineDispatchers: CoroutineDispatchers,
) : Presenter<QrCodeScanState> {
    private var isScanning by mutableStateOf(true)

    private val isProcessingCode = AtomicBoolean(false)

    @Composable
    override fun present(): QrCodeScanState {
        val coroutineScope = rememberCoroutineScope()
        val authenticationAction: MutableState<AsyncAction<MatrixQrCodeLoginData>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        ObserveQRCodeLoginFailures {
            authenticationAction.value = AsyncAction.Failure(it)
        }

        fun handleEvents(event: QrCodeScanEvents) {
            when (event) {
                QrCodeScanEvents.TryAgain -> {
                    isScanning = true
                    authenticationAction.value = AsyncAction.Uninitialized
                }
                is QrCodeScanEvents.QrCodeScanned -> {
                    isScanning = false
                    coroutineScope.getQrCodeData(authenticationAction, event.code)
                }
            }
        }

        return QrCodeScanState(
            isScanning = isScanning,
            authenticationAction = authenticationAction.value,
            eventSink = ::handleEvents
        )
    }

    @Composable
    private fun ObserveQRCodeLoginFailures(onQrCodeLoginError: (QrLoginException) -> Unit) {
        LaunchedEffect(onQrCodeLoginError) {
            qrCodeLoginManager.currentLoginStep
                .onEach { state ->
                    if (state is QrCodeLoginStep.Failed) {
                        onQrCodeLoginError(state.error)
                        // The error was handled here, reset the login state
                        qrCodeLoginManager.reset()
                    }
                }
                .launchIn(this)
        }
    }

    private fun CoroutineScope.getQrCodeData(codeScannedAction: MutableState<AsyncAction<MatrixQrCodeLoginData>>, code: ByteArray) {
        if (codeScannedAction.value.isSuccess() || isProcessingCode.compareAndSet(true, true)) return

        launch(coroutineDispatchers.computation) {
            suspend {
                qrCodeLoginDataFactory.parseQrCodeData(code).onFailure {
                    Timber.e(it, "Error parsing QR code data")
                }.getOrThrow()
            }.runCatchingUpdatingState(codeScannedAction)
        }.invokeOnCompletion {
            isProcessingCode.set(false)
        }
    }
}
