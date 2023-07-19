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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import im.vector.app.features.analytics.plan.Composer
import io.element.android.features.location.impl.MapDefaults
import io.element.android.features.location.impl.permissions.PermissionsEvents
import io.element.android.features.location.impl.permissions.PermissionsPresenter
import io.element.android.features.location.impl.permissions.PermissionsState
import io.element.android.features.location.impl.show.LocationActions
import io.element.android.features.messages.api.MessageComposerContext
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SendLocationPresenter @Inject constructor(
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val room: MatrixRoom,
    private val analyticsService: AnalyticsService,
    private val messageComposerContext: MessageComposerContext,
    private val locationActions: LocationActions,
    private val systemClock: SystemClock,
    private val buildMeta: BuildMeta,
) : Presenter<SendLocationState> {

    private val permissionsPresenter = permissionsPresenterFactory.create(MapDefaults.permissions)

    @Composable
    override fun present(): SendLocationState {
        val permissionsState: PermissionsState = permissionsPresenter.present()
        var mode: SendLocationState.Mode by remember {
            mutableStateOf(
                if (permissionsState.isAnyGranted) SendLocationState.Mode.SenderLocation
                else SendLocationState.Mode.PinLocation
            )
        }
        val appName by remember { derivedStateOf { buildMeta.applicationName } }
        var permissionDialog: SendLocationState.Dialog by remember {
            mutableStateOf(SendLocationState.Dialog.None)
        }
        val scope = rememberCoroutineScope()

        LaunchedEffect(permissionsState.permissions) {
            if (permissionsState.isAnyGranted) {
                mode = SendLocationState.Mode.SenderLocation
                permissionDialog = SendLocationState.Dialog.None
            }
        }

        fun handleEvents(event: SendLocationEvents) {
            when (event) {
                is SendLocationEvents.SendLocation -> scope.launch {
                    sendLocation(event, mode)
                }
                SendLocationEvents.SwitchToMyLocationMode -> when {
                    permissionsState.isAnyGranted -> mode = SendLocationState.Mode.SenderLocation
                    permissionsState.shouldShowRationale -> permissionDialog = SendLocationState.Dialog.PermissionRationale
                    else -> permissionDialog = SendLocationState.Dialog.PermissionDenied
                }
                SendLocationEvents.SwitchToPinLocationMode -> mode = SendLocationState.Mode.PinLocation
                SendLocationEvents.DismissDialog -> permissionDialog = SendLocationState.Dialog.None
                SendLocationEvents.OpenAppSettings -> {
                    locationActions.openSettings()
                    permissionDialog = SendLocationState.Dialog.None
                }
                SendLocationEvents.RequestPermissions -> permissionsState.eventSink(PermissionsEvents.RequestPermissions)
            }
        }

        return SendLocationState(
            permissionDialog = permissionDialog,
            mode = mode,
            hasLocationPermission = permissionsState.isAnyGranted,
            appName = appName,
            eventSink = ::handleEvents,
        )
    }

    private suspend fun sendLocation(
        event: SendLocationEvents.SendLocation,
        mode: SendLocationState.Mode,
    ) {
        when (mode) {
            SendLocationState.Mode.PinLocation -> {
                val geoUri = event.cameraPosition.toGeoUri()
                room.sendLocation(
                    body = generateBody(geoUri, systemClock.epochMillis()),
                    geoUri = geoUri,
                    description = null,
                    zoomLevel = MapDefaults.DEFAULT_ZOOM.toInt(),
                    assetType = AssetType.PIN
                )
                analyticsService.capture(
                    Composer(
                        inThread = messageComposerContext.composerMode.inThread,
                        isEditing = messageComposerContext.composerMode.isEditing,
                        isLocation = true,
                        isReply = messageComposerContext.composerMode.isReply,
                        locationType = Composer.LocationType.PinDrop,
                    )
                )
            }
            SendLocationState.Mode.SenderLocation -> {
                val geoUri = event.toGeoUri()
                room.sendLocation(
                    body = generateBody(geoUri, systemClock.epochMillis()),
                    geoUri = geoUri,
                    description = null,
                    zoomLevel = MapDefaults.DEFAULT_ZOOM.toInt(),
                    assetType = AssetType.SENDER
                )
                analyticsService.capture(
                    Composer(
                        inThread = messageComposerContext.composerMode.inThread,
                        isEditing = messageComposerContext.composerMode.isEditing,
                        isLocation = true,
                        isReply = messageComposerContext.composerMode.isReply,
                        locationType = Composer.LocationType.MyLocation,
                    )
                )
            }
        }
    }
}

private fun SendLocationEvents.SendLocation.toGeoUri(): String = location?.toGeoUri() ?: cameraPosition.toGeoUri()

private fun SendLocationEvents.SendLocation.CameraPosition.toGeoUri(): String = "geo:$lat,$lon"

private fun generateBody(uri: String, epochMillis: Long): String {
    val timestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
    return "Location was shared at $uri as of $timestamp"
}
