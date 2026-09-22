/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import io.element.android.call.api.ElementCallData
import io.element.android.call.api.ElementCallLifecycleListener
import io.element.android.features.call.api.CallData
import io.element.android.features.call.api.CurrentCall
import io.element.android.features.call.api.CurrentCallTracker
import io.element.android.features.call.api.RingingCallTracker
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Reports a native call back to the parts of Element X that already track calls.
 *
 * The library knows it is in a call; Element X's room list, timeline and ringing machinery each
 * already know how to react to one. This joins the two, so that a native call looks like any other
 * call to the rest of the app rather than needing every reader to learn about it.
 */
class ElementXCallLifecycleListener(
    private val sessionId: SessionId,
    private val currentCallTracker: CurrentCallTracker,
    private val ringingCallTracker: RingingCallTracker,
    private val sessionCoroutineScope: CoroutineScope,
    appForegroundStateService: AppForegroundStateService,
) : ElementCallLifecycleListener {
    override val isAppInForeground: Flow<Boolean> = appForegroundStateService.isInForeground

    override fun onCallStarted(callData: ElementCallData) {
        currentCallTracker.onCallStarted(CurrentCall.RoomCall(callData.roomId()))
    }

    override fun onCallJoined(callData: ElementCallData) {
        // The ringing side is suspending and these callbacks are not, so the work is handed to the
        // session scope rather than blocking the controller's. It outlives the call on purpose: a
        // ring still has to be taken down even if the call ends while this is in flight.
        sessionCoroutineScope.launch { ringingCallTracker.onCallJoined(callData.toCallData()) }
    }

    override fun onCallEnded(callData: ElementCallData?) {
        currentCallTracker.onCallEnded()
        callData?.let { sessionCoroutineScope.launch { ringingCallTracker.onCallEnded(it.toCallData()) } }
    }

    private fun ElementCallData.roomId() = RoomId(roomId.value)

    private fun ElementCallData.toCallData() = CallData(
        sessionId = sessionId,
        roomId = roomId(),
        isAudioCall = isAudioCall,
    )
}
