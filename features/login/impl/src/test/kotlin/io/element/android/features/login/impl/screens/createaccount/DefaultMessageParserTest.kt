/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.createaccount

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.matrix.api.auth.external.ExternalSession
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertThrows
import org.junit.Test

class DefaultMessageParserTest {
    private val validMessage = """
        {
            "user_id": "user_id",
            "home_server": "home_server",
            "access_token": "access_token",
            "device_id": "device_id"
        }
   """.trimIndent()

    @Test
    fun `DefaultMessageParser is able to parse correct message`() {
        val sut = createDefaultMessageParser()
        assertThat(sut.parse(validMessage)).isEqualTo(
            anExternalSession(
                homeserverUrl = "home_server",
            )
        )
    }

    @Test
    fun `DefaultMessageParser should throw Exception in case of error`() {
        val sut = createDefaultMessageParser()
        // kotlinx.serialization.json.internal.JsonDecodingException
        assertThrows(SerializationException::class.java) { sut.parse("invalid json") }
        // missing userId
        assertThrows(IllegalStateException::class.java) { sut.parse(validMessage.replace(""""user_id": "user_id",""", "")) }
        // missing accessToken
        assertThrows(IllegalStateException::class.java) { sut.parse(validMessage.replace(""""access_token": "access_token",""", "")) }
        // missing deviceId
        assertThrows(IllegalStateException::class.java) {
            sut.parse(
                validMessage
                    .replace(""""access_token": "access_token",""", """"access_token": "access_token"""")
                    .replace(""""device_id": "device_id"""", "")
            )
        }
    }

    @Test
    fun `DefaultMessageParser should be successful even is homeserver url is missing`() {
        val sut = createDefaultMessageParser()
        // missing homeServer
        assertThat(sut.parse(validMessage.replace(""""home_server": "home_server",""", ""))).isEqualTo(
            anExternalSession(
                homeserverUrl = AuthenticationConfig.MATRIX_ORG_URL,
            )
        )
    }

    private fun createDefaultMessageParser(): DefaultMessageParser {
        return DefaultMessageParser(
            accountProviderDataSource = AccountProviderDataSource(FakeEnterpriseService()),
            json = DefaultJsonProvider(),
        )
    }
}

internal fun anExternalSession(
    homeserverUrl: String = "home_server",
) = ExternalSession(
    userId = "user_id",
    homeserverUrl = homeserverUrl,
    accessToken = "access_token",
    deviceId = "device_id",
    refreshToken = null,
)
