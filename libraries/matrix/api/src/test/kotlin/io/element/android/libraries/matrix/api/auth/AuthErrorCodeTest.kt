/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AuthErrorCodeTest {
    @Test
    fun `errorCode finds UNKNOWN code`() {
        val error = AuthenticationException.Generic("M_UNKNOWN")
        assertThat(error.errorCode).isEqualTo(AuthErrorCode.UNKNOWN)
    }

    @Test
    fun `errorCode finds USER_DEACTIVATED code`() {
        val error = AuthenticationException.Generic("M_USER_DEACTIVATED")
        assertThat(error.errorCode).isEqualTo(AuthErrorCode.USER_DEACTIVATED)
    }

    @Test
    fun `errorCode finds FORBIDDEN code`() {
        val error = AuthenticationException.Generic("M_FORBIDDEN")
        assertThat(error.errorCode).isEqualTo(AuthErrorCode.FORBIDDEN)
    }

    @Test
    fun `errorCode cannot find code so it returns UNKNOWN`() {
        val error = AuthenticationException.Generic("Some other error")
        assertThat(error.errorCode).isEqualTo(AuthErrorCode.UNKNOWN)
    }
}
