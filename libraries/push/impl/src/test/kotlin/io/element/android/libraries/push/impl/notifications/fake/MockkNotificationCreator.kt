/*
 * Copyright (c) 2021 New Vector Ltd
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

package io.element.android.libraries.push.impl.notifications.fake

import android.app.Notification
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.mockk.every
import io.mockk.mockk

class MockkNotificationCreator {
    val instance = mockk<NotificationCreator>()

    fun givenCreateRoomInvitationNotificationFor(event: InviteNotifiableEvent): Notification {
        val mockNotification = mockk<Notification>()
        every { instance.createRoomInvitationNotification(event) } returns mockNotification
        return mockNotification
    }

    fun givenCreateSimpleInvitationNotificationFor(event: SimpleNotifiableEvent): Notification {
        val mockNotification = mockk<Notification>()
        every { instance.createSimpleEventNotification(event) } returns mockNotification
        return mockNotification
    }

    fun givenCreateDiagnosticNotification(): Notification {
        val mockNotification = mockk<Notification>()
        every { instance.createDiagnosticNotification() } returns mockNotification
        return mockNotification
    }
}
