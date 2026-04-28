/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.channels

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.NotificationSound

class FakeNotificationChannels(
    var channelForIncomingCall: (ring: Boolean) -> String = { _ -> "" },
    var channelIdForMessage: (sessionId: SessionId, noisy: Boolean) -> String = { _, _ -> "" },
    var channelIdForTest: () -> String = { "" },
    var recreateNoisyChannelLambda: (sound: NotificationSound, version: Int) -> Unit = { _, _ -> },
    var recreateRingingCallChannelLambda: (sound: NotificationSound, version: Int) -> Unit = { _, _ -> },
) : NotificationChannels {
    override fun getChannelForIncomingCall(ring: Boolean): String {
        return channelForIncomingCall(ring)
    }

    override fun getChannelIdForMessage(sessionId: SessionId, noisy: Boolean): String {
        return channelIdForMessage(sessionId, noisy)
    }

    override fun getChannelIdForTest(): String {
        return channelIdForTest()
    }

    override fun recreateNoisyChannel(sound: NotificationSound, version: Int) {
        recreateNoisyChannelLambda(sound, version)
    }

    override fun recreateRingingCallChannel(sound: NotificationSound, version: Int) {
        recreateRingingCallChannelLambda(sound, version)
    }
}
