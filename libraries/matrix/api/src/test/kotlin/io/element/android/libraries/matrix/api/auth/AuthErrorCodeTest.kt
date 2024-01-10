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
