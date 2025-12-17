/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.error

import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.R
import io.element.android.libraries.matrix.api.auth.AuthenticationException
import io.element.android.libraries.ui.strings.CommonStrings
import org.junit.Test

class ErrorFormatterTest {
    // region loginError
    @Test
    fun `loginError - invalid unknown error returns unknown error message`() {
        val error = RuntimeException("Some unknown error")
        assertThat(loginError(error)).isEqualTo(CommonStrings.error_unknown)
    }

    @Test
    fun `loginError - invalid auth error returns unknown error message`() {
        val error = AuthenticationException.SlidingSyncVersion("Some message. Also contains M_FORBIDDEN, but won't be parsed")
        assertThat(loginError(error)).isEqualTo(CommonStrings.error_unknown)
    }

    @Test
    fun `loginError - unknown error returns unknown error message`() {
        val error = AuthenticationException.Generic("M_UNKNOWN")
        assertThat(loginError(error)).isEqualTo(CommonStrings.error_unknown)
    }

    @Test
    fun `loginError - forbidden error returns incorrect credentials message`() {
        val error = AuthenticationException.Generic("M_FORBIDDEN")
        assertThat(loginError(error)).isEqualTo(R.string.screen_login_error_invalid_credentials)
    }

    @Test
    fun `loginError - user_deactivated error returns deactivated account message`() {
        val error = AuthenticationException.Generic("M_USER_DEACTIVATED")
        assertThat(loginError(error)).isEqualTo(R.string.screen_login_error_deactivated_account)
    }

    // endregion loginError
}
