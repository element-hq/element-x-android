/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
