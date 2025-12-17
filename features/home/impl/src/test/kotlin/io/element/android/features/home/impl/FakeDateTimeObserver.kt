/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import io.element.android.libraries.androidutils.system.DateTimeObserver
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeDateTimeObserver : DateTimeObserver {
    override val changes = MutableSharedFlow<DateTimeObserver.Event>(extraBufferCapacity = 1)

    fun given(event: DateTimeObserver.Event) {
        changes.tryEmit(event)
    }
}
