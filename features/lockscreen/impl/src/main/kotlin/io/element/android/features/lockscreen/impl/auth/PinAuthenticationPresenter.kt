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

package io.element.android.features.lockscreen.impl.auth

import androidx.compose.runtime.Composable
import io.element.android.features.lockscreen.api.LockScreenStateService
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class PinAuthenticationPresenter @Inject constructor(
    private val pinStateService: LockScreenStateService,
    private val coroutineScope: CoroutineScope,
) : Presenter<PinAuthenticationState> {

    @Composable
    override fun present(): PinAuthenticationState {

        fun handleEvents(event: PinAuthenticationEvents) {
            when (event) {
                PinAuthenticationEvents.Unlock -> coroutineScope.launch { pinStateService.unlock() }
            }
        }
        return PinAuthenticationState(
            eventSink = ::handleEvents
        )
    }
}
