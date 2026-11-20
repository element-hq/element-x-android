/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.rtc

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rtc.BackendSubscription
import org.matrix.rtc.FfiBackendException
import org.matrix.rtc.FfiEventIn
import org.matrix.rtc.FfiLiveKitTokenRequest
import org.matrix.rtc.FfiOpenIdToken
import org.matrix.rtc.FfiRoomSubjects
import org.matrix.rtc.FfiToDeviceDelivery
import org.matrix.rtc.FfiToDeviceRecipient
import org.matrix.rtc.MatrixBackend
import org.matrix.rtc.RoomSink
import org.matrix.rtc.ToDeviceSink
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.DelayedEventUpdateAction
import org.matrix.rustcomponents.sdk.RequestConfigOverride
import org.matrix.rustcomponents.sdk.Room
import timber.log.Timber

/**
 * The RTC core's [MatrixBackend] over the session's Rust SDK [Client], handed to `ElementCallStack.Builder`.
 *
 * Ported from element-call-android's reference backend (`sample/…/matrix/SdkMatrixBackend.kt`, v0.1.2),
 * which the library no longer ships: compare against it when bumping the library. Unlike the reference, delayed events
 * and `get_token` go through the SDK bindings, which have them since sdk-android 26.11.17.
 *
 * One per Matrix session, alive between calls.
 *
 * @param client the logged-in SDK client the session runs on.
 * @param sessionScope lives as long as the Matrix session; the core's subscriptions run in it until it
 * cancels them.
 * @param dispatchers where SDK calls run.
 */
