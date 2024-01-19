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

package io.element.android.features.location.impl.send

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.Composer
import io.element.android.features.location.api.Location
import io.element.android.features.location.impl.aPermissionsState
import io.element.android.features.location.impl.common.actions.FakeLocationActions
import io.element.android.features.location.impl.common.permissions.PermissionsEvents
import io.element.android.features.location.impl.common.permissions.PermissionsPresenter
import io.element.android.features.location.impl.common.permissions.PermissionsPresenterFake
import io.element.android.features.location.impl.common.permissions.PermissionsState
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.SendLocationInvocation
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SendLocationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val permissionsPresenterFake = PermissionsPresenterFake()
    private val fakeMatrixRoom = FakeMatrixRoom()
    private val fakeAnalyticsService = FakeAnalyticsService()
    private val fakeMessageComposerContext = FakeMessageComposerContext()
    private val fakeLocationActions = FakeLocationActions()
    private val fakeBuildMeta = aBuildMeta(applicationName = "app name")
    private val sendLocationPresenter: SendLocationPresenter = SendLocationPresenter(
        permissionsPresenterFactory = object : PermissionsPresenter.Factory {
            override fun create(permissions: List<String>): PermissionsPresenter = permissionsPresenterFake
        },
        room = fakeMatrixRoom,
        analyticsService = fakeAnalyticsService,
        messageComposerContext = fakeMessageComposerContext,
        locationActions = fakeLocationActions,
        buildMeta = fakeBuildMeta,
    )

    @Test
    fun `initial state with permissions granted`() = runTest {
        permissionsPresenterFake.givenState(
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
        permissionsPresenterFake.givenState(
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
        permissionsPresenterFake.givenState(
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
        permissionsPresenterFake.givenState(
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
        permissionsPresenterFake.givenState(
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
        permissionsPresenterFake.givenState(
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
            assertThat(permissionsPresenterFake.events.last()).isEqualTo(PermissionsEvents.RequestPermissions)
        }
    }

    @Test
    fun `permission denied dialog dismiss`() = runTest {
        permissionsPresenterFake.givenState(
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
        permissionsPresenterFake.givenState(
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

            assertThat(fakeMatrixRoom.sentLocations.size).isEqualTo(1)
            assertThat(fakeMatrixRoom.sentLocations.last()).isEqualTo(
                SendLocationInvocation(
                    body = "Location was shared at geo:3.0,4.0;u=5.0",
                    geoUri = "geo:3.0,4.0;u=5.0",
                    description = null,
                    zoomLevel = 15,
                    assetType = AssetType.SENDER
                )
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
        permissionsPresenterFake.givenState(
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

            assertThat(fakeMatrixRoom.sentLocations.size).isEqualTo(1)
            assertThat(fakeMatrixRoom.sentLocations.last()).isEqualTo(
                SendLocationInvocation(
                    body = "Location was shared at geo:0.0,1.0",
                    geoUri = "geo:0.0,1.0",
                    description = null,
                    zoomLevel = 15,
                    assetType = AssetType.PIN
                )
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
        permissionsPresenterFake.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )
        fakeMessageComposerContext.apply {
            composerMode = MessageComposerMode.Edit(
                eventId = null,
                defaultContent = "",
                transactionId = null
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
        permissionsPresenterFake.givenState(
            aPermissionsState(
                permissions = PermissionsState.Permissions.NoneGranted,
                shouldShowRationale = false,
            )
        )
        fakeMessageComposerContext.apply {
            composerMode = MessageComposerMode.Edit(
                eventId = null,
                defaultContent = "",
                transactionId = null
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
        moleculeFlow(RecompositionMode.Immediate) {
            sendLocationPresenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.appName).isEqualTo("app name")
        }
    }
}
