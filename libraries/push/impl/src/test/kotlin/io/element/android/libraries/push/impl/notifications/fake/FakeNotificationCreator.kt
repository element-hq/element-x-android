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

package io.element.android.libraries.push.impl.notifications.fake

import android.app.Notification
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.impl.notifications.RoomEventGroupInfo
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.libraries.push.impl.notifications.fixtures.A_NOTIFICATION
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.tests.testutils.lambda.LambdaFiveParamsRecorder
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
    var createSummaryListNotificationResult: LambdaFiveParamsRecorder<MatrixUser, NotificationCompat.InboxStyle?, String, Boolean, Long, Notification> =
        lambdaRecorder { _, _, _, _, _ -> A_NOTIFICATION },
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
        style: NotificationCompat.InboxStyle?,
        compatSummary: String,
        noisy: Boolean,
        lastMessageTimestamp: Long
    ): Notification {
        return createSummaryListNotificationResult(currentUser, style, compatSummary, noisy, lastMessageTimestamp)
    }

    override fun createDiagnosticNotification(): Notification {
        return createDiagnosticNotificationResult()
    }
}
