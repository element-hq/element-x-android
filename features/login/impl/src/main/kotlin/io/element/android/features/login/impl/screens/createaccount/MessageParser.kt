/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.createaccount

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.matrix.api.auth.external.ExternalSession

interface MessageParser {
    /**
     * Parse the message and return the ExternalSession object, or
     * throw an exception if the message is invalid.
     */
    fun parse(message: String): ExternalSession
}

@ContributesBinding(AppScope::class)
class DefaultMessageParser(
    private val accountProviderDataSource: AccountProviderDataSource,
    private val json: JsonProvider,
) : MessageParser {
    override fun parse(message: String): ExternalSession {
        val response = json().decodeFromString(MobileRegistrationResponse.serializer(), message)
        val userId = response.userId ?: error("No user ID in response")
        val homeServer = response.homeServer ?: accountProviderDataSource.flow.value.url
        val accessToken = response.accessToken ?: error("No access token in response")
        val deviceId = response.deviceId ?: error("No device ID in response")
        return ExternalSession(
            userId = userId,
            homeserverUrl = homeServer,
            accessToken = accessToken,
            deviceId = deviceId,
            refreshToken = null,
        )
    }
}
