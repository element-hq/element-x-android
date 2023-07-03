/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");0.0
 * you0.0 may not0.0 use this file except in compliance with the License.
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

package io.element.android.features.location.impl.send

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import im.vector.app.features.analytics.plan.Composer
import io.element.android.features.analytics.test.FakeAnalyticsService
import io.element.android.features.location.api.Location
import io.element.android.features.location.impl.permissions.PermissionsEvents
import io.element.android.features.location.impl.permissions.PermissionsPresenter
import io.element.android.features.location.impl.permissions.PermissionsPresenterFake
import io.element.android.features.location.impl.permissions.PermissionsState
import io.element.android.features.messages.test.MessageComposerContextFake
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.SendLocationInvocation
import io.element.android.libraries.textcomposer.MessageComposerMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SendLocationPresenterTest {

    val permissionsPresenterFake = PermissionsPresenterFake()
    val fakeMatrixRoom = FakeMatrixRoom()
    val fakeAnalyticsService = FakeAnalyticsService()
    val messageComposerContextFake = MessageComposerContextFake()
    val sendLocationPresenter: SendLocationPresenter = SendLocationPresenter(
        permissionsPresenterFactory = object : PermissionsPresenter.Factory {
            override fun create(permissions: List<String>): PermissionsPresenter = permissionsPresenterFake
        },
        room = fakeMatrixRoom,
        analyticsService = fakeAnalyticsService,
        messageComposerContext = messageComposerContextFake,
    )

    @Test
    fun `initial state with permissions granted`() = runTest {
        permissionsPresenterFake.givenState(
            PermissionsState(
                permissions = PermissionsState.Permissions.AllGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionClock.Immediate) {
            sendLocationPresenter.present()
        }.test {

            val initialState = awaitItem()
            Truth.assertThat(initialState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            Truth.assertThat(initialState.mode).isEqualTo(SendLocationState.Mode.SenderLocation)
            Truth.assertThat(initialState.hasLocationPermission).isEqualTo(true)

            // Swipe the map to switch mode
            initialState.eventSink(SendLocationEvents.SwitchToPinLocationMode)
            val myLocationState = awaitItem()
            Truth.assertThat(myLocationState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            Truth.assertThat(myLocationState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            Truth.assertThat(myLocationState.hasLocationPermission).isEqualTo(true)
        }
    }

    @Test
    fun `initial state with permissions denied`() = runTest {
        permissionsPresenterFake.givenState(
            PermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionClock.Immediate) {
            sendLocationPresenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            Truth.assertThat(initialState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            Truth.assertThat(initialState.hasLocationPermission).isEqualTo(false)

            // Click on the button to switch mode
            initialState.eventSink(SendLocationEvents.SwitchToMyLocationMode)
            val myLocationState = awaitItem()
            Truth.assertThat(myLocationState.permissionDialog).isEqualTo(SendLocationState.Dialog.PermissionDenied)
            Truth.assertThat(myLocationState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            Truth.assertThat(myLocationState.hasLocationPermission).isEqualTo(false)
        }
    }

    @Test
    fun `initial state with permissions denied once`() = runTest {
        permissionsPresenterFake.givenState(
            PermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = true,
            )
        )

        moleculeFlow(RecompositionClock.Immediate) {
            sendLocationPresenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            Truth.assertThat(initialState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            Truth.assertThat(initialState.hasLocationPermission).isEqualTo(false)

            // Click on the button to switch mode
            initialState.eventSink(SendLocationEvents.SwitchToMyLocationMode)
            val myLocationState = awaitItem()
            Truth.assertThat(myLocationState.permissionDialog).isEqualTo(SendLocationState.Dialog.PermissionRationale)
            Truth.assertThat(myLocationState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            Truth.assertThat(myLocationState.hasLocationPermission).isEqualTo(false)
        }
    }

    @Test
    fun `rationale dialog dismiss`() = runTest {
        permissionsPresenterFake.givenState(
            PermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = true,
            )
        )

        moleculeFlow(RecompositionClock.Immediate) {
            sendLocationPresenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            // Click on the button to switch mode
            initialState.eventSink(SendLocationEvents.SwitchToMyLocationMode)
            val myLocationState = awaitItem()
            Truth.assertThat(myLocationState.permissionDialog).isEqualTo(SendLocationState.Dialog.PermissionRationale)
            Truth.assertThat(myLocationState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            Truth.assertThat(myLocationState.hasLocationPermission).isEqualTo(false)

            // Dismiss the dialog
            myLocationState.eventSink(SendLocationEvents.DismissDialog)
            val dialogDismissedState = awaitItem()
            Truth.assertThat(dialogDismissedState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            Truth.assertThat(dialogDismissedState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            Truth.assertThat(dialogDismissedState.hasLocationPermission).isEqualTo(false)
        }
    }

    @Test
    fun `rationale dialog continue`() = runTest {
        permissionsPresenterFake.givenState(
            PermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = true,
            )
        )

        moleculeFlow(RecompositionClock.Immediate) {
            sendLocationPresenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            // Click on the button to switch mode
            initialState.eventSink(SendLocationEvents.SwitchToMyLocationMode)
            val myLocationState = awaitItem()
            Truth.assertThat(myLocationState.permissionDialog).isEqualTo(SendLocationState.Dialog.PermissionRationale)
            Truth.assertThat(myLocationState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            Truth.assertThat(myLocationState.hasLocationPermission).isEqualTo(false)

            // Continue the dialog sends permission request to the permissions presenter
            myLocationState.eventSink(SendLocationEvents.RequestPermissions)
            Truth.assertThat(permissionsPresenterFake.events.last()).isEqualTo(PermissionsEvents.RequestPermissions)
        }
    }

    @Test
    fun `permission denied dialog dismiss`() = runTest {
        permissionsPresenterFake.givenState(
            PermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionClock.Immediate) {
            sendLocationPresenter.present()
        }.test {
            // Skip initial state
            val initialState = awaitItem()

            // Click on the button to switch mode
            initialState.eventSink(SendLocationEvents.SwitchToMyLocationMode)
            val myLocationState = awaitItem()
            Truth.assertThat(myLocationState.permissionDialog).isEqualTo(SendLocationState.Dialog.PermissionDenied)
            Truth.assertThat(myLocationState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            Truth.assertThat(myLocationState.hasLocationPermission).isEqualTo(false)

            // Dismiss the dialog
            myLocationState.eventSink(SendLocationEvents.DismissDialog)
            val dialogDismissedState = awaitItem()
            Truth.assertThat(dialogDismissedState.permissionDialog).isEqualTo(SendLocationState.Dialog.None)
            Truth.assertThat(dialogDismissedState.mode).isEqualTo(SendLocationState.Mode.PinLocation)
            Truth.assertThat(dialogDismissedState.hasLocationPermission).isEqualTo(false)
        }
    }

    @Test
    fun `share sender location`() = runTest {
        permissionsPresenterFake.givenState(
            PermissionsState(
                permissions = PermissionsState.Permissions.AllGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionClock.Immediate) {
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

            Truth.assertThat(fakeMatrixRoom.sentLocations.size).isEqualTo(1)
            Truth.assertThat(fakeMatrixRoom.sentLocations.last()).isEqualTo(
                SendLocationInvocation(
                    body = "Location was shared at geo:3.0,4.0;u=5.0",
                    geoUri = "geo:3.0,4.0;u=5.0",
                    description = null,
                    zoomLevel = 15,
                    assetType = AssetType.SENDER
                )
            )

            Truth.assertThat(fakeAnalyticsService.capturedEvents.size).isEqualTo(1)
            Truth.assertThat(fakeAnalyticsService.capturedEvents.last()).isEqualTo(
                Composer(
                    inThread = false,
                    isEditing = false,
                    isLocation = true,
                    isReply = false,
                    locationType = Composer.LocationType.MyLocation,
                )
            )
        }
    }

    @Test
    fun `share pin location`() = runTest {
        permissionsPresenterFake.givenState(
            PermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )

        moleculeFlow(RecompositionClock.Immediate) {
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

            Truth.assertThat(fakeMatrixRoom.sentLocations.size).isEqualTo(1)
            Truth.assertThat(fakeMatrixRoom.sentLocations.last()).isEqualTo(
                SendLocationInvocation(
                    body = "Location was shared at geo:0.0,1.0",
                    geoUri = "geo:0.0,1.0",
                    description = null,
                    zoomLevel = 15,
                    assetType = AssetType.PIN
                )
            )

            Truth.assertThat(fakeAnalyticsService.capturedEvents.size).isEqualTo(1)
            Truth.assertThat(fakeAnalyticsService.capturedEvents.last()).isEqualTo(
                Composer(
                    inThread = false,
                    isEditing = false,
                    isLocation = true,
                    isReply = false,
                    locationType = Composer.LocationType.PinDrop,
                )
            )
        }
    }

    @Test
    fun `composer context passes through analytics`() = runTest {
        permissionsPresenterFake.givenState(
            PermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )
        messageComposerContextFake.apply {
            composerMode = MessageComposerMode.Edit(
                eventId = null, defaultContent = "", transactionId = null
            )
        }

        moleculeFlow(RecompositionClock.Immediate) {
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

            Truth.assertThat(fakeAnalyticsService.capturedEvents.size).isEqualTo(1)
            Truth.assertThat(fakeAnalyticsService.capturedEvents.last()).isEqualTo(
                Composer(
                    inThread = false,
                    isEditing = true,
                    isLocation = true,
                    isReply = false,
                    locationType = Composer.LocationType.PinDrop,
                )
            )
        }
    }
}
