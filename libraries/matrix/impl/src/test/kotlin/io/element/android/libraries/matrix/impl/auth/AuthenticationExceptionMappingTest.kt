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

package io.element.android.libraries.matrix.impl.auth

import com.google.common.truth.ThrowableSubject
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.auth.AuthenticationException
import org.junit.Test
import org.matrix.rustcomponents.sdk.ClientBuildException

class AuthenticationExceptionMappingTest {
    @Test
    fun `mapping an exception with no message returns 'Unknown error' message`() {
        val exception = Exception()
        val mappedException = exception.mapAuthenticationException()
        assertThat(mappedException.message).isEqualTo("Unknown error")
    }

    @Test
    fun `mapping a generic exception returns a Generic AuthenticationException`() {
        val exception = Exception("Generic exception")
        val mappedException = exception.mapAuthenticationException()
        assertThat(mappedException).isException<AuthenticationException.Generic>("Generic exception")
    }

    @Test
    fun `mapping specific exceptions map to their kotlin counterparts`() {
        assertThat(ClientBuildException.Generic("Unknown error").mapAuthenticationException())
            .isException<AuthenticationException.Generic>("Unknown error")

        assertThat(ClientBuildException.InvalidServerName("Invalid server name").mapAuthenticationException())
            .isException<AuthenticationException.InvalidServerName>("Invalid server name")

        assertThat(ClientBuildException.Sdk("SDK issue").mapAuthenticationException())
            .isException<AuthenticationException.Generic>("SDK issue")

        assertThat(ClientBuildException.SlidingSyncNotAvailable("Sliding sync not available").mapAuthenticationException())
            .isException<AuthenticationException.SlidingSyncNotAvailable>("Sliding sync not available")
    }

    private inline fun <reified T> ThrowableSubject.isException(message: String) {
        isInstanceOf(T::class.java)
        hasMessageThat().isEqualTo(message)
    }
}
