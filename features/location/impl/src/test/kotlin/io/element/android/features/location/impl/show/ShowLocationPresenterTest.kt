/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.api.Location
import io.element.android.features.location.impl.aPermissionsState
import io.element.android.features.location.impl.common.actions.FakeLocationActions
import io.element.android.features.location.impl.common.permissions.FakePermissionsPresenter
import io.element.android.features.location.impl.common.permissions.PermissionsEvents
import io.element.android.features.location.impl.common.permissions.PermissionsPresenter
import io.element.android.features.location.impl.common.permissions.PermissionsState
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ShowLocationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val fakePermissionsPresenter = FakePermissionsPresenter()
    private val fakeLocationActions = FakeLocationActions()
    private val fakeBuildMeta = aBuildMeta(applicationName = "app name")
    private val location = Location(1.23, 4.56, 7.8f)
    private val presenter = ShowLocationPresenter(
        permissionsPresenterFactory = object : PermissionsPresenter.Factory {
            override fun create(permissions: List<String>): PermissionsPresenter = fakePermissionsPresenter
        },
        locationActions = fakeLocationActions,
        buildMeta = fakeBuildMeta,
        location = location,
        description = A_DESCRIPTION,
    )

    @Test
    fun `emits initial state with no location permission`() = runTest {
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.location).isEqualTo(location)
            assertThat(initialState.description).isEqualTo(A_DESCRIPTION)
            assertThat(initialState.hasLocationPermission).isFalse()
            assertThat(initialState.isTrackMyLocation).isFalse()
        }
    }

    @Test
    fun `emits initial state location permission denied once`() = runTest {
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = true,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.location).isEqualTo(location)
            assertThat(initialState.description).isEqualTo(A_DESCRIPTION)
            assertThat(initialState.hasLocationPermission).isFalse()
            assertThat(initialState.isTrackMyLocation).isFalse()
        }
    }

    @Test
    fun `emits initial state with location permission`() = runTest {
        fakePermissionsPresenter.givenState(aPermissionsState(permissions = PermissionsState.Permissions.AllGranted))

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.location).isEqualTo(location)
            assertThat(initialState.description).isEqualTo(A_DESCRIPTION)
            assertThat(initialState.hasLocationPermission).isTrue()
            assertThat(initialState.isTrackMyLocation).isFalse()
        }
    }

    @Test
    fun `emits initial state with partial location permission`() = runTest {
        fakePermissionsPresenter.givenState(aPermissionsState(permissions = PermissionsState.Permissions.SomeGranted))

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.location).isEqualTo(location)
            assertThat(initialState.description).isEqualTo(A_DESCRIPTION)
            assertThat(initialState.hasLocationPermission).isTrue()
            assertThat(initialState.isTrackMyLocation).isFalse()
        }
    }

    @Test
    fun `uses action to share location`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ShowLocationEvents.Share)

            assertThat(fakeLocationActions.sharedLocation).isEqualTo(location)
            assertThat(fakeLocationActions.sharedLabel).isEqualTo(A_DESCRIPTION)
        }
    }

    @Test
    fun `centers on user location`() = runTest {
        fakePermissionsPresenter.givenState(aPermissionsState(permissions = PermissionsState.Permissions.AllGranted))

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.hasLocationPermission).isTrue()
            assertThat(initialState.isTrackMyLocation).isFalse()

            initialState.eventSink(ShowLocationEvents.TrackMyLocation(true))
            val trackMyLocationState = awaitItem()

            delay(1)

            assertThat(trackMyLocationState.hasLocationPermission).isTrue()
            assertThat(trackMyLocationState.isTrackMyLocation).isTrue()

            // Swipe the map to switch mode
            initialState.eventSink(ShowLocationEvents.TrackMyLocation(false))
            val trackLocationDisabledState = awaitItem()
            assertThat(trackLocationDisabledState.permissionDialog).isEqualTo(ShowLocationState.Dialog.None)
            assertThat(trackLocationDisabledState.isTrackMyLocation).isFalse()
            assertThat(trackLocationDisabledState.hasLocationPermission).isTrue()
        }
    }

    @Test
    fun `rationale dialog dismiss`() = runTest {
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = true,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            // Click on the button to switch mode
            initialState.eventSink(ShowLocationEvents.TrackMyLocation(true))
            val trackLocationState = awaitItem()
            assertThat(trackLocationState.permissionDialog).isEqualTo(ShowLocationState.Dialog.PermissionRationale)
            assertThat(trackLocationState.isTrackMyLocation).isFalse()
            assertThat(trackLocationState.hasLocationPermission).isFalse()

            // Dismiss the dialog
            initialState.eventSink(ShowLocationEvents.DismissDialog)
            val dialogDismissedState = awaitItem()
            assertThat(dialogDismissedState.permissionDialog).isEqualTo(ShowLocationState.Dialog.None)
            assertThat(dialogDismissedState.isTrackMyLocation).isFalse()
            assertThat(dialogDismissedState.hasLocationPermission).isFalse()
        }
    }

    @Test
    fun `rationale dialog continue`() = runTest {
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = true,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            // Click on the button to switch mode
            initialState.eventSink(ShowLocationEvents.TrackMyLocation(true))
            val trackLocationState = awaitItem()
            assertThat(trackLocationState.permissionDialog).isEqualTo(ShowLocationState.Dialog.PermissionRationale)
            assertThat(trackLocationState.isTrackMyLocation).isFalse()
            assertThat(trackLocationState.hasLocationPermission).isFalse()

            // Continue the dialog sends permission request to the permissions presenter
            trackLocationState.eventSink(ShowLocationEvents.RequestPermissions)
            assertThat(fakePermissionsPresenter.events.last()).isEqualTo(PermissionsEvents.RequestPermissions)
        }
    }

    @Test
    fun `permission denied dialog dismiss`() = runTest {
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            // Click on the button to switch mode
            initialState.eventSink(ShowLocationEvents.TrackMyLocation(true))
            val trackLocationState = awaitItem()
            assertThat(trackLocationState.permissionDialog).isEqualTo(ShowLocationState.Dialog.PermissionDenied)
            assertThat(trackLocationState.isTrackMyLocation).isFalse()
            assertThat(trackLocationState.hasLocationPermission).isFalse()

            // Dismiss the dialog
            initialState.eventSink(ShowLocationEvents.DismissDialog)
            val dialogDismissedState = awaitItem()
            assertThat(dialogDismissedState.permissionDialog).isEqualTo(ShowLocationState.Dialog.None)
            assertThat(dialogDismissedState.isTrackMyLocation).isFalse()
            assertThat(dialogDismissedState.hasLocationPermission).isFalse()
        }
    }

    @Test
    fun `open settings activity`() = runTest {
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            initialState.eventSink(ShowLocationEvents.TrackMyLocation(true))
            val dialogShownState = awaitItem()

            // Open settings
            dialogShownState.eventSink(ShowLocationEvents.OpenAppSettings)
            val settingsOpenedState = awaitItem()

            assertThat(settingsOpenedState.permissionDialog).isEqualTo(ShowLocationState.Dialog.None)
            assertThat(fakeLocationActions.openSettingsInvocationsCount).isEqualTo(1)
        }
    }

    @Test
    fun `application name is in state`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.appName).isEqualTo("app name")
        }
    }

    companion object {
        private const val A_DESCRIPTION = "My happy place"
    }
}
