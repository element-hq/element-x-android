/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.test

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.LoginType
import io.element.android.libraries.sessionstorage.api.SessionData

fun aSessionData(
    sessionId: SessionId = SessionId("@alice:server.org"),
    isTokenValid: Boolean = false,
    sessionPath: String = "/a/path/to/a/session",
    cachePath: String = "/a/path/to/a/cache",
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
        sessionPath = sessionPath,
        cachePath = cachePath,
    )
}
