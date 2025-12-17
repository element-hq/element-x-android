/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.fake

import android.app.Notification
import android.graphics.Bitmap
import androidx.annotation.ColorInt
import coil3.ImageLoader
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.push.impl.notifications.RoomEventGroupInfo
import io.element.android.libraries.push.impl.notifications.factories.NotificationAccountParams
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.libraries.push.impl.notifications.fixtures.A_NOTIFICATION
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.tests.testutils.lambda.LambdaFiveParamsRecorder
import io.element.android.tests.testutils.lambda.LambdaListAnyParamsRecorder
import io.element.android.tests.testutils.lambda.LambdaOneParamRecorder
import io.element.android.tests.testutils.lambda.LambdaTwoParamsRecorder
import io.element.android.tests.testutils.lambda.lambdaAnyRecorder
import io.element.android.tests.testutils.lambda.lambdaRecorder

class FakeNotificationCreator(
    var createMessagesListNotificationResult: LambdaListAnyParamsRecorder<Notification> = lambdaAnyRecorder { A_NOTIFICATION },
    var createRoomInvitationNotificationResult: LambdaTwoParamsRecorder<NotificationAccountParams, InviteNotifiableEvent, Notification> =
        lambdaRecorder { _, _ -> A_NOTIFICATION },
    var createSimpleNotificationResult: LambdaTwoParamsRecorder<NotificationAccountParams, SimpleNotifiableEvent, Notification> =
        lambdaRecorder { _, _ -> A_NOTIFICATION },
    var createFallbackNotificationResult: LambdaTwoParamsRecorder<NotificationAccountParams, FallbackNotifiableEvent, Notification> =
        lambdaRecorder { _, _ -> A_NOTIFICATION },
    var createSummaryListNotificationResult: LambdaFiveParamsRecorder<
        NotificationAccountParams, String, Boolean, Long, NotificationAccountParams, Notification
        > = lambdaRecorder { _, _, _, _, _ -> A_NOTIFICATION },
    var createDiagnosticNotificationResult: LambdaOneParamRecorder<Int, Notification> =
        lambdaRecorder<Int, Notification> { _ -> A_NOTIFICATION },
    val createUnregistrationNotificationResult: LambdaOneParamRecorder<NotificationAccountParams, Notification> =
        lambdaRecorder { _ -> A_NOTIFICATION },
) : NotificationCreator {
    override suspend fun createMessagesListNotification(
        notificationAccountParams: NotificationAccountParams,
        roomInfo: RoomEventGroupInfo,
        threadId: ThreadId?,
        largeIcon: Bitmap?,
        lastMessageTimestamp: Long,
        tickerText: String,
        existingNotification: Notification?,
        imageLoader: ImageLoader,
        events: List<NotifiableMessageEvent>,
    ): Notification {
        return createMessagesListNotificationResult(
            listOf(notificationAccountParams, roomInfo, threadId, largeIcon, lastMessageTimestamp, tickerText, existingNotification, imageLoader, events)
        )
    }

    override fun createRoomInvitationNotification(
        notificationAccountParams: NotificationAccountParams,
        inviteNotifiableEvent: InviteNotifiableEvent,
    ): Notification {
        return createRoomInvitationNotificationResult(notificationAccountParams, inviteNotifiableEvent)
    }

    override fun createSimpleEventNotification(
        notificationAccountParams: NotificationAccountParams,
        simpleNotifiableEvent: SimpleNotifiableEvent,
    ): Notification {
        return createSimpleNotificationResult(notificationAccountParams, simpleNotifiableEvent)
    }

    override fun createFallbackNotification(
        notificationAccountParams: NotificationAccountParams,
        fallbackNotifiableEvent: FallbackNotifiableEvent,
    ): Notification {
        return createFallbackNotificationResult(notificationAccountParams, fallbackNotifiableEvent)
    }

    override fun createSummaryListNotification(
        notificationAccountParams: NotificationAccountParams,
        compatSummary: String,
        noisy: Boolean,
        lastMessageTimestamp: Long,
    ): Notification {
        return createSummaryListNotificationResult(notificationAccountParams, compatSummary, noisy, lastMessageTimestamp, notificationAccountParams)
    }

    override fun createDiagnosticNotification(
        @ColorInt color: Int,
    ): Notification {
        return createDiagnosticNotificationResult(color)
    }

    override fun createUnregistrationNotification(notificationAccountParams: NotificationAccountParams): Notification {
        return createUnregistrationNotificationResult(notificationAccountParams)
    }
}
