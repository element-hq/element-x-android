/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.sessionstorage.api.LoginType
import io.element.android.libraries.sessionstorage.api.SessionData

open class SignedOutStateProvider : PreviewParameterProvider<SignedOutState> {
    override val values: Sequence<SignedOutState>
        get() = sequenceOf(
            aSignedOutState(),
            // Add other states here
        )
}

private fun aSignedOutState() = SignedOutState(
    appName = "AppName",
    signedOutSession = aSessionData(),
    eventSink = {},
)

private fun aSessionData(
    sessionId: String = "@alice:server.org",
    isTokenValid: Boolean = false,
): SessionData {
    return SessionData(
        userId = sessionId,
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
        cachePath = "/a/path/to/a/cache",
    )
}
