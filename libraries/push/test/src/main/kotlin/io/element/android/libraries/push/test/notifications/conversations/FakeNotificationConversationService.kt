/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications.conversations

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.notifications.conversations.NotificationConversationService
import io.element.android.tests.testutils.lambda.LambdaFiveParamsRecorder
import io.element.android.tests.testutils.lambda.lambdaRecorder

class FakeNotificationConversationService(
    private val onSendMessageLambda: LambdaFiveParamsRecorder<SessionId, RoomId, String?, Boolean, String?, Unit> = lambdaRecorder { _, _, _, _, _ -> },
) : NotificationConversationService {
    override suspend fun onSendMessage(
        sessionId: SessionId,
        roomId: RoomId,
        roomName: String?,
        roomIsDirect: Boolean,
        roomAvatarUrl: String?,
    ) = onSendMessageLambda(sessionId, roomId, roomName, roomIsDirect, roomAvatarUrl)

    override suspend fun onLeftRoom(sessionId: SessionId, roomId: RoomId) = Unit

    override suspend fun onAvailableRoomsChanged(sessionId: SessionId, roomIds: Set<RoomId>) = Unit
}
