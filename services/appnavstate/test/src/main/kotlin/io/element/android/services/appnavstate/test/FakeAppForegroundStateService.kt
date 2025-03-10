/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.test

import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppForegroundStateService(
    initialForegroundValue: Boolean = true,
    initialIsInCallValue: Boolean = false,
    initialIsSyncingNotificationEventValue: Boolean = false
) : AppForegroundStateService {
    override val isInForeground = MutableStateFlow(initialForegroundValue)
    override val isInCall = MutableStateFlow(initialIsInCallValue)
    override val isSyncingNotificationEvent = MutableStateFlow(initialIsSyncingNotificationEventValue)

    override fun startObservingForeground() {
        // No-op
    }

    fun givenIsInForeground(isInForeground: Boolean) {
        this.isInForeground.value = isInForeground
    }

    override fun updateIsInCallState(isInCall: Boolean) {
        this.isInCall.value = isInCall
    }

    override fun updateIsSyncingNotificationEvent(isSyncingNotificationEvent: Boolean) {
        this.isSyncingNotificationEvent.value = isSyncingNotificationEvent
    }
}
