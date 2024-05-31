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
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.impl.notifications.OneShotNotification
import io.element.android.libraries.push.impl.notifications.RoomNotification
import io.element.android.libraries.push.impl.notifications.SummaryGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fixtures.A_NOTIFICATION
import io.element.android.tests.testutils.lambda.LambdaFiveParamsRecorder
import io.element.android.tests.testutils.lambda.lambdaRecorder

class FakeSummaryGroupMessageCreator(
    var createSummaryNotificationResult: LambdaFiveParamsRecorder<
        MatrixUser, List<RoomNotification>, List<OneShotNotification>, List<OneShotNotification>, List<OneShotNotification>, Notification
    > =
        lambdaRecorder { _, _, _, _, _ -> A_NOTIFICATION }
) : SummaryGroupMessageCreator {
    override fun createSummaryNotification(
        currentUser: MatrixUser,
        roomNotifications: List<RoomNotification>,
        invitationNotifications: List<OneShotNotification>,
        simpleNotifications: List<OneShotNotification>,
        fallbackNotifications: List<OneShotNotification>,
    ): Notification {
        return createSummaryNotificationResult(
            currentUser,
            roomNotifications,
            invitationNotifications,
            simpleNotifications,
            fallbackNotifications,
        )
    }
}
