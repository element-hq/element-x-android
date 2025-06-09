/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiHomeserverLoginDetails
import org.junit.Test

class HomeserverDetailsKtTest {
    @Test
    fun `map should be correct`() {
        // Given
        val homeserverLoginDetails = FakeFfiHomeserverLoginDetails(
            url = "https://example.org",
            supportsPasswordLogin = true,
            supportsOidcLogin = false
        )

        // When
        val result = homeserverLoginDetails.map()

        // Then
        assertThat(result).isEqualTo(
            MatrixHomeServerDetails(
                url = "https://example.org",
                supportsPasswordLogin = true,
                supportsOidcLogin = false
            )
        )
    }
}
