/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.deeplink

import android.content.Intent
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.tests.testutils.assertThrowsInDebug
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeeplinkParserTest {
    companion object {
        const val A_URI =
            "elementx://open/@alice:server.org"
        const val A_URI_WITH_ROOM =
            "elementx://open/@alice:server.org/!aRoomId:domain"
        const val A_URI_WITH_ROOM_WITH_THREAD =
            "elementx://open/@alice:server.org/!aRoomId:domain/\$aThreadId"
    }

    private val sut = DeeplinkParser()

    @Test
    fun `nominal cases`() {
        assertThat(sut.getFromIntent(createIntent(A_URI)))
            .isEqualTo(DeeplinkData.Root(A_SESSION_ID))
        assertThat(sut.getFromIntent(createIntent(A_URI_WITH_ROOM)))
            .isEqualTo(DeeplinkData.Room(A_SESSION_ID, A_ROOM_ID, null))
        assertThat(sut.getFromIntent(createIntent(A_URI_WITH_ROOM_WITH_THREAD)))
            .isEqualTo(DeeplinkData.Room(A_SESSION_ID, A_ROOM_ID, A_THREAD_ID))
    }

    @Test
    fun `error cases`() {
        val sut = DeeplinkParser()
        // Bad scheme
        assertThat(sut.getFromIntent(createIntent("x://open/@alice:server.org"))).isNull()
        // Bad host
        assertThat(sut.getFromIntent(createIntent("elementx://close/@alice:server.org"))).isNull()
        // No session Id
        assertThat(sut.getFromIntent(createIntent("elementx://open"))).isNull()

        assertThrowsInDebug {
            // Invalid sessionId
            sut.getFromIntent(createIntent("elementx://open/alice:server.org"))
        }
        assertThrowsInDebug {
            // Empty sessionId
            sut.getFromIntent(createIntent("elementx://open//"))
        }
    }

    private fun createIntent(uri: String): Intent {
        return Intent().apply {
            action = Intent.ACTION_VIEW
            data = uri.toUri()
        }
    }
}
