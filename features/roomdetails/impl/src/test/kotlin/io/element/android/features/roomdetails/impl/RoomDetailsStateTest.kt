/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class RoomDetailsStateTest {
    @Test
    fun `room not public not encrypted should have no badges`() {
        val sut = aRoomDetailsState(
            isPublic = false,
            isEncrypted = false,
        )
        assertThat(sut.roomBadges).isEmpty()
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
}
