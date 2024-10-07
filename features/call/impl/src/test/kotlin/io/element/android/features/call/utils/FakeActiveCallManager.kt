/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.utils

import io.element.android.features.call.api.CallType
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.features.call.impl.utils.ActiveCall
import io.element.android.features.call.impl.utils.ActiveCallManager
import kotlinx.coroutines.flow.MutableStateFlow

class FakeActiveCallManager(
    var registerIncomingCallResult: (CallNotificationData) -> Unit = {},
    var incomingCallTimedOutResult: () -> Unit = {},
    var hungUpCallResult: (CallType) -> Unit = {},
    var joinedCallResult: (CallType) -> Unit = {},
) : ActiveCallManager {
    override val activeCall = MutableStateFlow<ActiveCall?>(null)

    override fun registerIncomingCall(notificationData: CallNotificationData) {
        registerIncomingCallResult(notificationData)
    }

    override fun incomingCallTimedOut() {
        incomingCallTimedOutResult()
    }

    override fun hungUpCall(callType: CallType) {
        hungUpCallResult(callType)
    }

    override fun joinedCall(callType: CallType) {
        joinedCallResult(callType)
    }

    fun setActiveCall(value: ActiveCall?) {
        this.activeCall.value = value
    }
}
