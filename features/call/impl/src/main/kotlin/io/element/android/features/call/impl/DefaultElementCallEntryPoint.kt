/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.call.api.CallData
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.features.call.impl.utils.ActiveCallManager
import io.element.android.features.call.impl.utils.IntentProvider
import io.element.android.features.callnative.api.NativeCallEntryPoint
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesBinding(AppScope::class)
class DefaultElementCallEntryPoint(
    @ApplicationContext private val context: Context,
    private val activeCallManager: ActiveCallManager,
    private val nativeCallEntryPoint: NativeCallEntryPoint,
    private val featureFlagService: FeatureFlagService,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
) : ElementCallEntryPoint {
    companion object {
        const val EXTRA_CALL_TYPE = "EXTRA_CALL_TYPE"
        const val REQUEST_CODE = 2255
    }

    override fun startCall(callData: CallData) {
        // The flag is backed by storage, so it has to be read asynchronously. Caching it in an
        // eagerly started StateFlow would race: a call placed just after app start could be routed
        // on the default value before the real one arrived.
        appCoroutineScope.launch {
            val useNativeCall = featureFlagService.isFeatureEnabled(FeatureFlags.NativeCall)
            Timber.i("startCall: roomId=${callData.roomId}, nativeCall=$useNativeCall")
            if (useNativeCall) {
                nativeCallEntryPoint.startCall(callData)
            } else {
                context.startActivity(IntentProvider.createIntent(context, callData))
            }
        }
    }

    override suspend fun handleIncomingCall(
        callData: CallData,
        eventId: EventId,
        senderId: UserId,
        roomName: String?,
        senderName: String?,
        avatarUrl: String?,
        timestamp: Long,
        expirationTimestamp: Long,
        notificationChannelId: String,
        textContent: String?,
    ) {
        val incomingCallNotificationData = CallNotificationData(
            sessionId = callData.sessionId,
            roomId = callData.roomId,
            eventId = eventId,
            senderId = senderId,
            roomName = roomName,
            senderName = senderName,
            avatarUrl = avatarUrl,
            timestamp = timestamp,
            expirationTimestamp = expirationTimestamp,
            notificationChannelId = notificationChannelId,
            textContent = textContent,
            audioOnly = callData.isAudioCall,
        )
        activeCallManager.registerIncomingCall(notificationData = incomingCallNotificationData)
    }
}
