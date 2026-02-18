/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.Composer
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.features.location.impl.common.actions.LocationActions
import io.element.android.features.location.impl.common.permissions.PermissionsEvents
import io.element.android.features.location.impl.common.permissions.PermissionsPresenter
import io.element.android.features.location.impl.common.permissions.PermissionsState
import io.element.android.features.messages.api.MessageComposerContext
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.launch

@AssistedInject
class ShareLocationPresenter(
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val room: JoinedRoom,
    @Assisted private val timelineMode: Timeline.Mode,
    private val analyticsService: AnalyticsService,
    private val messageComposerContext: MessageComposerContext,
    private val locationActions: LocationActions,
    private val buildMeta: BuildMeta,
) : Presenter<ShareLocationState> {
    @AssistedFactory
    fun interface Factory {
        fun create(timelineMode: Timeline.Mode): ShareLocationPresenter
    }

    private val permissionsPresenter = permissionsPresenterFactory.create(MapDefaults.permissions)

    @Composable
    override fun present(): ShareLocationState {
        val permissionsState: PermissionsState = permissionsPresenter.present()
        var mode: ShareLocationState.Mode by remember {
            mutableStateOf(
                if (permissionsState.isAnyGranted) {
                    ShareLocationState.Mode.SenderLocation
                } else {
                    ShareLocationState.Mode.PinLocation
                }
            )
        }
        val appName by remember { derivedStateOf { buildMeta.applicationName } }
        var permissionDialog: ShareLocationState.Dialog by remember {
            mutableStateOf(ShareLocationState.Dialog.None)
        }
        val scope = rememberCoroutineScope()

        LaunchedEffect(permissionsState.permissions) {
            if (permissionsState.isAnyGranted) {
                mode = ShareLocationState.Mode.SenderLocation
                permissionDialog = ShareLocationState.Dialog.None
            }
        }

        fun handleEvent(event: ShareLocationEvents) {
            when (event) {
                is ShareLocationEvents.ShareLocation -> scope.launch {
                    shareLocation(event, mode)
                }
                ShareLocationEvents.SwitchToMyLocationMode -> when {
                    permissionsState.isAnyGranted -> mode = ShareLocationState.Mode.SenderLocation
                    permissionsState.shouldShowRationale -> permissionDialog = ShareLocationState.Dialog.PermissionRationale
                    else -> permissionDialog = ShareLocationState.Dialog.PermissionDenied
                }
                ShareLocationEvents.SwitchToPinLocationMode -> mode = ShareLocationState.Mode.PinLocation
                ShareLocationEvents.DismissDialog -> permissionDialog = ShareLocationState.Dialog.None
                ShareLocationEvents.OpenAppSettings -> {
                    locationActions.openSettings()
                    permissionDialog = ShareLocationState.Dialog.None
                }
                ShareLocationEvents.RequestPermissions -> permissionsState.eventSink(PermissionsEvents.RequestPermissions)
            }
        }

        return ShareLocationState(
            permissionDialog = permissionDialog,
            mode = mode,
            hasLocationPermission = permissionsState.isAnyGranted,
            appName = appName,
            eventSink = ::handleEvent,
        )
    }

    private suspend fun shareLocation(
        event: ShareLocationEvents.ShareLocation,
        mode: ShareLocationState.Mode,
    ) {
        val replyMode = messageComposerContext.composerMode as? MessageComposerMode.Reply
        val inReplyToEventId = replyMode?.eventId
        when (mode) {
            ShareLocationState.Mode.PinLocation -> {
                val geoUri = event.cameraPosition.toGeoUri()
                getTimeline().flatMap {
                    it.sendLocation(
                        body = generateBody(geoUri),
                        geoUri = geoUri,
                        description = null,
                        zoomLevel = MapDefaults.DEFAULT_ZOOM.toInt(),
                        assetType = AssetType.PIN,
                        inReplyToEventId = inReplyToEventId,
                    )
                }
                analyticsService.capture(
                    Composer(
                        inThread = messageComposerContext.composerMode.inThread,
                        isEditing = messageComposerContext.composerMode.isEditing,
                        isReply = messageComposerContext.composerMode.isReply,
                        messageType = Composer.MessageType.LocationPin,
                    )
                )
            }
            ShareLocationState.Mode.SenderLocation -> {
                val geoUri = event.toGeoUri()
                getTimeline().flatMap {
                    it.sendLocation(
                        body = generateBody(geoUri),
                        geoUri = geoUri,
                        description = null,
                        zoomLevel = MapDefaults.DEFAULT_ZOOM.toInt(),
                        assetType = AssetType.SENDER,
                        inReplyToEventId = inReplyToEventId,
                    )
                }
                analyticsService.capture(
                    Composer(
                        inThread = messageComposerContext.composerMode.inThread,
                        isEditing = messageComposerContext.composerMode.isEditing,
                        isReply = messageComposerContext.composerMode.isReply,
                        messageType = Composer.MessageType.LocationUser,
                    )
                )
            }
        }
    }

    private suspend fun getTimeline(): Result<Timeline> {
        return when (timelineMode) {
            is Timeline.Mode.Thread -> room.createTimeline(CreateTimelineParams.Threaded(timelineMode.threadRootId))
            else -> Result.success(room.liveTimeline)
        }
    }
}

private fun ShareLocationEvents.ShareLocation.toGeoUri(): String = location?.toGeoUri() ?: cameraPosition.toGeoUri()

private fun ShareLocationEvents.ShareLocation.CameraPosition.toGeoUri(): String = "geo:$lat,$lon"

private fun generateBody(uri: String): String = "Location was shared at $uri"
