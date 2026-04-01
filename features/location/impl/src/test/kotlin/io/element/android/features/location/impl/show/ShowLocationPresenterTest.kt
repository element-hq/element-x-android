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
import io.element.android.features.location.api.ShowLocationMode
import io.element.android.features.location.impl.aPermissionsState
import io.element.android.features.location.impl.common.actions.FakeLocationActions
import io.element.android.features.location.impl.common.permissions.FakePermissionsPresenter
import io.element.android.features.location.impl.common.permissions.PermissionsEvents
import io.element.android.features.location.impl.common.permissions.PermissionsState
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialogState
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
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
    private val fakeDateFormatter = FakeDateFormatter()
    private val location = Location(1.23, 4.56, 7.8f)

    private fun createShowLocationPresenter(
        mode: ShowLocationMode = ShowLocationMode.Static(
            location = location,
            senderName = "Alice",
            senderId = UserId("@alice:matrix.org"),
            senderAvatarUrl = null,
            timestamp = System.currentTimeMillis(),
            assetType = null,
        ),
        locationActions: FakeLocationActions = fakeLocationActions,
    ) = ShowLocationPresenter(
        mode = mode,
        permissionsPresenterFactory = { fakePermissionsPresenter },
        locationActions = locationActions,
        buildMeta = fakeBuildMeta,
        dateFormatter = fakeDateFormatter,
        stringProvider = FakeStringProvider()
    )

    @Test
    fun `emits initial state with no location permission`() = runTest {
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )

        val presenter = createShowLocationPresenter()
        presenter.test {
            val initialState = awaitItem()
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

        val presenter = createShowLocationPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.hasLocationPermission).isFalse()
            assertThat(initialState.isTrackMyLocation).isFalse()
        }
    }

    @Test
    fun `emits initial state with location permission`() = runTest {
        fakePermissionsPresenter.givenState(aPermissionsState(permissions = PermissionsState.Permissions.AllGranted))

        val presenter = createShowLocationPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.hasLocationPermission).isTrue()
            assertThat(initialState.isTrackMyLocation).isFalse()
        }
    }

    @Test
    fun `emits initial state with partial location permission`() = runTest {
        fakePermissionsPresenter.givenState(aPermissionsState(permissions = PermissionsState.Permissions.SomeGranted))

        val presenter = createShowLocationPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.hasLocationPermission).isTrue()
            assertThat(initialState.isTrackMyLocation).isFalse()
        }
    }

    @Test
    fun `uses action to share location`() = runTest {
        val presenter = createShowLocationPresenter()
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ShowLocationEvent.Share(location))

            assertThat(fakeLocationActions.sharedLocation).isEqualTo(location)
        }
    }

    @Test
    fun `centers on user location`() = runTest {
        fakePermissionsPresenter.givenState(aPermissionsState(permissions = PermissionsState.Permissions.AllGranted))

        val presenter = createShowLocationPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.hasLocationPermission).isTrue()
            assertThat(initialState.isTrackMyLocation).isFalse()

            initialState.eventSink(ShowLocationEvent.TrackMyLocation(true))
            val trackMyLocationState = awaitItem()

            delay(1)

            assertThat(trackMyLocationState.hasLocationPermission).isTrue()
            assertThat(trackMyLocationState.isTrackMyLocation).isTrue()

            // Swipe the map to switch mode
            initialState.eventSink(ShowLocationEvent.TrackMyLocation(false))
            val trackLocationDisabledState = awaitItem()
            assertThat(trackLocationDisabledState.dialogState).isEqualTo(LocationConstraintsDialogState.None)
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

        val presenter = createShowLocationPresenter()
        presenter.test {
            // Skip initial state
            val initialState = awaitItem()

            // Click on the button to switch mode
            initialState.eventSink(ShowLocationEvent.TrackMyLocation(true))
            val trackLocationState = awaitItem()
            assertThat(trackLocationState.dialogState).isEqualTo(LocationConstraintsDialogState.PermissionRationale)
            assertThat(trackLocationState.isTrackMyLocation).isFalse()
            assertThat(trackLocationState.hasLocationPermission).isFalse()

            // Dismiss the dialog
            initialState.eventSink(ShowLocationEvent.DismissDialog)
            val dialogDismissedState = awaitItem()
            assertThat(dialogDismissedState.dialogState).isEqualTo(LocationConstraintsDialogState.None)
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
        val presenter = createShowLocationPresenter()
       presenter.test {
            // Skip initial state
            val initialState = awaitItem()

            // Click on the button to switch mode
            initialState.eventSink(ShowLocationEvent.TrackMyLocation(true))
            val trackLocationState = awaitItem()
            assertThat(trackLocationState.dialogState).isEqualTo(LocationConstraintsDialogState.PermissionRationale)
            assertThat(trackLocationState.isTrackMyLocation).isFalse()
            assertThat(trackLocationState.hasLocationPermission).isFalse()

            // Continue the dialog sends permission request to the permissions presenter
            trackLocationState.eventSink(ShowLocationEvent.RequestPermissions)
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

        val presenter = createShowLocationPresenter()
        presenter.test {
            // Skip initial state
            val initialState = awaitItem()

            // Click on the button to switch mode
            initialState.eventSink(ShowLocationEvent.TrackMyLocation(true))
            val trackLocationState = awaitItem()
            assertThat(trackLocationState.dialogState).isEqualTo(LocationConstraintsDialogState.PermissionDenied)
            assertThat(trackLocationState.isTrackMyLocation).isFalse()
            assertThat(trackLocationState.hasLocationPermission).isFalse()

            // Dismiss the dialog
            initialState.eventSink(ShowLocationEvent.DismissDialog)
            val dialogDismissedState = awaitItem()
            assertThat(dialogDismissedState.dialogState).isEqualTo(LocationConstraintsDialogState.None)
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

        val presenter = createShowLocationPresenter()
        presenter.test {
            // Skip initial state
            val initialState = awaitItem()

            initialState.eventSink(ShowLocationEvent.TrackMyLocation(true))
            val dialogShownState = awaitItem()

            // Open settings
            dialogShownState.eventSink(ShowLocationEvent.OpenAppSettings)
            val settingsOpenedState = awaitItem()

            assertThat(settingsOpenedState.dialogState).isEqualTo(LocationConstraintsDialogState.None)
            assertThat(fakeLocationActions.openSettingsInvocationsCount).isEqualTo(1)
        }
    }

    @Test
    fun `application name is in state`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            createShowLocationPresenter().present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.appName).isEqualTo("app name")
        }
    }

    @Test
    fun `location service disabled shows dialog`() = runTest {
        fakePermissionsPresenter.givenState(aPermissionsState(permissions = PermissionsState.Permissions.AllGranted))
        fakeLocationActions.givenLocationEnabled(false)

        val presenter = createShowLocationPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.hasLocationPermission).isTrue()

            // Try to track location when location services are disabled
            initialState.eventSink(ShowLocationEvent.TrackMyLocation(true))
            val dialogShownState = awaitItem()

            assertThat(dialogShownState.dialogState).isEqualTo(LocationConstraintsDialogState.LocationServiceDisabled)
            assertThat(dialogShownState.isTrackMyLocation).isFalse()
        }
    }

    @Test
    fun `open location settings from dialog`() = runTest {
        fakePermissionsPresenter.givenState(aPermissionsState(permissions = PermissionsState.Permissions.AllGranted))
        fakeLocationActions.givenLocationEnabled(false)

        val presenter = createShowLocationPresenter()
        presenter.test {
            val initialState = awaitItem()

            initialState.eventSink(ShowLocationEvent.TrackMyLocation(true))
            val dialogShownState = awaitItem()
            assertThat(dialogShownState.dialogState).isEqualTo(LocationConstraintsDialogState.LocationServiceDisabled)

            // Open location settings
            dialogShownState.eventSink(ShowLocationEvent.OpenLocationSettings)
            val settingsOpenedState = awaitItem()

            assertThat(settingsOpenedState.dialogState).isEqualTo(LocationConstraintsDialogState.None)
            assertThat(fakeLocationActions.openLocationSettingsInvocationsCount).isEqualTo(1)
        }
    }
}
