/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.impl

import io.element.android.libraries.matrix.session.SessionData
import io.element.android.libraries.sessionstorage.api.LoginType

internal fun aSessionData() = SessionData(
    userId = "userId",
    deviceId = "deviceId",
    accessToken = "accessToken",
    refreshToken = "refreshToken",
    homeserverUrl = "homeserverUrl",
    slidingSyncProxy = null,
    loginTimestamp = null,
    oidcData = "aOidcData",
    isTokenValid = 1,
    loginType = LoginType.UNKNOWN.name,
    passphrase = null,
    sessionPath = "sessionPath",
    cachePath = "cachePath",
)
