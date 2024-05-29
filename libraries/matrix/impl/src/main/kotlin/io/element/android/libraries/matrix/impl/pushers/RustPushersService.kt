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

package io.element.android.libraries.matrix.impl.pushers

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.pusher.PushersService
import io.element.android.libraries.matrix.api.pusher.SetHttpPusherData
import io.element.android.libraries.matrix.api.pusher.UnsetHttpPusherData
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.HttpPusherData
import org.matrix.rustcomponents.sdk.PushFormat
import org.matrix.rustcomponents.sdk.PusherIdentifiers
import org.matrix.rustcomponents.sdk.PusherKind

class RustPushersService(
    private val client: Client,
    private val dispatchers: CoroutineDispatchers
) : PushersService {
    override suspend fun setHttpPusher(setHttpPusherData: SetHttpPusherData): Result<Unit> {
        return withContext(dispatchers.io) {
            runCatching {
                client.setPusher(
                    identifiers = PusherIdentifiers(
                        pushkey = setHttpPusherData.pushKey,
                        appId = setHttpPusherData.appId
                    ),
                    kind = PusherKind.Http(
                        data = HttpPusherData(
                            url = setHttpPusherData.url,
                            format = PushFormat.EVENT_ID_ONLY,
                            defaultPayload = setHttpPusherData.defaultPayload
                        )
                    ),
                    appDisplayName = setHttpPusherData.appDisplayName,
                    deviceDisplayName = setHttpPusherData.deviceDisplayName,
                    profileTag = setHttpPusherData.profileTag,
                    lang = setHttpPusherData.lang
                )
            }
        }
    }

    override suspend fun unsetHttpPusher(unsetHttpPusherData: UnsetHttpPusherData): Result<Unit> {
        return withContext(dispatchers.io) {
            runCatching {
                client.deletePusher(
                    identifiers = PusherIdentifiers(
                        pushkey = unsetHttpPusherData.pushKey,
                        appId = unsetHttpPusherData.appId
                    ),
                )
            }
        }
    }
}
