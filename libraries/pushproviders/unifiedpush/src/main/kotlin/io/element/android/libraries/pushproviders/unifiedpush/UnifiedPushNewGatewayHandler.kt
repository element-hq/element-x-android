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

package io.element.android.libraries.pushproviders.unifiedpush

import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("UnifiedPushNewGatewayHandler", LoggerTag.PushLoggerTag)

/**
 * Handle new endpoint received from UnifiedPush. Will update all the sessions which are using UnifiedPush as a push provider.
 */
class UnifiedPushNewGatewayHandler @Inject constructor(
    private val pusherSubscriber: PusherSubscriber,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val pushClientSecret: PushClientSecret,
    private val matrixAuthenticationService: MatrixAuthenticationService,
) {
    suspend fun handle(endpoint: String, pushGateway: String, clientSecret: String) {
        // Register the pusher for the session with this client secret, if is it using UnifiedPush.
        val userId = pushClientSecret.getUserIdFromSecret(clientSecret) ?: return Unit.also {
            Timber.w("Unable to retrieve session")
        }
        val userDataStore = userPushStoreFactory.getOrCreate(userId)
        if (userDataStore.getPushProviderName() == UnifiedPushConfig.NAME) {
            matrixAuthenticationService.restoreSession(userId).getOrNull()?.use { client ->
                pusherSubscriber.registerPusher(client, endpoint, pushGateway)
            }
        } else {
            Timber.tag(loggerTag.value).d("This session is not using UnifiedPush pusher")
        }
    }
}
