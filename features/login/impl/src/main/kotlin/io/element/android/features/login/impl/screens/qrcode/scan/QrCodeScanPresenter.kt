/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.login.impl.screens.qrcode.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class QrCodeScanPresenter @Inject constructor(
    private val authenticationService: MatrixAuthenticationService,
) : Presenter<QrCodeScanState> {

    @Composable
    override fun present(): QrCodeScanState {
        val coroutineScope = rememberCoroutineScope()
        var isScanning by remember { mutableStateOf(true) }
        val authenticationAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        fun handleEvents(event: QrCodeScanEvents) {
            when (event) {
                QrCodeScanEvents.TryAgain -> {
                    isScanning = true
                    authenticationAction.value = AsyncAction.Uninitialized
                }
                is QrCodeScanEvents.QrCodeScanned -> {
                    isScanning = false
                    coroutineScope.authenticateWithCode(authenticationAction, event.code)
                }
            }
        }

        return QrCodeScanState(
            isScanning = isScanning,
            authenticationAction = authenticationAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.authenticateWithCode(authenticationAction: MutableState<AsyncAction<Unit>>, code: String) = launch {
        suspend {
            // TODO Call the SDK API, for now, just simulate an error
            delay(1000)
            throw Exception("Simulated error $code")
        }.runCatchingUpdatingState(authenticationAction)
    }
}
