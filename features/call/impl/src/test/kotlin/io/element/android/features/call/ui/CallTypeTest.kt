/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.ui

import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.api.CallType
import io.element.android.features.call.impl.ui.getSessionId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import org.junit.Test

class CallTypeTest {
    @Test
    fun `getSessionId returns null for ExternalUrl`() {
        assertThat(CallType.ExternalUrl("aURL").getSessionId()).isNull()
    }

    @Test
    fun `getSessionId returns the sessionId for RoomCall`() {
        assertThat(
            CallType.RoomCall(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
            ).getSessionId()
        ).isEqualTo(A_SESSION_ID)
    }

    @Test
    fun `ExternalUrl stringification does not contain the URL`() {
        assertThat(CallType.ExternalUrl("aURL").toString()).isEqualTo("ExternalUrl")
    }

    @Test
    fun `RoomCall stringification does not contain the URL`() {
        assertThat(CallType.RoomCall(A_SESSION_ID, A_ROOM_ID).toString())
            .isEqualTo("RoomCall(sessionId=$A_SESSION_ID, roomId=$A_ROOM_ID)")
    }
}
