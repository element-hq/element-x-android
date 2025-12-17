/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spaces

import com.google.common.truth.Truth.assertThat
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.invite.test.InMemorySeenInvitesStore
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HomeSpacesPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        presenter.test {
            val state = awaitItem()
            assertThat(state.space).isEqualTo(CurrentSpace.Root)
            assertThat(state.spaceRooms).isEmpty()
            assertThat(state.hideInvitesAvatar).isFalse()
            assertThat(state.seenSpaceInvites).isEmpty()
        }
    }

    private fun createPresenter(
        client: MatrixClient = FakeMatrixClient(),
        seenInvitesStore: SeenInvitesStore = InMemorySeenInvitesStore(),
    ) = HomeSpacesPresenter(
        client = client,
        seenInvitesStore = seenInvitesStore,
    )
}
