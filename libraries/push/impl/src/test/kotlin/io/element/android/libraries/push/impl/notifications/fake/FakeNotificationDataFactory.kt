/*
 * Copyright 2021-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.fake

import coil3.ImageLoader
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.impl.notifications.NotificationDataFactory
import io.element.android.libraries.push.impl.notifications.OneShotNotification
import io.element.android.libraries.push.impl.notifications.RoomNotification
import io.element.android.libraries.push.impl.notifications.SummaryNotification
import io.element.android.libraries.push.impl.notifications.fixtures.A_NOTIFICATION
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.tests.testutils.lambda.LambdaFiveParamsRecorder
import io.element.android.tests.testutils.lambda.LambdaOneParamRecorder
import io.element.android.tests.testutils.lambda.LambdaThreeParamsRecorder
import io.element.android.tests.testutils.lambda.lambdaRecorder

class FakeNotificationDataFactory(
    var messageEventToNotificationsResult: LambdaThreeParamsRecorder<List<NotifiableMessageEvent>, MatrixUser, ImageLoader, List<RoomNotification>> =
        lambdaRecorder { _, _, _ -> emptyList() },
    var summaryToNotificationsResult: LambdaFiveParamsRecorder<
        MatrixUser,
        List<RoomNotification>,
        List<OneShotNotification>,
        List<OneShotNotification>,
        List<OneShotNotification>,
        SummaryNotification
    > = lambdaRecorder { _, _, _, _, _ -> SummaryNotification.Update(A_NOTIFICATION) },
    var inviteToNotificationsResult: LambdaOneParamRecorder<List<InviteNotifiableEvent>, List<OneShotNotification>> = lambdaRecorder { _ -> emptyList() },
    var simpleEventToNotificationsResult: LambdaOneParamRecorder<List<SimpleNotifiableEvent>, List<OneShotNotification>> = lambdaRecorder { _ -> emptyList() },
    var fallbackEventToNotificationsResult: LambdaOneParamRecorder<List<FallbackNotifiableEvent>, List<OneShotNotification>> =
        lambdaRecorder { _ -> emptyList() },
) : NotificationDataFactory {
    override suspend fun toNotifications(messages: List<NotifiableMessageEvent>, currentUser: MatrixUser, imageLoader: ImageLoader): List<RoomNotification> {
        return messageEventToNotificationsResult(messages, currentUser, imageLoader)
    }

    @JvmName("toNotificationInvites")
    @Suppress("INAPPLICABLE_JVM_NAME")
    override fun toNotifications(invites: List<InviteNotifiableEvent>): List<OneShotNotification> {
        return inviteToNotificationsResult(invites)
    }

    @JvmName("toNotificationSimpleEvents")
    @Suppress("INAPPLICABLE_JVM_NAME")
    override fun toNotifications(simpleEvents: List<SimpleNotifiableEvent>): List<OneShotNotification> {
        return simpleEventToNotificationsResult(simpleEvents)
    }

    @JvmName("toNotificationFallbackEvents")
    @Suppress("INAPPLICABLE_JVM_NAME")
    override fun toNotifications(fallback: List<FallbackNotifiableEvent>): List<OneShotNotification> {
        return fallbackEventToNotificationsResult(fallback)
    }

    override fun createSummaryNotification(
        currentUser: MatrixUser,
        roomNotifications: List<RoomNotification>,
        invitationNotifications: List<OneShotNotification>,
        simpleNotifications: List<OneShotNotification>,
        fallbackNotifications: List<OneShotNotification>,
    ): SummaryNotification {
        return summaryToNotificationsResult(
            currentUser,
            roomNotifications,
            invitationNotifications,
            simpleNotifications,
            fallbackNotifications,
        )
    }
}
