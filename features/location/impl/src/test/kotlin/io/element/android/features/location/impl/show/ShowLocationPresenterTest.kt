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
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.room.location.LastLocation
import io.element.android.libraries.matrix.api.room.location.LiveLocationShare
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
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
        joinedRoom: JoinedRoom = FakeJoinedRoom(),
    ) = ShowLocationPresenter(
        mode = mode,
        permissionsPresenterFactory = { fakePermissionsPresenter },
        locationActions = locationActions,
        buildMeta = fakeBuildMeta,
        dateFormatter = fakeDateFormatter,
        stringProvider = FakeStringProvider(),
        joinedRoom = joinedRoom,
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

    @Test
    fun `live mode emits empty location shares initially`() = runTest {
        val presenter = createShowLocationPresenter(
            mode = ShowLocationMode.Live(senderId = UserId("@alice:matrix.org")),
            joinedRoom = FakeJoinedRoom(),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.locationShares).isEmpty()
            assertThat(initialState.isSheetDraggable).isFalse()
        }
    }

    @Test
    fun `live mode collects live shares from room`() = runTest {
        val userId = UserId("@bob:matrix.org")
        val liveSharesFlow = MutableStateFlow(
            listOf(
                aLiveLocationShare(userId = userId)
            )
        )
        val fakeRoom = FakeJoinedRoom(liveLocationSharesFlow = liveSharesFlow)

        val presenter = createShowLocationPresenter(
            mode = ShowLocationMode.Live(senderId = userId),
            joinedRoom = fakeRoom,
        )
        presenter.test {
            // Skip initial empty state from collectAsState(initial = emptyList())
            skipItems(1)
            val state = awaitItem()

            assertThat(state.locationShares).hasSize(1)
            val item = state.locationShares.first()
            assertThat(item.userId).isEqualTo(userId)
            assertThat(item.location.lat).isEqualTo(48.8584)
            assertThat(item.location.lon).isEqualTo(2.2945)
            assertThat(item.isLive).isTrue()
            assertThat(state.isSheetDraggable).isTrue()
        }
    }

    @Test
    fun `live mode handles invalid geo uri gracefully`() = runTest {
        val validUserId = UserId("@alice:matrix.org")
        val invalidUserId = UserId("@bob:matrix.org")
        val liveSharesFlow = MutableStateFlow(
            listOf(
                aLiveLocationShare(userId = validUserId),
                aLiveLocationShare(userId = invalidUserId, geoUri = "invalid-geo-uri"),
            )
        )
        val fakeRoom = FakeJoinedRoom(liveLocationSharesFlow = liveSharesFlow)

        val presenter = createShowLocationPresenter(
            mode = ShowLocationMode.Live(senderId = validUserId),
            joinedRoom = fakeRoom,
        )
        presenter.test {
            // Skip initial empty state from collectAsState(initial = emptyList())
            skipItems(1)
            val state = awaitItem()

            // Only the valid location share should be present
            assertThat(state.locationShares).hasSize(1)
            assertThat(state.locationShares.first().userId).isEqualTo(validUserId)
        }
    }

    @Test
    fun `live mode updates when shares change`() = runTest {
        val userId = UserId("@bob:matrix.org")
        val liveSharesFlow = MutableStateFlow(emptyList<LiveLocationShare>())
        val fakeRoom = FakeJoinedRoom(liveLocationSharesFlow = liveSharesFlow)

        val presenter = createShowLocationPresenter(
            mode = ShowLocationMode.Live(senderId = userId),
            joinedRoom = fakeRoom,
        )
        presenter.test {
            // Initial state is empty
            val initialState = awaitItem()
            assertThat(initialState.locationShares).isEmpty()

            // Emit a new live share
            liveSharesFlow.value = listOf(
                aLiveLocationShare(userId = userId)
            )

            val updatedState = awaitItem()
            assertThat(updatedState.locationShares).hasSize(1)
            assertThat(updatedState.locationShares.first().userId).isEqualTo(userId)
        }
    }

    @Test
    fun `static mode emits location share with correct data`() = runTest {
        val senderId = UserId("@alice:matrix.org")
        val senderName = "Alice"
        val avatarUrl = "https://example.com/avatar.png"
        val mode = ShowLocationMode.Static(
            location = location,
            senderName = senderName,
            senderId = senderId,
            senderAvatarUrl = avatarUrl,
            timestamp = 0L,
            assetType = AssetType.SENDER,
        )

        val presenter = createShowLocationPresenter(mode = mode)
        presenter.test {
            val state = awaitItem()
            assertThat(state.locationShares).hasSize(1)

            val item = state.locationShares.first()
            assertThat(item.userId).isEqualTo(senderId)
            assertThat(item.displayName).isEqualTo(senderName)
            assertThat(item.location).isEqualTo(location)
            assertThat(item.isLive).isFalse()
            assertThat(item.assetType).isEqualTo(AssetType.SENDER)
            assertThat(item.avatarData.id).isEqualTo(senderId.value)
            assertThat(item.avatarData.name).isEqualTo(senderName)
            assertThat(item.avatarData.url).isEqualTo(avatarUrl)
        }
    }

    @Test
    fun `static mode has non-draggable sheet`() = runTest {
        val presenter = createShowLocationPresenter()
        presenter.test {
            val state = awaitItem()
            assertThat(state.isSheetDraggable).isFalse()
        }
    }
}

private fun aLiveLocationShare(
    userId: UserId,
    geoUri: String = "geo:48.8584,2.2945",
    timestamp: Long = 0L,
    startTimestamp: Long = 0L,
    endTimestamp: Long = Long.MAX_VALUE,
    assetType: AssetType = AssetType.SENDER,
): LiveLocationShare {
    return LiveLocationShare(
        userId = userId,
        lastLocation = LastLocation(
            geoUri = geoUri,
            timestamp = timestamp,
            assetType = assetType,
        ),
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp,
    )
}
