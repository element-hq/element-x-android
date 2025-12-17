/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class LeaveSpaceStateTest {
    @Test
    fun `test loading`() {
        val sut = aLeaveSpaceState(
            selectableSpaceRooms = AsyncData.Loading()
        )
        assertThat(sut.showQuickAction).isFalse()
        assertThat(sut.showLeaveButton).isFalse()
        assertThat(sut.areAllSelected).isTrue()
        assertThat(sut.hasOnlyLastAdminRoom).isFalse()
        assertThat(sut.selectedRoomsCount).isEqualTo(0)
    }

    @Test
    fun `test no rooms`() {
        val sut = aLeaveSpaceState(
            selectableSpaceRooms = AsyncData.Success(
                persistentListOf()
            )
        )
        assertThat(sut.showQuickAction).isFalse()
        assertThat(sut.showLeaveButton).isTrue()
        assertThat(sut.areAllSelected).isTrue()
        assertThat(sut.hasOnlyLastAdminRoom).isFalse()
        assertThat(sut.selectedRoomsCount).isEqualTo(0)
    }

    @Test
    fun `test last admin`() {
        val sut = aLeaveSpaceState(
            isLastAdmin = true,
            selectableSpaceRooms = AsyncData.Success(
                persistentListOf(
                    aSelectableSpaceRoom(isLastAdmin = false, isSelected = false),
                )
            )
        )
        assertThat(sut.showQuickAction).isFalse()
        assertThat(sut.showLeaveButton).isFalse()
        assertThat(sut.areAllSelected).isFalse()
        assertThat(sut.hasOnlyLastAdminRoom).isFalse()
        assertThat(sut.selectedRoomsCount).isEqualTo(0)
    }

    @Test
    fun `test no last admin, 1 selected, 1 not selected`() {
        val sut = aLeaveSpaceState(
            selectableSpaceRooms = AsyncData.Success(
                listOf(
                    aSelectableSpaceRoom(isLastAdmin = false, isSelected = true),
                    aSelectableSpaceRoom(isLastAdmin = false, isSelected = false),
                ).toImmutableList()
            )
        )
        assertThat(sut.showQuickAction).isTrue()
        assertThat(sut.showLeaveButton).isTrue()
        assertThat(sut.areAllSelected).isFalse()
        assertThat(sut.hasOnlyLastAdminRoom).isFalse()
        assertThat(sut.selectedRoomsCount).isEqualTo(1)
    }

    @Test
    fun `test no last admin, 2 selected`() {
        val sut = aLeaveSpaceState(
            selectableSpaceRooms = AsyncData.Success(
                listOf(
                    aSelectableSpaceRoom(isLastAdmin = false, isSelected = true),
                    aSelectableSpaceRoom(isLastAdmin = false, isSelected = true),
                ).toImmutableList()
            )
        )
        assertThat(sut.showQuickAction).isTrue()
        assertThat(sut.showLeaveButton).isTrue()
        assertThat(sut.areAllSelected).isTrue()
        assertThat(sut.hasOnlyLastAdminRoom).isFalse()
        assertThat(sut.selectedRoomsCount).isEqualTo(2)
    }

    @Test
    fun `test 1 last admin, 2 selected`() {
        val sut = aLeaveSpaceState(
            selectableSpaceRooms = AsyncData.Success(
                persistentListOf(
                    aSelectableSpaceRoom(isLastAdmin = true, isSelected = false),
                    aSelectableSpaceRoom(isLastAdmin = false, isSelected = true),
                    aSelectableSpaceRoom(isLastAdmin = false, isSelected = true),
                )
            )
        )
        assertThat(sut.showQuickAction).isTrue()
        assertThat(sut.showLeaveButton).isTrue()
        assertThat(sut.areAllSelected).isTrue()
        assertThat(sut.hasOnlyLastAdminRoom).isFalse()
        assertThat(sut.selectedRoomsCount).isEqualTo(2)
    }

    @Test
    fun `test only last admin`() {
        val sut = aLeaveSpaceState(
            selectableSpaceRooms = AsyncData.Success(
                listOf(
                    aSelectableSpaceRoom(isLastAdmin = true, isSelected = false),
                    aSelectableSpaceRoom(isLastAdmin = true, isSelected = false),
                ).toImmutableList()
            )
        )
        assertThat(sut.showQuickAction).isFalse()
        assertThat(sut.showLeaveButton).isTrue()
        assertThat(sut.areAllSelected).isTrue()
        assertThat(sut.hasOnlyLastAdminRoom).isTrue()
        assertThat(sut.selectedRoomsCount).isEqualTo(0)
    }
}
