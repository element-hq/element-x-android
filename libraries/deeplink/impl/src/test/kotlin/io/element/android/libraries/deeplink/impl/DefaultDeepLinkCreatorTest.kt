/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.deeplink.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import org.junit.Test

class DefaultDeepLinkCreatorTest {
    @Test
    fun create() {
        val sut = DefaultDeepLinkCreator()
        assertThat(sut.create(A_SESSION_ID, null, null, null))
            .isEqualTo("elementx://open/@alice:server.org")
        assertThat(sut.create(A_SESSION_ID, A_ROOM_ID, null, null))
            .isEqualTo("elementx://open/@alice:server.org/!aRoomId:domain")
        assertThat(sut.create(A_SESSION_ID, A_ROOM_ID, A_THREAD_ID, null))
            .isEqualTo("elementx://open/@alice:server.org/!aRoomId:domain/\$aThreadId")
        assertThat(sut.create(A_SESSION_ID, A_ROOM_ID, A_THREAD_ID, AN_EVENT_ID))
            .isEqualTo("elementx://open/@alice:server.org/!aRoomId:domain/\$aThreadId/\$anEventId")
        assertThat(sut.create(A_SESSION_ID, A_ROOM_ID, null, AN_EVENT_ID))
            .isEqualTo("elementx://open/@alice:server.org/!aRoomId:domain//\$anEventId")
    }
}
