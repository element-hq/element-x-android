/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.troubleshoot

import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@SingleIn(AppScope::class)
class DiagnosticPushHandler @Inject constructor() {
    private val _state = MutableSharedFlow<Unit>()
    val state: SharedFlow<Unit> = _state

    suspend fun handlePush() {
        _state.emit(Unit)
    }
}
