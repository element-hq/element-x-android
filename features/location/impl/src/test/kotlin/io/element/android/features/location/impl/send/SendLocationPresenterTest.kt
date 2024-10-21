/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.impl.send

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
import io.element.android.features.location.impl.common.permissions.PermissionsPresenter
import io.element.android.features.location.impl.common.permissions.PermissionsState
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SendLocationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val fakePermissionsPresenter = FakePermissionsPresenter()
    private val fakeAnalyticsService = FakeAnalyticsService()
    private val fakeMessageComposerContext = FakeMessageComposerContext()
    private val fakeLocationActions = FakeLocationActions()
    private val fakeBuildMeta = aBuildMeta(applicationName = "app name")

    private fun createSendLocationPresenter(
        matrixRoom: MatrixRoom = FakeMatrixRoom(),
    ): SendLocationPresenter = SendLocationPresenter(
        permissionsPresenterFactory = object : PermissionsPresenter.Factory {
            override fun create(permissions: List<String>): PermissionsPresenter = fakePermissionsPresenter
        },
        room = matrixRoom,
        analyticsService = fakeAnalyticsService,
        messageComposerContext = fakeMessageComposerContext,
        locationActions = fakeLocationActions,
        buildMeta = fakeBuildMeta,
    )

    @Test
    fun `initial state with permissions granted`() = runTest {
        val sendLocationPresenter = createSendLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.AllGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            sendLocationPresenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            assertThat(initialState.mode).isEqualTo(SendLocationState.Mode.SenderLocation)
            assertThat(initialState.hasLocationPermission).isTrue()

            // Swipe the map to switch mode
            initialState.eventSink(SendLocationEvents.SwitchToPinLocationMode)
            val myLocationState = awaitItem()
            assertThat(myLocationState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            assertThat(myLocationState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            assertThat(myLocationState.hasLocationPermission).isTrue()
        }
    }

    @Test
    fun `initial state with permissions partially granted`() = runTest {
        val sendLocationPresenter = createSendLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.SomeGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            sendLocationPresenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            assertThat(initialState.mode).isEqualTo(SendLocationState.Mode.SenderLocation)
            assertThat(initialState.hasLocationPermission).isTrue()

            // Swipe the map to switch mode
            initialState.eventSink(SendLocationEvents.SwitchToPinLocationMode)
            val myLocationState = awaitItem()
            assertThat(myLocationState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            assertThat(myLocationState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            assertThat(myLocationState.hasLocationPermission).isTrue()
        }
    }

    @Test
    fun `initial state with permissions denied`() = runTest {
        val sendLocationPresenter = createSendLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            sendLocationPresenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            assertThat(initialState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            assertThat(initialState.hasLocationPermission).isFalse()

            // Click on the button to switch mode
            initialState.eventSink(SendLocationEvents.SwitchToMyLocationMode)
            val myLocationState = awaitItem()
            assertThat(myLocationState.permissionDialog).isEqualTo(SendLocationState.Dialog.PermissionDenied)
            assertThat(myLocationState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            assertThat(myLocationState.hasLocationPermission).isFalse()
        }
    }

    @Test
    fun `initial state with permissions denied once`() = runTest {
        val sendLocationPresenter = createSendLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = true,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            sendLocationPresenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            assertThat(initialState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            assertThat(initialState.hasLocationPermission).isFalse()

            // Click on the button to switch mode
            initialState.eventSink(SendLocationEvents.SwitchToMyLocationMode)
            val myLocationState = awaitItem()
            assertThat(myLocationState.permissionDialog).isEqualTo(SendLocationState.Dialog.PermissionRationale)
            assertThat(myLocationState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            assertThat(myLocationState.hasLocationPermission).isFalse()
        }
    }

    @Test
    fun `rationale dialog dismiss`() = runTest {
        val sendLocationPresenter = createSendLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = true,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            sendLocationPresenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            // Click on the button to switch mode
            initialState.eventSink(SendLocationEvents.SwitchToMyLocationMode)
            val myLocationState = awaitItem()
            assertThat(myLocationState.permissionDialog).isEqualTo(SendLocationState.Dialog.PermissionRationale)
            assertThat(myLocationState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            assertThat(myLocationState.hasLocationPermission).isFalse()

            // Dismiss the dialog
            myLocationState.eventSink(SendLocationEvents.DismissDialog)
            val dialogDismissedState = awaitItem()
            assertThat(dialogDismissedState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            assertThat(dialogDismissedState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            assertThat(dialogDismissedState.hasLocationPermission).isFalse()
        }
    }

    @Test
    fun `rationale dialog continue`() = runTest {
        val sendLocationPresenter = createSendLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = true,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            sendLocationPresenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            // Click on the button to switch mode
            initialState.eventSink(SendLocationEvents.SwitchToMyLocationMode)
            val myLocationState = awaitItem()
            assertThat(myLocationState.permissionDialog).isEqualTo(SendLocationState.Dialog.PermissionRationale)
            assertThat(myLocationState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            assertThat(myLocationState.hasLocationPermission).isFalse()

            // Continue the dialog sends permission request to the permissions presenter
            myLocationState.eventSink(SendLocationEvents.RequestPermissions)
            assertThat(fakePermissionsPresenter.events.last()).isEqualTo(PermissionsEvents.RequestPermissions)
        }
    }

    @Test
    fun `permission denied dialog dismiss`() = runTest {
        val sendLocationPresenter = createSendLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            sendLocationPresenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            // Click on the button to switch mode
            initialState.eventSink(SendLocationEvents.SwitchToMyLocationMode)
            val myLocationState = awaitItem()
            assertThat(myLocationState.permissionDialog).isEqualTo(SendLocationState.Dialog.PermissionDenied)
            assertThat(myLocationState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            assertThat(myLocationState.hasLocationPermission).isFalse()

            // Dismiss the dialog
            myLocationState.eventSink(SendLocationEvents.DismissDialog)
            val dialogDismissedState = awaitItem()
            assertThat(dialogDismissedState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            assertThat(dialogDismissedState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            assertThat(dialogDismissedState.hasLocationPermission).isFalse()
        }
    }

    @Test
    fun `share sender location`() = runTest {
        val sendLocationResult = lambdaRecorder<String, String, String?, Int?, AssetType?, Result<Unit>> { _, _, _, _, _ ->
            Result.success(Unit)
        }
        val matrixRoom = FakeMatrixRoom(
            sendLocationResult = sendLocationResult,
        )
        val sendLocationPresenter = createSendLocationPresenter(matrixRoom)
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.AllGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            sendLocationPresenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            // Send location
            initialState.eventSink(
                SendLocationEvents.SendLocation(
                    cameraPosition = SendLocationEvents.SendLocation.CameraPosition(
                        lat = 0.0,
                        lon = 1.0,
                        zoom = 2.0,
                    ),
                    location = Location(
                        lat = 3.0,
                        lon = 4.0,
                        accuracy = 5.0f,
                    )
                )
            )

            delay(1) // Wait for the coroutine to finish

            sendLocationResult.assertions().isCalledOnce()
                .with(
                    value("Location was shared at geo:3.0,4.0;u=5.0"),
                    value("geo:3.0,4.0;u=5.0"),
                    value(null),
                    value(15),
                    value(AssetType.SENDER),
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
        }
    }

    @Test
    fun `share pin location`() = runTest {
        val sendLocationResult = lambdaRecorder<String, String, String?, Int?, AssetType?, Result<Unit>> { _, _, _, _, _ ->
            Result.success(Unit)
        }
        val matrixRoom = FakeMatrixRoom(
            sendLocationResult = sendLocationResult,
        )
        val sendLocationPresenter = createSendLocationPresenter(matrixRoom)
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionMode.Immediate) {
            sendLocationPresenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            // Send location
            initialState.eventSink(
                SendLocationEvents.SendLocation(
                    cameraPosition = SendLocationEvents.SendLocation.CameraPosition(
                        lat = 0.0,
                        lon = 1.0,
                        zoom = 2.0,
                    ),
                    location = Location(
                        lat = 3.0,
                        lon = 4.0,
                        accuracy = 5.0f,
                    )
                )
            )

            delay(1) // Wait for the coroutine to finish

            sendLocationResult.assertions().isCalledOnce()
                .with(
                    value("Location was shared at geo:0.0,1.0"),
                    value("geo:0.0,1.0"),
                    value(null),
                    value(15),
                    value(AssetType.PIN),
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
        }
    }

    @Test
    fun `composer context passes through analytics`() = runTest {
        val sendLocationResult = lambdaRecorder<String, String, String?, Int?, AssetType?, Result<Unit>> { _, _, _, _, _ ->
            Result.success(Unit)
        }
        val matrixRoom = FakeMatrixRoom(
            sendLocationResult = sendLocationResult,
        )
        val sendLocationPresenter = createSendLocationPresenter(matrixRoom)
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )
        fakeMessageComposerContext.apply {
            composerMode = MessageComposerMode.Edit(
                eventOrTransactionId = AN_EVENT_ID.toEventOrTransactionId(),
                content = ""
            )
        }

        moleculeFlow(RecompositionMode.Immediate) {
            sendLocationPresenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            // Send location
            initialState.eventSink(
                SendLocationEvents.SendLocation(
                    cameraPosition = SendLocationEvents.SendLocation.CameraPosition(
                        lat = 0.0,
                        lon = 1.0,
                        zoom = 2.0,
                    ),
                    location = null
                )
            )

            delay(1) // Wait for the coroutine to finish

            assertThat(fakeAnalyticsService.capturedEvents.size).isEqualTo(1)
            assertThat(fakeAnalyticsService.capturedEvents.last()).isEqualTo(
                Composer(
                    inThread = false,
                    isEditing = true,
                    isReply = false,
                    messageType = Composer.MessageType.LocationPin,
                )
            )
        }
    }

    @Test
    fun `open settings activity`() = runTest {
        val sendLocationPresenter = createSendLocationPresenter()
        fakePermissionsPresenter.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )
        fakeMessageComposerContext.apply {
            composerMode = MessageComposerMode.Edit(
                eventOrTransactionId = AN_EVENT_ID.toEventOrTransactionId(),
                content = ""
            )
        }

        moleculeFlow(RecompositionMode.Immediate) {
            sendLocationPresenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            initialState.eventSink(SendLocationEvents.SwitchToMyLocationMode)
            val dialogShownState = awaitItem()

            // Open settings
            dialogShownState.eventSink(SendLocationEvents.OpenAppSettings)
            val settingsOpenedState = awaitItem()

            assertThat(settingsOpenedState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            assertThat(fakeLocationActions.openSettingsInvocationsCount).isEqualTo(1)
        }
    }

    @Test
    fun `application name is in state`() = runTest {
        val sendLocationPresenter = createSendLocationPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            sendLocationPresenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.appName).isEqualTo("app name")
        }
    }
}
