/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.root

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import io.element.android.libraries.previewutils.room.aSpaceRoom
import org.junit.Test

class SpaceStateTest {
    @Test
    fun `test default state`() {
        val state = aSpaceState()
        assertThat(state.hasAnyJoinFailures).isFalse()
        assertThat(state.isJoining(A_ROOM_ID)).isFalse()
    }

    @Test
    fun `test has failure`() {
        val state = aSpaceState(
            joinActions = mapOf(
                A_ROOM_ID to AsyncAction.Uninitialized,
                A_ROOM_ID_2 to AsyncAction.Failure(AN_EXCEPTION),
                A_ROOM_ID_3 to AsyncAction.Success(Unit),
            )
        )
        assertThat(state.hasAnyJoinFailures).isTrue()
    }

    @Test
    fun `test isJoining`() {
        val state = aSpaceState(
            joinActions = mapOf(
                A_ROOM_ID to AsyncAction.Loading,
            )
        )
        assertThat(state.isJoining(A_ROOM_ID)).isTrue()
    }

    @Test
    fun `test isSelected returns true for selected room`() {
        val state = aSpaceState(
            selectedRoomIds = setOf(A_ROOM_ID)
        )
        assertThat(state.isSelected(A_ROOM_ID)).isTrue()
    }

    @Test
    fun `test isSelected returns false for non-selected room`() {
        val state = aSpaceState(
            selectedRoomIds = setOf(A_ROOM_ID)
        )
        assertThat(state.isSelected(A_ROOM_ID_2)).isFalse()
    }

    @Test
    fun `test showManageRoomsAction true when canManageRooms and has room children`() {
        val state = aSpaceState(
            canManageRooms = true,
            children = listOf(aSpaceRoom(roomType = RoomType.Room))
        )
        assertThat(state.showManageRoomsAction).isTrue()
    }

    @Test
    fun `test showManageRoomsAction false when canManageRooms but children empty`() {
        val state = aSpaceState(
            canManageRooms = true,
            children = emptyList()
        )
        assertThat(state.showManageRoomsAction).isFalse()
    }

    @Test
    fun `test showManageRoomsAction false when canManageRooms but only space children`() {
        val state = aSpaceState(
            canManageRooms = true,
            children = listOf(aSpaceRoom(roomType = RoomType.Space))
        )
        assertThat(state.showManageRoomsAction).isFalse()
    }

    @Test
    fun `test showManageRoomsAction false when has room children but canManageRooms false`() {
        val state = aSpaceState(
            canManageRooms = false,
            children = listOf(aSpaceRoom(roomType = RoomType.Room))
        )
        assertThat(state.showManageRoomsAction).isFalse()
    }

    @Test
    fun `test selectedCount returns correct count`() {
        val state = aSpaceState(
            selectedRoomIds = setOf(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3)
        )
        assertThat(state.selectedCount).isEqualTo(3)
    }

    @Test
    fun `test isRemoveButtonEnabled true when selectedRoomIds not empty`() {
        val state = aSpaceState(
            selectedRoomIds = setOf(A_ROOM_ID)
        )
        assertThat(state.isRemoveButtonEnabled).isTrue()
    }

    @Test
    fun `test isRemoveButtonEnabled false when selectedRoomIds empty`() {
        val state = aSpaceState(
            selectedRoomIds = emptySet()
        )
        assertThat(state.isRemoveButtonEnabled).isFalse()
    }
}
