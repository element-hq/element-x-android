/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications

import android.content.Intent
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.notifications.RoomNotificationSettingsIntentProvider
import io.element.android.tests.testutils.lambda.lambdaError

class FakeRoomNotificationSettingsIntentProvider(
    private val getIntentLambda: (SessionId, RoomId, String, Boolean, String?) -> Intent = { _, _, _, _, _ -> lambdaError() },
) : RoomNotificationSettingsIntentProvider {
    override suspend fun getIntent(sessionId: SessionId, roomId: RoomId, roomDisplayName: String, isDm: Boolean, roomAvatarUrl: String?): Intent {
        return getIntentLambda(sessionId, roomId, roomDisplayName, isDm, roomAvatarUrl)
    }
}
