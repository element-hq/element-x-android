/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ManageAuthorizedSpacesPresenterTest {
    @Test
    fun `present - initial state reflects shared state`() = runTest {
        val sharedStateHolder = SpaceSelectionStateHolder()
        val presenter = ManageAuthorizedSpacesPresenter(sharedStateHolder)
        presenter.test {
            with(awaitItem()) {
                assertThat(selectedIds).isEmpty()
                assertThat(isDoneButtonEnabled).isFalse()
            }
        }
    }

    @Test
    fun `present - state reflects shared state with pre-selected spaces`() = runTest {
        val sharedStateHolder = SpaceSelectionStateHolder()
        val roomId = A_ROOM_ID
        sharedStateHolder.update {
            it.copy(selectedSpaceIds = persistentListOf(roomId))
        }
        val presenter = ManageAuthorizedSpacesPresenter(sharedStateHolder)
        presenter.test {
            with(awaitItem()) {
                assertThat(selectedIds).containsExactly(roomId)
                assertThat(isDoneButtonEnabled).isTrue()
            }
        }
    }

    @Test
    fun `present - ToggleSpace event adds space to selectedIds in shared state`() = runTest {
        val sharedStateHolder = SpaceSelectionStateHolder()
        val presenter = ManageAuthorizedSpacesPresenter(sharedStateHolder)
        presenter.test {
            val initialState = awaitItem()
            val roomId = A_ROOM_ID
            initialState.eventSink(ManageAuthorizedSpacesEvent.ToggleSpace(roomId))
            with(awaitItem()) {
                assertThat(selectedIds).containsExactly(roomId)
                assertThat(isDoneButtonEnabled).isTrue()
            }
            // Verify the shared state is also updated
            assertThat(sharedStateHolder.state.value.selectedSpaceIds).containsExactly(roomId)
        }
    }

    @Test
    fun `present - ToggleSpace event removes space when already selected`() = runTest {
        val sharedStateHolder = SpaceSelectionStateHolder()
        sharedStateHolder.updateSelectedSpaceIds(persistentListOf(A_ROOM_ID))
        val presenter = ManageAuthorizedSpacesPresenter(sharedStateHolder)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.selectedIds).containsExactly(A_ROOM_ID)
            initialState.eventSink(ManageAuthorizedSpacesEvent.ToggleSpace(A_ROOM_ID))
            with(awaitItem()) {
                assertThat(selectedIds).isEmpty()
                assertThat(isDoneButtonEnabled).isFalse()
            }
            // Verify the shared state is also updated
            assertThat(sharedStateHolder.state.value.selectedSpaceIds).isEmpty()
        }
    }

    @Test
    fun `present - Done event sets completion to Completed`() = runTest {
        val sharedStateHolder = SpaceSelectionStateHolder()
        val presenter = ManageAuthorizedSpacesPresenter(sharedStateHolder)
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ManageAuthorizedSpacesEvent.Done)
            cancelAndIgnoreRemainingEvents()
            assertThat(sharedStateHolder.state.value.completion)
                .isEqualTo(SpaceSelectionState.Completion.Completed)
        }
    }

    @Test
    fun `present - Cancel event sets completion to Cancelled`() = runTest {
        val sharedStateHolder = SpaceSelectionStateHolder()
        val presenter = ManageAuthorizedSpacesPresenter(sharedStateHolder)
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ManageAuthorizedSpacesEvent.Cancel)
            cancelAndIgnoreRemainingEvents()
            assertThat(sharedStateHolder.state.value.completion)
                .isEqualTo(SpaceSelectionState.Completion.Cancelled)
        }
    }

    @Test
    fun `present - displays spaces from shared state`() = runTest {
        val sharedStateHolder = SpaceSelectionStateHolder()
        sharedStateHolder.update {
            it.copy(
                selectableSpaces = persistentSetOf(),
                unknownSpaceIds = persistentListOf(A_ROOM_ID),
            )
        }
        val presenter = ManageAuthorizedSpacesPresenter(sharedStateHolder)
        presenter.test {
            with(awaitItem()) {
                assertThat(selectableSpaces).isEmpty()
                assertThat(unknownSpaceIds).containsExactly(A_ROOM_ID)
            }
        }
    }
}
