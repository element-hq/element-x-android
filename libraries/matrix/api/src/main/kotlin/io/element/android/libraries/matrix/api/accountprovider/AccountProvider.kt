/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.accountprovider

import androidx.compose.runtime.Immutable

@Immutable
sealed interface AccountProvider {
    /**
     * An account provider that was input by the user.
     */
    data class Generic(
        val serverName: String,
    ) : AccountProvider

    /**
     * An account provider that is provided by the app.
     */
    data class Managed(
        val serverName: String,
        val baseUrl: String,
        val canUseServerName: Boolean,
    ) : AccountProvider

    fun serverNameOrBaseUrl(): String {
        return when (this) {
            is Generic -> serverName
            is Managed -> if (canUseServerName) serverName else baseUrl
        }.sanitized()
    }

    /**
     * Return the server name, to be used in the UI.
     */
    fun friendlyServerName(): String {
        return when (this) {
            is Generic -> serverName
            is Managed -> serverName
        }.sanitized()
    }

    /**
     * The user may reach an allowed account provider by either of its identifiers: the server name it is known by
     * (what the user types, e.g. `element.io`) or the base url its client API lives at (e.g. `https://element.ems.host`).
     */
    fun matches(other: AccountProvider): Boolean {
        return serverNameOrBaseUrl() == other.serverNameOrBaseUrl()
    }

    /**
     * Sanitizes the string with the following rules:
     * - Trim any whitespace.
     * - Lowercase the string.
     * - Removes a https scheme (treating it as implicit).
     * - Remove any trailing slashes.
     */
    private fun String.sanitized(): String {
        return trim()
            .lowercase()
            // Intentionally continue to show http:// as an indicator (and for history).
            .removePrefix("https://")
            .removeSuffix("/")
    }
}

val matrixOrgAccountProvider = AccountProvider.Generic(
    serverName = "matrix.org",
)
