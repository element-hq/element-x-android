/*
 * Copyright 2021-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
