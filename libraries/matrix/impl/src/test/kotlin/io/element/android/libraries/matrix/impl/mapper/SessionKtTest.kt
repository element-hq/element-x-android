/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.mapper

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.auth.external.ExternalSession
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustSession
import io.element.android.libraries.matrix.impl.paths.SessionPaths
import io.element.android.libraries.matrix.test.A_DEVICE_ID
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL_2
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.sessionstorage.api.LoginType
import org.junit.Test
import java.io.File

class SessionKtTest {
    @Test
    fun `toSessionData compute the expected result`() {
        val result = aRustSession().toSessionData(
            isTokenValid = true,
            loginType = LoginType.PASSWORD,
            passphrase = A_SECRET,
            sessionPaths = SessionPaths(File("/a/file"), File("/a/cache")),
        )
        assertThat(result.userId).isEqualTo(A_USER_ID.value)
        assertThat(result.deviceId).isEqualTo(A_DEVICE_ID.value)
        assertThat(result.accessToken).isEqualTo("accessToken")
        assertThat(result.refreshToken).isEqualTo("refreshToken")
        assertThat(result.homeserverUrl).isEqualTo(A_HOMESERVER_URL)
        assertThat(result.isTokenValid).isTrue()
        assertThat(result.oidcData).isNull()
        assertThat(result.loginType).isEqualTo(LoginType.PASSWORD)
        assertThat(result.loginTimestamp).isNotNull()
        assertThat(result.passphrase).isEqualTo(A_SECRET)
        assertThat(result.sessionPath).isEqualTo("/a/file")
        assertThat(result.cachePath).isEqualTo("/a/cache")
    }

    @Test
    fun `toSessionData can change the validity of the token`() {
        val result = aRustSession().toSessionData(
            isTokenValid = false,
            loginType = LoginType.PASSWORD,
            passphrase = A_SECRET,
            sessionPaths = SessionPaths(File("/a/file"), File("/a/cache")),
            homeserverUrl = null,
        )
        assertThat(result.isTokenValid).isFalse()
    }

    @Test
    fun `toSessionData can override the value of the homeserver url`() {
        val result = aRustSession().toSessionData(
            isTokenValid = true,
            loginType = LoginType.PASSWORD,
            passphrase = A_SECRET,
            sessionPaths = SessionPaths(File("/a/file"), File("/a/cache")),
            homeserverUrl = A_HOMESERVER_URL_2,
        )
        assertThat(result.homeserverUrl).isEqualTo(A_HOMESERVER_URL_2)
    }

    @Test
    fun `ExternalSession toSessionData compute the expected result`() {
        val result = anExternalSession().toSessionData(
            isTokenValid = true,
            loginType = LoginType.PASSWORD,
            passphrase = A_SECRET,
            sessionPaths = SessionPaths(File("/a/file"), File("/a/cache")),
        )
        assertThat(result.userId).isEqualTo(A_USER_ID.value)
        assertThat(result.deviceId).isEqualTo(A_DEVICE_ID.value)
        assertThat(result.accessToken).isEqualTo("accessToken")
        assertThat(result.refreshToken).isNull()
        assertThat(result.homeserverUrl).isEqualTo(A_HOMESERVER_URL)
        assertThat(result.isTokenValid).isTrue()
        assertThat(result.oidcData).isNull()
        assertThat(result.loginType).isEqualTo(LoginType.PASSWORD)
        assertThat(result.loginTimestamp).isNotNull()
        assertThat(result.passphrase).isEqualTo(A_SECRET)
        assertThat(result.sessionPath).isEqualTo("/a/file")
        assertThat(result.cachePath).isEqualTo("/a/cache")
    }

    @Test
    fun `ExternalSession toSessionData can change the validity of the token`() {
        val result = anExternalSession().toSessionData(
            isTokenValid = false,
            loginType = LoginType.PASSWORD,
            passphrase = A_SECRET,
            sessionPaths = SessionPaths(File("/a/file"), File("/a/cache")),
        )
        assertThat(result.isTokenValid).isFalse()
    }
}

private fun anExternalSession(
    userId: String = A_USER_ID.value,
    deviceId: String = A_DEVICE_ID.value,
    accessToken: String = "accessToken",
    refreshToken: String? = null,
    homeserverUrl: String = A_HOMESERVER_URL,
) = ExternalSession(
    userId = userId,
    deviceId = deviceId,
    accessToken = accessToken,
    refreshToken = refreshToken,
    homeserverUrl = homeserverUrl,
)
