/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.permalink

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.metadata.withReleaseBehavior
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.tests.testutils.assertThrowsInDebug
import org.junit.Test

class DefaultPermalinkBuilderTest {
    @Test
    fun `building a permalink for an invalid user id throws when verifying the id`() {
        assertThrowsInDebug {
            val userId = UserId("some invalid user id")
            DefaultPermalinkBuilder().permalinkForUser(userId)
        }
    }

    @Test
    fun `building a permalink for an invalid user id returns failure when not verifying the id`() {
        withReleaseBehavior {
            val userId = UserId("some invalid user id")
            assertThat(DefaultPermalinkBuilder().permalinkForUser(userId).isFailure).isTrue()
        }
    }

    @Test
    fun `building a permalink for a valid user id returns a matrix-to url`() {
        val userId = UserId("@user:matrix.org")
        assertThat(DefaultPermalinkBuilder().permalinkForUser(userId).getOrNull()).isEqualTo("https://matrix.to/#/@user:matrix.org")
    }
}
