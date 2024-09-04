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

package io.element.android.libraries.sessionstorage.impl

import io.element.android.libraries.sessionstorage.api.LoginType
import io.element.android.libraries.sessionstorage.api.SessionData
import java.util.Date
import io.element.android.libraries.matrix.session.SessionData as DbSessionData

internal fun SessionData.toDbModel(): DbSessionData {
    return DbSessionData(
        userId = userId,
        deviceId = deviceId,
        accessToken = accessToken,
        refreshToken = refreshToken,
        homeserverUrl = homeserverUrl,
        oidcData = oidcData,
        slidingSyncProxy = slidingSyncProxy,
        loginTimestamp = loginTimestamp?.time,
        isTokenValid = if (isTokenValid) 1L else 0L,
        loginType = loginType.name,
        passphrase = passphrase,
        sessionPath = sessionPath,
        cachePath = cachePath,
    )
}

internal fun DbSessionData.toApiModel(): SessionData {
    return SessionData(
        userId = userId,
        deviceId = deviceId,
        accessToken = accessToken,
        refreshToken = refreshToken,
        homeserverUrl = homeserverUrl,
        oidcData = oidcData,
        slidingSyncProxy = slidingSyncProxy,
        loginTimestamp = loginTimestamp?.let { Date(it) },
        isTokenValid = isTokenValid == 1L,
        loginType = LoginType.fromName(loginType ?: LoginType.UNKNOWN.name),
        passphrase = passphrase,
        sessionPath = sessionPath,
        cachePath = cachePath,
    )
}
