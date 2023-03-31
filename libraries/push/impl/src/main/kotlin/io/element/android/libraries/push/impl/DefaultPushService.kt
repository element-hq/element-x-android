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

package io.element.android.libraries.push.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.impl.config.PushConfig
import io.element.android.libraries.push.impl.log.pushLoggerTag
import io.element.android.libraries.push.impl.notifications.NotificationDrawerManager
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultPushService @Inject constructor(
    private val notificationDrawerManager: NotificationDrawerManager,
    private val pushersManager: PushersManager,
    private val fcmHelper: FcmHelper,
) : PushService {
    override fun setCurrentRoom(roomId: String?) {
        notificationDrawerManager.setCurrentRoom(roomId)
    }

    override fun setCurrentThread(threadId: String?) {
        notificationDrawerManager.setCurrentThread(threadId)
    }

    override fun notificationStyleChanged() {
        notificationDrawerManager.notificationStyleChanged()
    }

    override suspend fun registerFirebasePusher(matrixClient: MatrixClient) {
        val pushKey = fcmHelper.getFcmToken() ?: return Unit.also {
            Timber.tag(pushLoggerTag.value).w("Unable to register pusher, Firebase token is not known.")
        }
        pushersManager.registerPusher(matrixClient, pushKey, PushConfig.pusher_http_url)
    }

    override suspend fun testPush() {
        pushersManager.testPush()
    }
}
