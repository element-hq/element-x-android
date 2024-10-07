/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.channels

class FakeNotificationChannels(
    var channelForIncomingCall: (ring: Boolean) -> String = { _ -> "" },
    var channelIdForMessage: (noisy: Boolean) -> String = { _ -> "" },
    var channelIdForTest: () -> String = { "" }
) : NotificationChannels {
    override fun getChannelForIncomingCall(ring: Boolean): String {
        return channelForIncomingCall(ring)
    }

    override fun getChannelIdForMessage(noisy: Boolean): String {
        return channelIdForMessage(noisy)
    }

    override fun getChannelIdForTest(): String {
        return channelIdForTest()
    }
}
