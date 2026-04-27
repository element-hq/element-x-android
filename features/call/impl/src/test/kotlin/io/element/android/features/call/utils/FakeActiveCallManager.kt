/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.utils

import io.element.android.features.call.api.CallData
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.features.call.impl.utils.ActiveCall
import io.element.android.features.call.impl.utils.ActiveCallManager
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.MutableStateFlow

class FakeActiveCallManager(
    var registerIncomingCallResult: (CallNotificationData) -> Unit = {},
    var hangUpCallResult: (CallData, CallNotificationData?) -> Unit = { _, _ -> },
    var joinedCallResult: (CallData) -> Unit = {},
) : ActiveCallManager {
    override val activeCall = MutableStateFlow<ActiveCall?>(null)

    override suspend fun registerIncomingCall(notificationData: CallNotificationData) = simulateLongTask {
        registerIncomingCallResult(notificationData)
    }

    override suspend fun hangUpCall(callData: CallData, notificationData: CallNotificationData?) = simulateLongTask {
        hangUpCallResult(callData, notificationData)
    }

    override suspend fun joinedCall(callData: CallData) = simulateLongTask {
        joinedCallResult(callData)
    }

    fun setActiveCall(value: ActiveCall?) {
        this.activeCall.value = value
    }
}
