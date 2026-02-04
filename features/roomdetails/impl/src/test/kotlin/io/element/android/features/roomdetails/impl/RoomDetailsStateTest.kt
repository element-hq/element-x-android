/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class RoomDetailsStateTest {
    @Test
    fun `room not public not encrypted should have not encrypted badge`() {
        val sut = aRoomDetailsState(
            isPublic = false,
            isEncrypted = false,
        )
        assertThat(sut.roomBadges).isEqualTo(
            persistentListOf(RoomBadge.NOT_ENCRYPTED)
        )
    }

    @Test
    fun `room public not encrypted should have not encrypted and public badges`() {
        val sut = aRoomDetailsState(
            isPublic = true,
            isEncrypted = false,
        )
        assertThat(sut.roomBadges).isEqualTo(
            persistentListOf(RoomBadge.NOT_ENCRYPTED, RoomBadge.PUBLIC)
        )
    }

    @Test
    fun `room public encrypted should have encrypted and public badges`() {
        val sut = aRoomDetailsState(
            isPublic = true,
            isEncrypted = true,
        )
        assertThat(sut.roomBadges).isEqualTo(
            persistentListOf(RoomBadge.ENCRYPTED, RoomBadge.PUBLIC)
        )
    }

    @Test
    fun `room not public encrypted should have encrypted badges`() {
        val sut = aRoomDetailsState(
            isPublic = false,
            isEncrypted = true,
        )
        assertThat(sut.roomBadges).isEqualTo(
            persistentListOf(RoomBadge.ENCRYPTED)
        )
    }

    @Test
    fun `room public not encrypted should not have history sharing badges`() {
        val sut = aRoomDetailsState(
            isEncrypted = false,
            enableKeyShareOnInvite = true,
            roomHistoryVisibility = RoomHistoryVisibility.Shared
        )
        assertThat(sut.roomBadges).isEqualTo(
            persistentListOf(RoomBadge.NOT_ENCRYPTED, RoomBadge.PUBLIC)
        )
    }

    @Test
    fun `room public encrypted should have history sharing hidden badge`() {
        val sut = aRoomDetailsState(
            isEncrypted = true,
            enableKeyShareOnInvite = true,
            roomHistoryVisibility = RoomHistoryVisibility.Joined
        )
        assertThat(sut.roomBadges).isEqualTo(
            persistentListOf(RoomBadge.ENCRYPTED, RoomBadge.PUBLIC, RoomBadge.SHARED_HISTORY_HIDDEN)
        )
    }

    @Test
    fun `room public encrypted should have history sharing shared badge`() {
        val sut = aRoomDetailsState(
            isEncrypted = true,
            enableKeyShareOnInvite = true,
            roomHistoryVisibility = RoomHistoryVisibility.Shared
        )
        assertThat(sut.roomBadges).isEqualTo(
            persistentListOf(RoomBadge.ENCRYPTED, RoomBadge.PUBLIC, RoomBadge.SHARED_HISTORY_SHARED)
        )
    }

    @Test
    fun `room public encrypted should have history sharing world_readable badge`() {
        val sut = aRoomDetailsState(
            isEncrypted = true,
            enableKeyShareOnInvite = true,
            roomHistoryVisibility = RoomHistoryVisibility.WorldReadable
        )
        assertThat(sut.roomBadges).isEqualTo(
            persistentListOf(RoomBadge.ENCRYPTED, RoomBadge.PUBLIC, RoomBadge.SHARED_HISTORY_WORLD_READABLE)
        )
    }
}
