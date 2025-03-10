/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.fake

import android.app.Notification
import android.graphics.Bitmap
import coil3.ImageLoader
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.impl.notifications.RoomEventGroupInfo
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.libraries.push.impl.notifications.fixtures.A_NOTIFICATION
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.tests.testutils.lambda.LambdaFourParamsRecorder
import io.element.android.tests.testutils.lambda.LambdaListAnyParamsRecorder
import io.element.android.tests.testutils.lambda.LambdaNoParamRecorder
import io.element.android.tests.testutils.lambda.LambdaOneParamRecorder
import io.element.android.tests.testutils.lambda.lambdaAnyRecorder
import io.element.android.tests.testutils.lambda.lambdaRecorder

class FakeNotificationCreator(
    var createMessagesListNotificationResult: LambdaListAnyParamsRecorder<Notification> = lambdaAnyRecorder { A_NOTIFICATION },
    var createRoomInvitationNotificationResult: LambdaOneParamRecorder<InviteNotifiableEvent, Notification> = lambdaRecorder { _ -> A_NOTIFICATION },
    var createSimpleNotificationResult: LambdaOneParamRecorder<SimpleNotifiableEvent, Notification> = lambdaRecorder { _ -> A_NOTIFICATION },
    var createFallbackNotificationResult: LambdaOneParamRecorder<FallbackNotifiableEvent, Notification> = lambdaRecorder { _ -> A_NOTIFICATION },
    var createSummaryListNotificationResult: LambdaFourParamsRecorder<MatrixUser, String, Boolean, Long, Notification> =
        lambdaRecorder { _, _, _, _ -> A_NOTIFICATION },
    var createDiagnosticNotificationResult: LambdaNoParamRecorder<Notification> = lambdaRecorder { -> A_NOTIFICATION },
) : NotificationCreator {
    override suspend fun createMessagesListNotification(
        roomInfo: RoomEventGroupInfo,
        threadId: ThreadId?,
        largeIcon: Bitmap?,
        lastMessageTimestamp: Long,
        tickerText: String,
        currentUser: MatrixUser,
        existingNotification: Notification?,
        imageLoader: ImageLoader,
        events: List<NotifiableMessageEvent>
    ): Notification {
        return createMessagesListNotificationResult(
            listOf(roomInfo, threadId, largeIcon, lastMessageTimestamp, tickerText, currentUser, existingNotification, imageLoader, events)
        )
    }

    override fun createRoomInvitationNotification(inviteNotifiableEvent: InviteNotifiableEvent): Notification {
        return createRoomInvitationNotificationResult(inviteNotifiableEvent)
    }

    override fun createSimpleEventNotification(simpleNotifiableEvent: SimpleNotifiableEvent): Notification {
        return createSimpleNotificationResult(simpleNotifiableEvent)
    }

    override fun createFallbackNotification(fallbackNotifiableEvent: FallbackNotifiableEvent): Notification {
        return createFallbackNotificationResult(fallbackNotifiableEvent)
    }

    override fun createSummaryListNotification(
        currentUser: MatrixUser,
        compatSummary: String,
        noisy: Boolean,
        lastMessageTimestamp: Long
    ): Notification {
        return createSummaryListNotificationResult(currentUser, compatSummary, noisy, lastMessageTimestamp)
    }

    override fun createDiagnosticNotification(): Notification {
        return createDiagnosticNotificationResult()
    }
}
