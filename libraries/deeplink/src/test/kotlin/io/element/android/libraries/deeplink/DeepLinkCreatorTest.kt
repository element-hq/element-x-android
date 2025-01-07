/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
