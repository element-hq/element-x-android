/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.createroom.impl.addpeople

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.createroom.impl.CreateRoomDataStore
import io.element.android.features.createroom.impl.userlist.FakeUserListPresenterFactory
import io.element.android.features.createroom.impl.userlist.UserListDataStore
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
            CreateRoomDataStore(UserListDataStore())
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
