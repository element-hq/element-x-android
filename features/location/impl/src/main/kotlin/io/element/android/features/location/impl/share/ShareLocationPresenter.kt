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
import androidx.compose.runtime.collectAsState
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
import io.element.android.features.location.impl.common.LocationConstraintsCheck
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.features.location.impl.common.actions.LocationActions
import io.element.android.features.location.impl.common.checkLocationConstraints
import io.element.android.features.location.impl.common.permissions.PermissionsEvents
import io.element.android.features.location.impl.common.permissions.PermissionsPresenter
import io.element.android.features.location.impl.common.permissions.PermissionsState
import io.element.android.features.location.impl.common.toDialogState
import io.element.android.features.location.impl.share.ShareLocationState.Dialog.Constraints
import io.element.android.features.messages.api.MessageComposerContext
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.dateformatter.api.DurationFormatter
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val LIVE_LOCATION_DURATIONS = listOf(15.minutes, 1.hours, 8.hours)

@AssistedInject
class ShareLocationPresenter(
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val room: JoinedRoom,
    @Assisted private val timelineMode: Timeline.Mode,
    private val analyticsService: AnalyticsService,
    private val messageComposerContext: MessageComposerContext,
    private val locationActions: LocationActions,
    private val buildMeta: BuildMeta,
    private val featureFlagService: FeatureFlagService,
    private val client: MatrixClient,
    private val durationFormatter: DurationFormatter,
) : Presenter<ShareLocationState> {
    @AssistedFactory
    fun interface Factory {
        fun create(timelineMode: Timeline.Mode): ShareLocationPresenter
    }

    private val permissionsPresenter = permissionsPresenterFactory.create(MapDefaults.permissions)

    @Composable
    override fun present(): ShareLocationState {
        val permissionsState: PermissionsState = permissionsPresenter.present()
        var trackUserPosition: Boolean by remember { mutableStateOf(permissionsState.isAnyGranted && locationActions.isLocationEnabled()) }
        val isLiveLocationSharingEnabled by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.LiveLocationSharing)
        }.collectAsState(false)
        val appName by remember { derivedStateOf { buildMeta.applicationName } }
        var dialogState: ShareLocationState.Dialog by remember {
            mutableStateOf(ShareLocationState.Dialog.None)
        }
        val currentUser by client.userProfile.collectAsState()
        val scope = rememberCoroutineScope()

        fun checkLocationConstraints() {
            val locationConstraints = checkLocationConstraints(permissionsState, locationActions)
            dialogState = Constraints(locationConstraints.toDialogState())
            trackUserPosition = locationConstraints is LocationConstraintsCheck.Success
        }

        LaunchedEffect(permissionsState.permissions) { checkLocationConstraints() }

        fun handleEvent(event: ShareLocationEvent) {
            when (event) {
                is ShareLocationEvent.ShareStaticLocation -> scope.launch {
                    shareStaticLocation(event)
                }
                ShareLocationEvent.StartTrackingUserLocation -> checkLocationConstraints()
                ShareLocationEvent.StopTrackingUserLocation -> trackUserPosition = false
                ShareLocationEvent.DismissDialog -> dialogState = ShareLocationState.Dialog.None
                ShareLocationEvent.OpenAppSettings -> {
                    locationActions.openAppSettings()
                    dialogState = ShareLocationState.Dialog.None
                }
                ShareLocationEvent.OpenLocationSettings -> {
                    locationActions.openLocationSettings()
                    dialogState = ShareLocationState.Dialog.None
                }
                ShareLocationEvent.ShowLiveLocationDurationPicker -> {
                    val constraintsResult = checkLocationConstraints(permissionsState, locationActions)
                    dialogState = if (constraintsResult is LocationConstraintsCheck.Success) {
                        val durations = LIVE_LOCATION_DURATIONS.map {
                            LiveLocationDuration(duration = it, formatted = durationFormatter.format(it))
                        }
                        ShareLocationState.Dialog.LiveLocationDurations(durations.toImmutableList())
                    } else {
                        Constraints(constraintsResult.toDialogState())
                    }
                }
                is ShareLocationEvent.StartLiveLocationShare -> scope.launch {
                    dialogState = ShareLocationState.Dialog.None
                    // room.startLiveLocationShare(event.duration.inWholeMilliseconds)
                }
                ShareLocationEvent.RequestPermissions -> {
                    dialogState = ShareLocationState.Dialog.None
                    permissionsState.eventSink(PermissionsEvents.RequestPermissions)
                }
            }
        }

        return ShareLocationState(
            currentUser = currentUser,
            dialogState = dialogState,
            trackUserLocation = trackUserPosition,
            hasLocationPermission = permissionsState.isAnyGranted,
            canShareLiveLocation = isLiveLocationSharingEnabled,
            appName = appName,
            eventSink = ::handleEvent,
        )
    }

    private suspend fun shareStaticLocation(event: ShareLocationEvent.ShareStaticLocation) {
        val replyMode = messageComposerContext.composerMode as? MessageComposerMode.Reply
        val inReplyToEventId = replyMode?.eventId
        val geoUri = event.location.toGeoUri()
        getTimeline().flatMap {
            it.sendLocation(
                body = generateBody(geoUri),
                geoUri = geoUri,
                description = null,
                zoomLevel = MapDefaults.DEFAULT_ZOOM.toInt(),
                assetType = if (event.isPinned) AssetType.PIN else AssetType.SENDER,
                inReplyToEventId = inReplyToEventId,
            )
        }
        analyticsService.capture(
            Composer(
                inThread = messageComposerContext.composerMode.inThread,
                isEditing = messageComposerContext.composerMode.isEditing,
                isReply = messageComposerContext.composerMode.isReply,
                messageType = if (event.isPinned) Composer.MessageType.LocationPin else Composer.MessageType.LocationUser
            )
        )
    }

    private suspend fun getTimeline(): Result<Timeline> {
        return when (timelineMode) {
            is Timeline.Mode.Thread -> room.createTimeline(CreateTimelineParams.Threaded(timelineMode.threadRootId))
            else -> Result.success(room.liveTimeline)
        }
    }
}

private fun generateBody(uri: String): String = "Location was shared at $uri"
