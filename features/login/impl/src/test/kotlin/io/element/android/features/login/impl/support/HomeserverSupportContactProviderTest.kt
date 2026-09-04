/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.support

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HomeserverSupportContactProviderTest {
    @Test
    fun `an admin contact is preferred over the other roles`() {
        val support = MatrixSupport(
            contacts = listOf(
                MatrixSupportContact(role = "m.role.security", emailAddress = "security@example.org"),
                MatrixSupportContact(role = "m.role.admin", emailAddress = "admin@example.org"),
            ),
        )
        assertThat(support.preferredContact()).isEqualTo("admin@example.org")
    }

    @Test
    fun `an email address is preferred over a Matrix ID`() {
        val support = MatrixSupport(
            contacts = listOf(
                MatrixSupportContact(role = "m.role.admin", matrixId = "@admin:example.org", emailAddress = "admin@example.org"),
            ),
        )
        assertThat(support.preferredContact()).isEqualTo("admin@example.org")
    }

    @Test
    fun `a Matrix ID is used when there is no email address`() {
        val support = MatrixSupport(
            contacts = listOf(MatrixSupportContact(role = "m.role.admin", matrixId = "@admin:example.org")),
        )
        assertThat(support.preferredContact()).isEqualTo("@admin:example.org")
    }

    @Test
    fun `any contact is used when none of them declares the admin role`() {
        val support = MatrixSupport(
            contacts = listOf(MatrixSupportContact(emailAddress = "hello@example.org")),
        )
        assertThat(support.preferredContact()).isEqualTo("hello@example.org")
    }

    @Test
    fun `the support page is the last resort`() {
        val support = MatrixSupport(
            contacts = listOf(MatrixSupportContact(role = "m.role.admin")),
            supportPage = "https://example.org/support",
        )
        assertThat(support.preferredContact()).isEqualTo("https://example.org/support")
    }

    @Test
    fun `a homeserver advertising nothing has no contact`() {
        assertThat(MatrixSupport().preferredContact()).isNull()
        assertThat(MatrixSupport(contacts = emptyList(), supportPage = "  ").preferredContact()).isNull()
    }
}
