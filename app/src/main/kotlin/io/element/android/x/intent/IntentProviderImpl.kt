/*
 * Copyright (c) 2023 New Vector Ltd
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
class IntentProviderImpl @Inject constructor(
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
