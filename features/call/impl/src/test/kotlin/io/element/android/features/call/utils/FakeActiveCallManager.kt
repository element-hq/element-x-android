/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.utils

import io.element.android.features.call.api.CallType
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.features.call.impl.utils.ActiveCall
import io.element.android.features.call.impl.utils.ActiveCallManager
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.MutableStateFlow

class FakeActiveCallManager(
    var registerIncomingCallResult: (CallNotificationData) -> Unit = {},
    var hungUpCallResult: (CallType) -> Unit = {},
    var joinedCallResult: (CallType) -> Unit = {},
) : ActiveCallManager {
    override val activeCall = MutableStateFlow<ActiveCall?>(null)

    override suspend fun registerIncomingCall(notificationData: CallNotificationData) = simulateLongTask {
        registerIncomingCallResult(notificationData)
    }

    override suspend fun hungUpCall(callType: CallType) = simulateLongTask {
        hungUpCallResult(callType)
    }

    override suspend fun joinedCall(callType: CallType) = simulateLongTask {
        joinedCallResult(callType)
    }

    fun setActiveCall(value: ActiveCall?) {
        this.activeCall.value = value
    }
}
