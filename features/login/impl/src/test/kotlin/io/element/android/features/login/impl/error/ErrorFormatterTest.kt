/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        val error = Throwable("Some unknown error")
        assertThat(loginError(error)).isEqualTo(CommonStrings.error_unknown)
    }

    @Test
    fun `loginError - invalid auth error returns unknown error message`() {
        val error = AuthenticationException.SlidingSyncVersionMismatch("Some message. Also contains M_FORBIDDEN, but won't be parsed")
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
