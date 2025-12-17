/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush.registration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

data class RegistrationResult(
    val clientSecret: String,
    val result: Result<Unit>,
)

@SingleIn(AppScope::class)
@Inject
class EndpointRegistrationHandler {
    private val _state = MutableSharedFlow<RegistrationResult>()
    val state: SharedFlow<RegistrationResult> = _state

    suspend fun registrationDone(result: RegistrationResult) {
        _state.emit(result)
    }
}
