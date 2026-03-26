/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.location.impl.share

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.Composer
import io.element.android.features.location.api.Location
import io.element.android.features.location.impl.aPermissionsState
import io.element.android.features.location.impl.common.actions.FakeLocationActions
import io.element.android.features.location.impl.common.permissions.FakePermissionsPresenter
import io.element.android.features.location.impl.common.permissions.PermissionsEvents
import io.element.android.features.location.impl.common.permissions.PermissionsState
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialogState
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.libraries.dateformatter.test.FakeDurationFormatter
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ShareLocationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val fakePermissionsPresenter = FakePermissionsPresenter()
    private val fakeAnalyticsService = FakeAnalyticsService()
    private val fakeMessageComposerContext = FakeMessageComposerContext()
    private val fakeLocationActions = FakeLocationActions()
    private val fakeBuildMeta = aBuildMeta(applicationName = "app name")
    private val fakeFeatureFlagService = FakeFeatureFlagService()
    private val fakeMatrixClient = FakeMatrixClient(sessionId = A_USER_ID)

    private val durationFormatter = FakeDurationFormatter()

    private fun createShareLocationPresenter(
        joinedRoom: JoinedRoom = FakeJoinedRoom(),
        locationActions: FakeLocationActions = fakeLocationActions,
    ): ShareLocationPresenter = ShareLocationPresenter(
        permissionsPresenterFactory = { fakePermissionsPresenter },
        room = joinedRoom,
        timelineMode = Timeline.Mode.Live,
        analyticsService = fakeAnalyticsService,
        messageComposerContext = fakeMessageComposerContext,
        locationActions = locationActions,
        buildMeta = fakeBuildMeta,
        featureFlagService = fakeFeatureFlagService,
        client = fakeMatrixClient,
        durationFormatter = durationFormatter,
    )

    @Test
    fun `initial state with permissions granted and location enabled`() = runTest {
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.AllGranted,
                shouldShowRationale = false,
            )
        )

        val shareLocationPresenter = createShareLocationPresenter()
        shareLocationPresenter.test {
            skipItems(1)
            val state = awaitItem()
            assertThat(state.trackUserLocation).isTrue()
            assertThat(state.hasLocationPermission).isTrue()
            assertThat(state.dialogState).isEqualTo(ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.None))
        }
    }

    @Test
    fun `initial state with permissions partially granted and location enabled`() = runTest {
        val shareLocationPresenter = createShareLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.SomeGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            shareLocationPresenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.trackUserLocation).isTrue()
            assertThat(initialState.hasLocationPermission).isTrue()
            assertThat(initialState.dialogState).isEqualTo(ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.None))
        }
    }

    @Test
    fun `initial state with permissions denied`() = runTest {
        val shareLocationPresenter = createShareLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            shareLocationPresenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.trackUserLocation).isFalse()
            assertThat(initialState.hasLocationPermission).isFalse()
            assertThat(initialState.dialogState).isEqualTo(
                ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionDenied)
            )
        }
    }

    @Test
    fun `initial state with permissions denied with rationale`() = runTest {
        val shareLocationPresenter = createShareLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = true,
            )
        )

        shareLocationPresenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.trackUserLocation).isFalse()
            assertThat(initialState.hasLocationPermission).isFalse()
            assertThat(initialState.dialogState).isEqualTo(
                ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionRationale)
            )
        }
    }

    @Test
    fun `initial state with location services disabled`() = runTest {
        val locationActions = FakeLocationActions(isLocationEnabled = false)
        val shareLocationPresenter = createShareLocationPresenter(locationActions = locationActions)
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.AllGranted,
                shouldShowRationale = false,
            )
        )

        shareLocationPresenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.trackUserLocation).isFalse()
            assertThat(initialState.hasLocationPermission).isTrue()
            assertThat(initialState.dialogState).isEqualTo(
                ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.LocationServiceDisabled)
            )
        }
    }

    @Test
    fun `StopTrackingUserLocation event sets trackUserLocation to false`() = runTest {
        val shareLocationPresenter = createShareLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.AllGranted,
                shouldShowRationale = false,
            )
        )

        shareLocationPresenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.trackUserLocation).isTrue()

            initialState.eventSink(ShareLocationEvent.StopTrackingUserLocation)
            val stoppedState = awaitItem()
            assertThat(stoppedState.trackUserLocation).isFalse()
        }
    }

    @Test
    fun `DismissDialog event clears dialog state`() = runTest {
        val shareLocationPresenter = createShareLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = true,
            )
        )

        shareLocationPresenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.dialogState).isEqualTo(
                ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionRationale)
            )

            initialState.eventSink(ShareLocationEvent.DismissDialog)
            val dismissedState = awaitItem()
            assertThat(dismissedState.dialogState).isEqualTo(ShareLocationState.Dialog.None)
        }
    }

    @Test
    fun `RequestPermissions event triggers permission request`() = runTest {
        val shareLocationPresenter = createShareLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = true,
            )
        )

        shareLocationPresenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ShareLocationEvent.RequestPermissions)

            // Wait for dialog to be dismissed
            awaitItem()

            assertThat(fakePermissionsPresenter.events.last()).isEqualTo(PermissionsEvents.RequestPermissions)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OpenAppSettings event opens settings and clears dialog`() = runTest {
        val shareLocationPresenter = createShareLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )

        shareLocationPresenter.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink(ShareLocationEvent.OpenAppSettings)
            val settingsOpenedState = awaitItem()

            assertThat(settingsOpenedState.dialogState).isEqualTo(ShareLocationState.Dialog.None)
            assertThat(fakeLocationActions.openSettingsInvocationsCount).isEqualTo(1)
        }
    }

    @Test
    fun `OpenLocationSettings event opens location settings and clears dialog`() = runTest {
        val locationActions = FakeLocationActions(isLocationEnabled = false)
        val shareLocationPresenter = createShareLocationPresenter(locationActions = locationActions)
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.AllGranted,
                shouldShowRationale = false,
            )
        )

        shareLocationPresenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.dialogState).isEqualTo(
                ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.LocationServiceDisabled)
            )

            initialState.eventSink(ShareLocationEvent.OpenLocationSettings)
            val settingsOpenedState = awaitItem()

            assertThat(settingsOpenedState.dialogState).isEqualTo(ShareLocationState.Dialog.None)
            assertThat(locationActions.openLocationSettingsInvocationsCount).isEqualTo(1)
        }
    }

    @Test
    fun `ShowLiveLocationDurationPicker shows duration dialog when constraints pass`() = runTest {
        val shareLocationPresenter = createShareLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.AllGranted,
                shouldShowRationale = false,
            )
        )

        shareLocationPresenter.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink(ShareLocationEvent.ShowLiveLocationDurationPicker)
            val durationDialogState = awaitItem()

            assertThat(durationDialogState.dialogState).isInstanceOf(ShareLocationState.Dialog.LiveLocationDurations::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ShowLiveLocationDurationPicker shows constraint dialog when permissions denied`() = runTest {
        val shareLocationPresenter = createShareLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )

        shareLocationPresenter.test {
            skipItems(1)
            val initialState = awaitItem()
            // Dismiss initial dialog
            initialState.eventSink(ShareLocationEvent.DismissDialog)
            val dismissedState = awaitItem()

            dismissedState.eventSink(ShareLocationEvent.ShowLiveLocationDurationPicker)
            val constraintDialogState = awaitItem()

            assertThat(constraintDialogState.dialogState).isEqualTo(
                ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionDenied)
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ShareStaticLocation sends user location`() = runTest {
        val sendLocationResult = lambdaRecorder { _: String, _: String, _: String?, _: Int?, _: AssetType?, _: EventId? ->
            Result.success(Unit)
        }
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendLocationLambda = sendLocationResult
            },
        )
        val shareLocationPresenter = createShareLocationPresenter(joinedRoom)
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.AllGranted,
                shouldShowRationale = false,
            )
        )

        shareLocationPresenter.test {
            skipItems(1)
            val initialState = awaitItem()

            initialState.eventSink(
                ShareLocationEvent.ShareStaticLocation(
                    location = Location(lat = 3.0, lon = 4.0, accuracy = 5.0f),
                    isPinned = false,
                )
            )

            advanceUntilIdle()

            sendLocationResult.assertions().isCalledOnce()
                .with(
                    value("Location was shared at geo:3.0,4.0;u=5.0"),
                    value("geo:3.0,4.0;u=5.0"),
                    value(null),
                    value(15),
                    value(AssetType.SENDER),
                    value(null),
                )

            assertThat(fakeAnalyticsService.capturedEvents.size).isEqualTo(1)
            assertThat(fakeAnalyticsService.capturedEvents.last()).isEqualTo(
                Composer(
                    inThread = false,
                    isEditing = false,
                    isReply = false,
                    messageType = Composer.MessageType.LocationUser,
                )
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ShareStaticLocation sends pinned location`() = runTest {
        val sendLocationResult = lambdaRecorder { _: String, _: String, _: String?, _: Int?, _: AssetType?, _: EventId? ->
            Result.success(Unit)
        }
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendLocationLambda = sendLocationResult
            },
        )
        val shareLocationPresenter = createShareLocationPresenter(joinedRoom)
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.AllGranted,
                shouldShowRationale = false,
            )
        )

        shareLocationPresenter.test {
            val initialState = awaitItem()

            initialState.eventSink(
                ShareLocationEvent.ShareStaticLocation(
                    location = Location(lat = 1.0, lon = 2.0, accuracy = 3.0f),
                    isPinned = true,
                )
            )

            advanceUntilIdle()
            sendLocationResult.assertions().isCalledOnce()
                .with(
                    value("Location was shared at geo:1.0,2.0;u=3.0"),
                    value("geo:1.0,2.0;u=3.0"),
                    value(null),
                    value(15),
                    value(AssetType.PIN),
                    value(null),
                )

            assertThat(fakeAnalyticsService.capturedEvents.size).isEqualTo(1)
            assertThat(fakeAnalyticsService.capturedEvents.last()).isEqualTo(
                Composer(
                    inThread = false,
                    isEditing = false,
                    isReply = false,
                    messageType = Composer.MessageType.LocationPin,
                )
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
