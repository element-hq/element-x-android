/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.ElementCallConfig
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.CurrentCall
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.features.call.impl.notifications.RingingCallNotificationCreator
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.ui.media.ImageLoaderHolder
import io.element.android.libraries.push.api.notifications.ForegroundServiceType
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.api.notifications.OnMissedCallNotificationHandler
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * Manages the active call state.
 */
interface ActiveCallManager {
    /**
     * The active call state flow, which will be updated when the active call changes.
     */
    val activeCall: StateFlow<ActiveCall?>

    /**
     * Registers an incoming call if there isn't an existing active call and posts a [CallState.Ringing] notification.
     * @param notificationData The data for the incoming call notification.
     */
    suspend fun registerIncomingCall(notificationData: CallNotificationData)

    /**
     * Called when the active call has been hung up. It will remove any existing UI and the active call.
     * @param callType The type of call that the user hung up, either an external url one or a room one.
     */
    suspend fun hungUpCall(callType: CallType)

    /**
     * Called after the user joined a call. It will remove any existing UI and set the call state as [CallState.InCall].
     *
     * @param callType The type of call that the user joined, either an external url one or a room one.
     */
    suspend fun joinedCall(callType: CallType)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultActiveCallManager @Inject constructor(
    @ApplicationContext context: Context,
    @AppCoroutineScope
    private val coroutineScope: CoroutineScope,
    private val onMissedCallNotificationHandler: OnMissedCallNotificationHandler,
    private val ringingCallNotificationCreator: RingingCallNotificationCreator,
    private val notificationManagerCompat: NotificationManagerCompat,
    private val matrixClientProvider: MatrixClientProvider,
    private val defaultCurrentCallService: DefaultCurrentCallService,
    private val appForegroundStateService: AppForegroundStateService,
    private val imageLoaderHolder: ImageLoaderHolder,
) : ActiveCallManager {
    private val tag = "DefaultActiveCallManager"
    private var timedOutCallJob: Job? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val activeWakeLock: PowerManager.WakeLock? = context.getSystemService<PowerManager>()
        ?.takeIf { it.isWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK) }
        ?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${context.packageName}:IncomingCallWakeLock")

    override val activeCall = MutableStateFlow<ActiveCall?>(null)

    private val mutex = Mutex()

    init {
        observeRingingCall()
        observeCurrentCall()
    }

    override suspend fun registerIncomingCall(notificationData: CallNotificationData) {
        mutex.withLock {
            appForegroundStateService.updateHasRingingCall(true)
            Timber.tag(tag).d("Received incoming call for room id: ${notificationData.roomId}")
            if (activeCall.value != null) {
                displayMissedCallNotification(notificationData)
                Timber.tag(tag).w("Already have an active call, ignoring incoming call: $notificationData")
                return
            }
            activeCall.value = ActiveCall(
                callType = CallType.RoomCall(
                    sessionId = notificationData.sessionId,
                    roomId = notificationData.roomId,
                ),
                callState = CallState.Ringing(notificationData),
            )

            timedOutCallJob = coroutineScope.launch {
                setUpCoil(notificationData.sessionId)
                showIncomingCallNotification(notificationData)

                // Wait for the ringing call to time out
                delay(ElementCallConfig.RINGING_CALL_DURATION_SECONDS.seconds)
                incomingCallTimedOut(displayMissedCallNotification = true)
            }

            // Acquire a wake lock to keep the device awake during the incoming call, so we can process the room info data
            if (activeWakeLock?.isHeld == false) {
                Timber.tag(tag).d("Acquiring partial wakelock")
                activeWakeLock.acquire(ElementCallConfig.RINGING_CALL_DURATION_SECONDS * 1000L)
            }
        }
    }

    @OptIn(DelicateCoilApi::class)
    private suspend fun setUpCoil(sessionId: SessionId) {
        val matrixClient = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return
        // Ensure that the image loader is set, else the IncomingCallActivity will not be able to render the caller avatar
        SingletonImageLoader.setUnsafe(imageLoaderHolder.get(matrixClient))
    }

