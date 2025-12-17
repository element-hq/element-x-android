/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.test

import io.element.android.libraries.sessionstorage.api.LoginType
import io.element.android.libraries.sessionstorage.api.SessionData

fun aSessionData(
    sessionId: String = "@alice:server.org",
    deviceId: String = "aDeviceId",
    isTokenValid: Boolean = false,
    sessionPath: String = "/a/path/to/a/session",
    cachePath: String = "/a/path/to/a/cache",
    accessToken: String = "anAccessToken",
    refreshToken: String? = "aRefreshToken",
    position: Long = 0,
    lastUsageIndex: Long = 0,
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
        loginTimestamp = null,
        isTokenValid = isTokenValid,
        loginType = LoginType.UNKNOWN,
        passphrase = null,
        sessionPath = sessionPath,
        cachePath = cachePath,
        position = position,
        lastUsageIndex = lastUsageIndex,
        userDisplayName = userDisplayName,
        userAvatarUrl = userAvatarUrl,
    )
}