class SdkMatrixBackend(
    private val client: Client,
    private val sessionScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
) : MatrixBackend {
    private val userId = client.userId()
    private val deviceId = client.deviceId()

    /** The core retries a failed send itself, within its own budget, so the SDK must not retry underneath it. */
    private val noRetry = RequestConfigOverride(retryLimit = 0uL, timeout = null)

    override fun ownUserId(): String = userId

    override fun ownDeviceId(): String = deviceId

    override suspend fun sendStickyEvent(roomId: String, eventType: String, contentJson: String, durationMs: ULong): String =
        send("sendStickyEvent($eventType)") { room(roomId).use { it.sendStickyRaw(eventType, contentJson, durationMs, noRetry) } }

    /**
     * MSC4140, the call's dead man's switch. A homeserver without it answers `M_UNRECOGNIZED`, and the core then joins
     * without one, never asks again this session, and shortens the membership's lifetime instead.
     */
    override suspend fun sendDelayedStateEvent(roomId: String, eventType: String, stateKey: String, contentJson: String, delayMs: ULong): String =
        send("sendDelayedStateEvent($eventType)") {
            room(roomId).use { it.sendDelayedStateEvent(eventType, stateKey, contentJson, delayMs, noRetry) }
        }

    override suspend fun sendDelayedStickyEvent(
        roomId: String,
        eventType: String,
        contentJson: String,
        stickyDurationMs: ULong,
        delayMs: ULong,
    ): String = send("sendDelayedStickyEvent($eventType)") {
        room(roomId).use {
            // Not the core's argument order: the delay comes before the sticky duration here.
            it.sendStickyDelayedEvent(eventType, contentJson, delayMs = delayMs, stickyDurationMs = stickyDurationMs, requestConfigOverride = noRetry)
        }
    }

    // A delayed event is the client's, not the room's: the SDK needs only its id.
    override suspend fun restartDelayedEvent(roomId: String, delayId: String) =
        send("restartDelayedEvent") { client.updateDelayedEvent(delayId, DelayedEventUpdateAction.RESTART, noRetry) }

    override suspend fun cancelDelayedEvent(roomId: String, delayId: String) =
        send("cancelDelayedEvent") { client.updateDelayedEvent(delayId, DelayedEventUpdateAction.CANCEL, noRetry) }

    override suspend fun sendStateEvent(roomId: String, eventType: String, stateKey: String, contentJson: String): String =
        send("sendStateEvent($eventType)") { room(roomId).use { it.sendStateEventRaw(eventType, stateKey, contentJson, noRetry) } }

    override suspend fun sendRoomEvent(roomId: String, eventType: String, contentJson: String): String =
        send("sendRoomEvent($eventType)") { room(roomId).use { it.sendRaw(eventType, contentJson, noRetry) } }

    override suspend fun redactEvent(roomId: String, eventId: String, reason: String?) =
        send("redactEvent") { room(roomId).use { it.redact(eventId, reason) } }

    override suspend fun sendToDeviceMessage(
        recipients: List<FfiToDeviceRecipient>,
        messageType: String,
        contentJson: String,
    ): List<FfiToDeviceDelivery> = send("sendToDeviceMessage($messageType to [${recipients.joinToString { "${it.userId}/${it.deviceId}" }}])") {
        val devices = recipients.groupBy(keySelector = { it.userId }, valueTransform = { it.deviceId })
        val failures = client.sendEncryptedToDeviceMessage(messageType, devices, contentJson).failures
        if (failures.isNotEmpty()) Timber.w("MatrixRTC: to-device $messageType not delivered to $failures")
        recipients.toDeliveries(failures)
    }

    override fun subscribeRoom(roomId: String, subjects: FfiRoomSubjects, sink: RoomSink): BackendSubscription {
        val room = room(roomId)
        Timber.i("MatrixRTC: subscribing $roomId to state ${subjects.stateEventTypes}, timeline ${subjects.timelineEventTypes}")
        val job = sessionScope.launch {
            deliver("sticky events", room.stickyEventSets(dispatchers), sink::onStickyEvents)
            for (type in subjects.stateEventTypes) {
                deliver("state $type", room.stateEventSets(type, dispatchers)) { sink.onStateEvents(type, it) }
            }
            deliver("joined members", room.joinedMemberIds(dispatchers), sink::onJoinedMembers)
            deliver("encryption", room.encryptionUpdates(dispatchers), sink::onEncryption)
            for (type in subjects.timelineEventTypes - REDACTION_EVENT_TYPE) {
                deliver("timeline $type", room.messageLikeEvents(type, dispatchers)) { sink.onTimelineEvents(listOf(it)) }
            }
            deliver("redactions", room.redactedEventIds(dispatchers), sink::onRedaction)
        }
        job.invokeOnCompletion { room.close() }
        return JobSubscription(job)
    }

    override fun subscribeToDevice(eventTypes: List<String>, sink: ToDeviceSink): BackendSubscription =
        JobSubscription(sessionScope.deliver("to-device $eventTypes", client.toDeviceMessages(eventTypes, dispatchers), sink::onToDeviceMessage))

    /**
     * `Room.loadOrFetchEventWithRelations` returns typed content, with neither raw JSON nor encryption info, so a hand raised
     * before we joined shows only once it changes. None rather than a failure, which is how the core asks a backend that cannot
     * serve them to say so.
     */
    override suspend fun relations(roomId: String, eventId: String, relType: String, eventType: String): List<FfiEventIn> = emptyList()

    override suspend fun openidToken(): FfiOpenIdToken = send("openidToken") {
        val token = client.requestOpenidToken()
        FfiOpenIdToken(
            accessToken = token.accessToken,
            tokenType = token.tokenType,
            matrixServerName = token.matrixServerName,
            expiresInSecs = token.expiresInSeconds,
        )
    }

    /**
     * MSC4195's `get_token`, needed by every join in the MSC membership format (chosen in the call's Developer options)
     * and by a transport naming an SFU `url` alone; a transport with a `livekit_service_url` takes its token from that
     * service and never asks. A homeserver without the endpoint answers `M_UNRECOGNIZED`.
     */
    override suspend fun livekitToken(request: FfiLiveKitTokenRequest): String = send("livekitToken(${request.url})") {
        client.rtcLivekitGetToken(
            url = request.url,
            roomId = request.roomId,
            slotId = request.slotId,
            memberId = request.memberId,
            serverName = request.serverName,
        )
    }

    /** The SDK falls back to the well-known when the homeserver lacks the MSC4143 endpoint; neither answering is none. */
    override suspend fun rtcTransports(): String = send("rtcTransports") { client.discoverRtcTransports().orEmpty().toJson() }

    /** The caller closes it. */
    private fun room(roomId: String): Room {
        // Final, and no Matrix code says so: inventing one would be a lie about the homeserver.
        return client.getRoom(roomId)
            ?: throw FfiBackendException.Failed(errcode = null, status = null, retryAfterMs = null, permanent = true, reason = "Not a joined room: $roomId")
    }

    /** Off uniffi's thread, logged on entry, and failing as the core reads failures. */
    private suspend fun <T> send(description: String, block: suspend () -> T): T {
        Timber.i("MatrixRTC backend: $description")
        return try {
            withContext(dispatchers.io) { block() }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Timber.w(throwable, "MatrixRTC backend: $description failed")
            throw throwable.toFfiBackendException()
        }
    }

    /** Each subject on its own, and a failing one only ends itself: it never reaches the session scope. */
    private fun <T> CoroutineScope.deliver(what: String, flow: Flow<T>, block: (T) -> Unit): Job =
        launch(dispatchers.io) {
            runCatchingExceptions { flow.collect(block) }
                .onFailure { Timber.w(it, "MatrixRTC backend: the $what feed ended") }
        }

    private class JobSubscription(private val job: Job) : BackendSubscription {
        override fun cancel() = job.cancel()
    }
}
