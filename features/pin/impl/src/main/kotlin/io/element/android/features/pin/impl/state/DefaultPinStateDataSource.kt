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

package io.element.android.features.pin.impl.state

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.pin.api.PinState
import io.element.android.features.pin.api.PinStateDataSource
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultPinStateDataSource @Inject constructor() : PinStateDataSource {

    private val _pinState = MutableStateFlow<PinState>(PinState.Locked)
    override val pinState: StateFlow<PinState> = _pinState

    override fun unlock() {
        _pinState.value = PinState.Unlocked
    }

    override fun lock() {
        _pinState.value = PinState.Locked
    }
}
