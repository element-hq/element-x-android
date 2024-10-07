/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
