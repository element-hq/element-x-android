/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.test

import io.element.android.libraries.sessionstorage.api.LoginType
import io.element.android.libraries.sessionstorage.api.SessionData
import java.util.Date

fun aSessionData(
    sessionId: String = "@alice:server.org",
    deviceId: String = "aDeviceId",
    isTokenValid: Boolean = false,
    sessionPath: String = "/a/path/to/a/session",
    cachePath: String = "/a/path/to/a/cache",
    accessToken: String = "anAccessToken",
    refreshToken: String? = "aRefreshToken",
    lastUsageIndex: Long = 0,
    lastUsageDate: Date = Date(0),
    userDisplayName: String? = null,
    userAvatarUrl: String? = null,
): SessionData {
    return SessionData(
        userId = sessionId,
        deviceId = deviceId,
        accessToken = accessToken,
        refreshToken = refreshToken,
        homeserverUrl = "aHomeserverUrl",
        oidcData = null,
        slidingSyncProxy = null,
        loginTimestamp = null,
        isTokenValid = isTokenValid,
        loginType = LoginType.UNKNOWN,
        passphrase = null,
        sessionPath = sessionPath,
        cachePath = cachePath,
        lastUsageIndex = lastUsageIndex,
        lastUsageDate = lastUsageDate,
        userDisplayName = userDisplayName,
        userAvatarUrl = userAvatarUrl,
    )
}
