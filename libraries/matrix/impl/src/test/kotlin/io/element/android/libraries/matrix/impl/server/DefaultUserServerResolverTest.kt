/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.server

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.FakeMatrixClient
import org.junit.Test

class DefaultUserServerResolverTest {
    @Test
    fun resolve() {
        // Given
        val userServerResolver = DefaultUserServerResolver(FakeMatrixClient(
            userIdServerNameLambda = { "dummy.org" }
        ))

        // When
        val result = userServerResolver.resolve()

        // Then
        assertThat(result).isEqualTo("dummy.org")
    }
}
