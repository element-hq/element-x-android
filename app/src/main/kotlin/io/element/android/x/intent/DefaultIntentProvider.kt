/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.intent

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.deeplink.DeepLinkCreator
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.push.impl.intent.IntentProvider
import io.element.android.x.MainActivity
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultIntentProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deepLinkCreator: DeepLinkCreator,
) : IntentProvider {
    override fun getViewRoomIntent(
        sessionId: SessionId,
        roomId: RoomId?,
        threadId: ThreadId?,
    ): Intent {
        return Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = deepLinkCreator.room(sessionId, roomId, threadId).toUri()
        }
    }
}
