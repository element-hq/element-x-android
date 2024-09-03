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

package io.element.android.libraries.matrix.impl.mapper

import io.element.android.libraries.matrix.impl.paths.SessionPaths
import io.element.android.libraries.sessionstorage.api.LoginType
import io.element.android.libraries.sessionstorage.api.SessionData
import org.matrix.rustcomponents.sdk.Session
import java.util.Date

internal fun Session.toSessionData(
    isTokenValid: Boolean,
    loginType: LoginType,
    passphrase: String?,
    sessionPaths: SessionPaths,
    homeserverUrl: String? = null,
) = SessionData(
    userId = userId,
    deviceId = deviceId,
    accessToken = accessToken,
    refreshToken = refreshToken,
    homeserverUrl = homeserverUrl ?: this.homeserverUrl,
    oidcData = oidcData,
    slidingSyncProxy = slidingSyncProxy,
    loginTimestamp = Date(),
    isTokenValid = isTokenValid,
    loginType = loginType,
    passphrase = passphrase,
    sessionPath = sessionPaths.fileDirectory.absolutePath,
    cachePath = sessionPaths.cacheDirectory.absolutePath,
)
