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

package io.element.android.libraries.deeplink

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import org.junit.Test

class DeepLinkCreatorTest {
    @Test
    fun room() {
        val sut = DeepLinkCreator()
        assertThat(sut.room(A_SESSION_ID, null, null))
            .isEqualTo("elementx://open/@alice:server.org")
        assertThat(sut.room(A_SESSION_ID, A_ROOM_ID, null))
            .isEqualTo("elementx://open/@alice:server.org/!aRoomId:domain")
        assertThat(sut.room(A_SESSION_ID, A_ROOM_ID, A_THREAD_ID))
            .isEqualTo("elementx://open/@alice:server.org/!aRoomId:domain/\$aThreadId")
    }
}
