/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.addpeople

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.createroom.impl.CreateRoomDataStore
import io.element.android.features.createroom.impl.userlist.FakeUserListPresenterFactory
import io.element.android.features.createroom.impl.userlist.UserListDataStore
import io.element.android.libraries.matrix.test.room.alias.FakeRoomAliasHelper
import io.element.android.libraries.usersearch.test.FakeUserRepository
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddPeoplePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private lateinit var presenter: AddPeoplePresenter

    @Before
    fun setup() {
        presenter = AddPeoplePresenter(
            FakeUserListPresenterFactory(),
            FakeUserRepository(),
            CreateRoomDataStore(UserListDataStore(), FakeRoomAliasHelper())
        )
    }

    @Test
    fun `present - initial state`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // TODO This doesn't actually test anything...
            val initialState = awaitItem()
            assertThat(initialState)
        }
    }
}
