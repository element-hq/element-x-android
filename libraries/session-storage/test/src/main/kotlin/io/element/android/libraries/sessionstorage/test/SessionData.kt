/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.sessionstorage.test

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.LoginType
import io.element.android.libraries.sessionstorage.api.SessionData

fun aSessionData(
    sessionId: SessionId = SessionId("@alice:server.org"),
    isTokenValid: Boolean = false,
): SessionData {
    return SessionData(
        userId = sessionId.value,
        deviceId = "aDeviceId",
        accessToken = "anAccessToken",
        refreshToken = "aRefreshToken",
        homeserverUrl = "aHomeserverUrl",
        oidcData = null,
        slidingSyncProxy = null,
        loginTimestamp = null,
        isTokenValid = isTokenValid,
        loginType = LoginType.UNKNOWN,
        passphrase = null,
        sessionPath = "/a/path/to/a/session",
    )
}
