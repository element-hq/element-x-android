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

package io.element.android.libraries.pushproviders.unifiedpush.registration

import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

data class RegistrationResult(
    val clientSecret: String,
    val result: Result<Unit>,
)

@SingleIn(AppScope::class)
class EndpointRegistrationHandler @Inject constructor() {
    private val _state = MutableSharedFlow<RegistrationResult>()
    val state: SharedFlow<RegistrationResult> = _state

    suspend fun registrationDone(result: RegistrationResult) {
        _state.emit(result)
    }
}
