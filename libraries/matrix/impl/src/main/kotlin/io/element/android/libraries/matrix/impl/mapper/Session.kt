/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.mapper

import io.element.android.libraries.matrix.api.auth.external.ExternalSession
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
    slidingSyncProxy = null,
    loginTimestamp = Date(),
    isTokenValid = isTokenValid,
    loginType = loginType,
    passphrase = passphrase,
    sessionPath = sessionPaths.fileDirectory.absolutePath,
    cachePath = sessionPaths.cacheDirectory.absolutePath,
)

internal fun ExternalSession.toSessionData(
    isTokenValid: Boolean,
    loginType: LoginType,
    passphrase: String?,
    sessionPaths: SessionPaths,
) = SessionData(
    userId = userId,
    deviceId = deviceId,
    accessToken = accessToken,
    refreshToken = refreshToken,
    homeserverUrl = homeserverUrl,
    oidcData = null,
    slidingSyncProxy = slidingSyncProxy,
    loginTimestamp = Date(),
    isTokenValid = isTokenValid,
    loginType = loginType,
    passphrase = passphrase,
    sessionPath = sessionPaths.fileDirectory.absolutePath,
    cachePath = sessionPaths.cacheDirectory.absolutePath,
)
