/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.accountprovider

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AccountProviderTest {
    @Test
    fun `serverNameOrBaseUrl of a generic account provider is the server name the user input`() {
        assertThat(AccountProvider.Generic("matrix.org").serverNameOrBaseUrl()).isEqualTo("matrix.org")
    }

    @Test
    fun `serverNameOrBaseUrl of a managed account provider is its server name when it can be used`() {
        val accountProvider = AccountProvider.Managed(
            serverName = "element.io",
            baseUrl = "https://element.ems.host",
            canUseServerName = true,
        )
        assertThat(accountProvider.serverNameOrBaseUrl()).isEqualTo("element.io")
    }

    @Test
    fun `serverNameOrBaseUrl of a managed account provider is its base url when the server name cannot be used`() {
        val accountProvider = AccountProvider.Managed(
            serverName = "element.io",
            baseUrl = "https://element.ems.host",
            canUseServerName = false,
        )
        assertThat(accountProvider.serverNameOrBaseUrl()).isEqualTo("element.ems.host")
    }

    @Test
    fun `matrixOrgAccountProvider is known by its server name`() {
        assertThat(matrixOrgAccountProvider.serverNameOrBaseUrl()).isEqualTo("matrix.org")
    }

    @Test
    fun `serverNameOrBaseUrl trims, lowercases and drops the implicit https scheme and a trailing slash`() {
        assertThat(AccountProvider.Generic("  MATRIX.org ").serverNameOrBaseUrl()).isEqualTo("matrix.org")
        assertThat(AccountProvider.Generic("https://matrix.org").serverNameOrBaseUrl()).isEqualTo("matrix.org")
        assertThat(AccountProvider.Generic("https://matrix.org/").serverNameOrBaseUrl()).isEqualTo("matrix.org")
    }

    @Test
    fun `serverNameOrBaseUrl keeps an explicit http scheme as an indicator`() {
        assertThat(AccountProvider.Generic("http://localhost:8080").serverNameOrBaseUrl()).isEqualTo("http://localhost:8080")
    }

    @Test
    fun `matches ignores the scheme, the case and a trailing slash`() {
        val typed = AccountProvider.Generic("Element.io/")
        assertThat(typed.matches(AccountProvider.Generic("https://element.io"))).isTrue()
        assertThat(
            typed.matches(
                AccountProvider.Managed(serverName = "element.io", baseUrl = "https://element.ems.host", canUseServerName = true)
            )
        ).isTrue()
    }

    @Test
    fun `matches a managed provider that cannot use its server name only through its base url`() {
        val managed = AccountProvider.Managed(
            serverName = "element.io",
            baseUrl = "https://element.ems.host",
            canUseServerName = false,
        )
        assertThat(AccountProvider.Generic("element.ems.host").matches(managed)).isTrue()
        assertThat(AccountProvider.Generic("element.io").matches(managed)).isFalse()
    }

    @Test
    fun `matches is false for a different account provider`() {
        val typed = AccountProvider.Generic("element.io")
        assertThat(typed.matches(AccountProvider.Generic("matrix.org"))).isFalse()
        // A managed provider that can use its server name is not reached through its base url.
        assertThat(
            typed.matches(
                AccountProvider.Managed(serverName = "other.io", baseUrl = "https://element.io", canUseServerName = true)
            )
        ).isFalse()
    }
}