    /**
     * Called when the incoming call timed out. It will remove the active call and remove any associated UI, adding a 'missed call' notification.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun incomingCallTimedOut(displayMissedCallNotification: Boolean) = mutex.withLock {
        Timber.tag(tag).d("Incoming call timed out")

        val previousActiveCall = activeCall.value ?: return
        val notificationData = (previousActiveCall.callState as? CallState.Ringing)?.notificationData ?: return
        activeCall.value = null
        if (activeWakeLock?.isHeld == true) {
            Timber.tag(tag).d("Releasing partial wakelock after timeout")
            activeWakeLock.release()
        }

        cancelIncomingCallNotification()

        if (displayMissedCallNotification) {
            displayMissedCallNotification(notificationData)
        }
    }

    override suspend fun hungUpCall(callType: CallType) = mutex.withLock {
        if (activeCall.value?.callType != callType) {
            Timber.tag(tag).w("Call type $callType does not match the active call type, ignoring")
            return
        }

        Timber.tag(tag).d("Hung up call: $callType")

        cancelIncomingCallNotification()
        if (activeWakeLock?.isHeld == true) {
            Timber.tag(tag).d("Releasing partial wakelock after hang up")
            activeWakeLock.release()
        }
        timedOutCallJob?.cancel()
        activeCall.value = null
    }

    override suspend fun joinedCall(callType: CallType) = mutex.withLock {
        Timber.tag(tag).d("Joined call: $callType")

        cancelIncomingCallNotification()
        if (activeWakeLock?.isHeld == true) {
            Timber.tag(tag).d("Releasing partial wakelock after joining call")
            activeWakeLock.release()
        }
        timedOutCallJob?.cancel()

        activeCall.value = ActiveCall(
            callType = callType,
            callState = CallState.InCall,
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun showIncomingCallNotification(notificationData: CallNotificationData) {
        Timber.tag(tag).d("Displaying ringing call notification")
        val notification = ringingCallNotificationCreator.createNotification(
            sessionId = notificationData.sessionId,
            roomId = notificationData.roomId,
            eventId = notificationData.eventId,
            senderId = notificationData.senderId,
            roomName = notificationData.roomName,
            senderDisplayName = notificationData.senderName ?: notificationData.senderId.value,
            roomAvatarUrl = notificationData.avatarUrl,
            notificationChannelId = notificationData.notificationChannelId,
            timestamp = notificationData.timestamp,
            textContent = notificationData.textContent,
        ) ?: return
        runCatchingExceptions {
            notificationManagerCompat.notify(
                NotificationIdProvider.getForegroundServiceNotificationId(ForegroundServiceType.INCOMING_CALL),
                notification,
            )
        }.onFailure {
            Timber.e(it, "Failed to publish notification for incoming call")
        }
    }

    private fun cancelIncomingCallNotification() {
        appForegroundStateService.updateHasRingingCall(false)
        Timber.tag(tag).d("Ringing call notification cancelled")
        notificationManagerCompat.cancel(NotificationIdProvider.getForegroundServiceNotificationId(ForegroundServiceType.INCOMING_CALL))
    }

    private fun displayMissedCallNotification(notificationData: CallNotificationData) {
        Timber.tag(tag).d("Displaying missed call notification")
        coroutineScope.launch {
            onMissedCallNotificationHandler.addMissedCallNotification(
                sessionId = notificationData.sessionId,
                roomId = notificationData.roomId,
                eventId = notificationData.eventId,
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRingingCall() {
        // This will observe ringing calls and ensure they're terminated if the room call is cancelled or if the user
        // has joined the call from another session.
        activeCall
            .filterNotNull()
            .filter { it.callState is CallState.Ringing && it.callType is CallType.RoomCall }
            .flatMapLatest { activeCall ->
                val callType = activeCall.callType as CallType.RoomCall
                // Get a flow of updated `hasRoomCall` and `activeRoomCallParticipants` values for the room
                val room = matrixClientProvider.getOrRestore(callType.sessionId).getOrNull()?.getRoom(callType.roomId) ?: run {
                    Timber.tag(tag).d("Couldn't find room for incoming call: $activeCall")
                    return@flatMapLatest flowOf()
                }
                room.roomInfoFlow.map {
                    Timber.tag(tag).d("Has room call status changed for ringing call: ${it.hasRoomCall}")
                    it.hasRoomCall to (callType.sessionId in it.activeRoomCallParticipants)
                }
            }
            // We only want to check if the room active call status changes
            .distinctUntilChanged()
            // Skip the first one, we're not interested in it (if the check below passes, it had to be active anyway)
            .drop(1)
            .onEach { (roomHasActiveCall, userIsInTheCall) ->
                if (!roomHasActiveCall) {
                    // The call was cancelled
                    timedOutCallJob?.cancel()
                    incomingCallTimedOut(displayMissedCallNotification = true)
                } else if (userIsInTheCall) {
                    // The user joined the call from another session
                    timedOutCallJob?.cancel()
                    incomingCallTimedOut(displayMissedCallNotification = false)
                }
            }
            .launchIn(coroutineScope)
    }

    private fun observeCurrentCall() {
        activeCall
            .onEach { value ->
                if (value == null) {
                    defaultCurrentCallService.onCallEnded()
                } else {
                    when (value.callState) {
                        is CallState.Ringing -> {
                            // Nothing to do
                        }
                        is CallState.InCall -> {
                            when (val callType = value.callType) {
                                is CallType.ExternalUrl -> defaultCurrentCallService.onCallStarted(CurrentCall.ExternalUrl(callType.url))
                                is CallType.RoomCall -> defaultCurrentCallService.onCallStarted(CurrentCall.RoomCall(callType.roomId))
                            }
                        }
                    }
                }
            }
            .launchIn(coroutineScope)
    }
}

/**
 * Represents an active call.
 */
data class ActiveCall(
    val callType: CallType,
    val callState: CallState,
)

/**
 * Represents the state of an active call.
 */
sealed interface CallState {
    /**
     * The call is in a ringing state.
     * @param notificationData The data for the incoming call notification.
     */
    data class Ringing(val notificationData: CallNotificationData) : CallState

    /**
     * The call is in an in-call state.
     */
    data object InCall : CallState
}
