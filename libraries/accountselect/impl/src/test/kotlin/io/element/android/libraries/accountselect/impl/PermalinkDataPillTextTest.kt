/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class PermalinkDataPillTextTest : RobolectricTest() {
    @Test
    fun `room alias link is rendered with the alias`() {
        assertThat(PermalinkData.RoomLink(RoomIdOrAlias.Alias(A_ROOM_ALIAS)).toPillText()).isEqualTo(A_ROOM_ALIAS.value)
    }

    @Test
    fun `room id link is rendered with the room id`() {
        assertThat(PermalinkData.RoomLink(RoomIdOrAlias.Id(A_ROOM_ID)).toPillText()).isEqualTo(A_ROOM_ID.value)
    }

    @Test
    fun `message link is rendered as a message mention`() {
        val permalinkData = PermalinkData.RoomLink(RoomIdOrAlias.Alias(A_ROOM_ALIAS), eventId = AN_EVENT_ID)
        assertThat(permalinkData.toPillText()).isEqualTo("💬 > ${A_ROOM_ALIAS.value}")
    }

    @Test
    fun `thread link is rendered as a message mention`() {
        val permalinkData = PermalinkData.RoomLink(RoomIdOrAlias.Id(A_ROOM_ID), threadId = A_THREAD_ID)
        assertThat(permalinkData.toPillText()).isEqualTo("💬 > ${A_ROOM_ID.value}")
    }

    @Test
    fun `user link is rendered with the user id`() {
        assertThat(PermalinkData.UserLink(A_USER_ID).toPillText()).isEqualTo(A_USER_ID.value)
    }

    @Test
    fun `fallback link is not rendered`() {
        assertThat(PermalinkData.FallbackLink(Uri.parse("https://example.org")).toPillText()).isNull()
    }
}
