/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.pushers

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.pusher.PushersService
import io.element.android.libraries.matrix.api.pusher.SetHttpPusherData
import io.element.android.libraries.matrix.api.pusher.UnsetHttpPusherData
import io.element.android.libraries.matrix.impl.exception.mapClientException
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
            runCatchingExceptions {
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
                .mapFailure { it.mapClientException() }
        }
    }

    override suspend fun unsetHttpPusher(unsetHttpPusherData: UnsetHttpPusherData): Result<Unit> {
        return withContext(dispatchers.io) {
            runCatchingExceptions {
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
