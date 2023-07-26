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

package io.element.android.features.location.impl.show

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.features.location.api.Location
import io.element.android.features.location.impl.common.actions.FakeLocationActions
import io.element.android.features.location.impl.common.permissions.PermissionsPresenter
import io.element.android.features.location.impl.common.permissions.PermissionsPresenterFake
import io.element.android.features.location.impl.common.permissions.PermissionsState
import io.element.android.libraries.matrix.test.core.aBuildMeta
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ShowLocationPresenterTest {

    private val permissionsPresenterFake = PermissionsPresenterFake()
    private val fakeLocationActions = FakeLocationActions()
    private val fakeBuildMeta = aBuildMeta(applicationName = "app name")
    private val location = Location(1.23, 4.56, 7.8f)
    private val presenter = ShowLocationPresenter(
        permissionsPresenterFactory = object : PermissionsPresenter.Factory {
            override fun create(permissions: List<String>): PermissionsPresenter = permissionsPresenterFake
        },
        fakeLocationActions,
        fakeBuildMeta,
        location,
        A_DESCRIPTION,
    )

    @Test
    fun `emits initial state with no location permission`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.location).isEqualTo(location)
            Truth.assertThat(initialState.description).isEqualTo(A_DESCRIPTION)
            Truth.assertThat(initialState.hasLocationPermission).isEqualTo(false)
            Truth.assertThat(initialState.isTrackMyLocation).isEqualTo(false)
        }
    }

    @Test
    fun `emits initial state with location permission`() = runTest {
        permissionsPresenterFake.givenState(PermissionsState(permissions = PermissionsState.Permissions.AllGranted))

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.location).isEqualTo(location)
            Truth.assertThat(initialState.description).isEqualTo(A_DESCRIPTION)
            Truth.assertThat(initialState.hasLocationPermission).isEqualTo(true)
            Truth.assertThat(initialState.isTrackMyLocation).isEqualTo(false)
        }
    }

    @Test
    fun `emits initial state with partial location permission`() = runTest {
        permissionsPresenterFake.givenState(PermissionsState(permissions = PermissionsState.Permissions.SomeGranted))

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.location).isEqualTo(location)
            Truth.assertThat(initialState.description).isEqualTo(A_DESCRIPTION)
            Truth.assertThat(initialState.hasLocationPermission).isEqualTo(true)
            Truth.assertThat(initialState.isTrackMyLocation).isEqualTo(false)
        }
    }

    @Test
    fun `uses action to share location`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ShowLocationEvents.Share)

            Truth.assertThat(fakeLocationActions.sharedLocation).isEqualTo(location)
            Truth.assertThat(fakeLocationActions.sharedLabel).isEqualTo(A_DESCRIPTION)
        }
    }

    @Test
    fun `centers on user location`() = runTest {
        permissionsPresenterFake.givenState(PermissionsState(permissions = PermissionsState.Permissions.AllGranted))

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.hasLocationPermission).isEqualTo(true)
            Truth.assertThat(initialState.isTrackMyLocation).isEqualTo(false)

            initialState.eventSink(ShowLocationEvents.TrackMyLocation(true))
            val trackMyLocationState = awaitItem()

            delay(1)

            Truth.assertThat(trackMyLocationState.hasLocationPermission).isEqualTo(true)
            Truth.assertThat(trackMyLocationState.isTrackMyLocation).isEqualTo(true)
        }
    }

    companion object {
        private const val A_DESCRIPTION = "My happy place"
    }
}
