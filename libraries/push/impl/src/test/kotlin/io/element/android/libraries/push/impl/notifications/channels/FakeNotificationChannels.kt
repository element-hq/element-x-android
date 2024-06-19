/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
