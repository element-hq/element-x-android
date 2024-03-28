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

import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.impl.notifications.GroupedNotificationEvents
import io.element.android.libraries.push.impl.notifications.NotificationFactory
import io.element.android.libraries.push.impl.notifications.OneShotNotification
import io.element.android.libraries.push.impl.notifications.RoomNotification
import io.element.android.libraries.push.impl.notifications.SummaryNotification
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

class MockkNotificationFactory {
    val instance = mockk<NotificationFactory>()

    fun givenNotificationsFor(
        groupedEvents: GroupedNotificationEvents,
        matrixUser: MatrixUser,
        useCompleteNotificationFormat: Boolean,
        roomNotifications: List<RoomNotification>,
        invitationNotifications: List<OneShotNotification>,
        simpleNotifications: List<OneShotNotification>,
        fallbackNotifications: List<OneShotNotification>,
        summaryNotification: SummaryNotification
    ) {
        with(instance) {
            coEvery { groupedEvents.roomEvents.toNotifications(matrixUser, any()) } returns roomNotifications
            every { groupedEvents.invitationEvents.toNotifications() } returns invitationNotifications
            every { groupedEvents.simpleEvents.toNotifications() } returns simpleNotifications
            every { groupedEvents.fallbackEvents.toNotifications() } returns fallbackNotifications

            every {
                createSummaryNotification(
                    matrixUser,
                    roomNotifications,
                    invitationNotifications,
                    simpleNotifications,
                    fallbackNotifications,
                    useCompleteNotificationFormat
                )
            } returns summaryNotification
        }
    }
}
