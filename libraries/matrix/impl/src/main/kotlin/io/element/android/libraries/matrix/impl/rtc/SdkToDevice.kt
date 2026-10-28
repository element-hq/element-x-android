/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.rtc

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOn
import org.matrix.rtc.FfiToDeviceDelivery
import org.matrix.rtc.FfiToDeviceMessageIn
import org.matrix.rtc.FfiToDeviceRecipient
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ToDeviceMessage
import org.matrix.rustcomponents.sdk.ToDeviceMessageListener

/**
 * The to-device messages of [eventTypes] this client receives while collected, decrypted, with their
 * encryption info; the SDK drops what it could not decrypt. Unbounded: a dropped media key is a
 * participant nobody can hear.
 */
internal fun Client.toDeviceMessages(eventTypes: List<String>, dispatchers: CoroutineDispatchers): Flow<FfiToDeviceMessageIn> = mxCallbackFlow {
    subscribeToCustomToDeviceMessages(
        eventTypes,
        object : ToDeviceMessageListener {
            override fun onMessage(message: ToDeviceMessage) {
                trySend(message.toFfi())
            }
        },
    )
}
    .buffer(Channel.UNLIMITED)
    .flowOn(dispatchers.io)

/** The claimed sender is unauthenticated; an encrypted message's attested one is what counts. */
internal fun ToDeviceMessage.toFfi() = FfiToDeviceMessageIn(
    sender = encryptionInfo?.senderId ?: senderId,
    eventType = eventType,
    contentJson = content,
    encryption = encryptionInfo.toFfi(),
)

/** One verdict per recipient, from the SDK's `user id -> device ids` it could not serve: the core re-sends only to those. */
internal fun List<FfiToDeviceRecipient>.toDeliveries(failures: Map<String, List<String>>): List<FfiToDeviceDelivery> = map { recipient ->
    val failed = failures[recipient.userId]?.contains(recipient.deviceId) == true
    FfiToDeviceDelivery(
        userId = recipient.userId,
        deviceId = recipient.deviceId,
        error = if (failed) "The message could not be delivered to this device" else null,
    )
}
