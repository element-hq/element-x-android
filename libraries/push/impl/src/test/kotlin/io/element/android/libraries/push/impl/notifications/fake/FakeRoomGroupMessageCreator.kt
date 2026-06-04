/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2021-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.fake

import android.app.Notification
import coil3.ImageLoader
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.push.impl.notifications.RoomGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.factories.NotificationAccountParams
import io.element.android.libraries.push.impl.notifications.fixtures.A_NOTIFICATION
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.tests.testutils.lambda.LambdaSixParamsRecorder
import io.element.android.tests.testutils.lambda.lambdaRecorder

// We just can't make the param types fit
@Suppress("MaxLineLength", "ktlint:standard:max-line-length", "ktlint:standard:parameter-wrapping")
class FakeRoomGroupMessageCreator(
    var createRoomMessageResult: LambdaSixParamsRecorder<
        NotificationAccountParams, List<NotifiableMessageEvent>, RoomId, ThreadId?, ImageLoader, Notification?, Notification
        > = lambdaRecorder { _, _, _, _, _, _ -> A_NOTIFICATION }
) : RoomGroupMessageCreator {
    override suspend fun createRoomMessage(
        notificationAccountParams: NotificationAccountParams,
        events: List<NotifiableMessageEvent>,
        roomId: RoomId,
        threadId: ThreadId?,
        imageLoader: ImageLoader,
        existingNotification: Notification?,
    ): Notification {
        return createRoomMessageResult(notificationAccountParams, events, roomId, threadId, imageLoader, existingNotification)
    }
}
