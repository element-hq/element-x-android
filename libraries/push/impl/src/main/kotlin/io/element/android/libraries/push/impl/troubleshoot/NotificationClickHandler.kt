/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.troubleshoot

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@SingleIn(AppScope::class)
@Inject
class NotificationClickHandler {
    private val _state = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val state: SharedFlow<Unit> = _state

    fun handleNotificationClick() {
        _state.tryEmit(Unit)
    }
}
