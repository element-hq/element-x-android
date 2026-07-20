/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.zacsweers.metro.Inject
import io.element.android.features.ptt.api.PttChannelConfig
import io.element.android.features.ptt.api.PttConnectionState
import io.element.android.features.ptt.api.PttFloorState
import io.element.android.features.ptt.api.PttRoomService
import io.element.android.features.ptt.api.PttTransportType
import io.element.android.features.ptt.impl.services.PttSessionHostService
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.permissions.api.PermissionsEvent
import io.element.android.libraries.permissions.api.PermissionsPresenter
import kotlinx.coroutines.launch

/**
 * Stage 1 PTT prototype presenter.
 *
 * Drives the [PttSessionHostService] (which owns the app-scoped [PttSessionController]) rather than a
 * specific backend: joining requests the microphone permission then starts the foreground session;
 * the talk button takes/releases the half-duplex floor, playing go/deny [PttTones]. State is derived
 * from the transport's live session state.
 */
@Inject
class PttPrototypePresenter(
    @ApplicationContext private val context: Context,
    private val room: JoinedRoom,
    private val pttRoomService: PttRoomService,
    private val pttSessionController: PttSessionController,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
) : Presenter<PttPrototypeState> {
    private val recordAudioPermissionPresenter =
        permissionsPresenterFactory.create(Manifest.permission.RECORD_AUDIO)

    @Composable
    override fun present(): PttPrototypeState {
        val coroutineScope = rememberCoroutineScope()
        val permissionsState = recordAudioPermissionPresenter.present()
        var pendingJoin by remember { mutableStateOf(false) }

        // "Draw over other apps" is a special permission granted via a settings screen — re-check on
        // resume so the UI updates after the user returns from it.
        val lifecycleOwner = LocalLifecycleOwner.current
        var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    canDrawOverlays = Settings.canDrawOverlays(context)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        val isPttEnabled by pttRoomService.isPttEnabledFlow().collectAsState(initial = false)
        val sessionState by pttSessionController.sessionState.collectAsState()

        val hasLiveChannel = sessionState != null
        val isUserInChannel = sessionState?.connection is PttConnectionState.Connected
        val isTransmitting = sessionState?.floor is PttFloorState.HeldByMe
        val participantCount = sessionState?.participantCount ?: 0

        // Once the microphone permission is granted, honour a pending join.
        LaunchedEffect(permissionsState.permissionGranted) {
            if (permissionsState.permissionGranted && pendingJoin) {
                pendingJoin = false
                startSession()
            }
        }

        fun handleEvent(event: PttPrototypeEvent) {
            when (event) {
                PttPrototypeEvent.JoinPttChannel -> {
                    if (permissionsState.permissionGranted) {
                        startSession()
                    } else {
                        pendingJoin = true
                        permissionsState.eventSink(PermissionsEvent.RequestPermissions)
                    }
                }
                PttPrototypeEvent.LeavePttChannel -> PttSessionHostService.hangup(context)
                PttPrototypeEvent.StartTransmitting -> pttSessionController.pressToTalk()
                PttPrototypeEvent.StopTransmitting -> pttSessionController.releaseToTalk()
                is PttPrototypeEvent.SetPttEnabled -> coroutineScope.launch {
                    pttRoomService.setPttEnabled(event.enabled)
                }
                PttPrototypeEvent.GrantOverlayPermission -> {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}"),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }
        }

        return PttPrototypeState(
            isPttAvailable = true,
            isPttEnabled = isPttEnabled,
            hasLiveChannel = hasLiveChannel,
            participantCount = participantCount,
            isUserInChannel = isUserInChannel,
            isTransmitting = isTransmitting,
            permissionsState = permissionsState,
            canDrawOverlays = canDrawOverlays,
            eventSink = ::handleEvent,
        )
    }

    private fun startSession() {
        PttSessionHostService.start(context, interimMumbleConfig(room.sessionId, room.roomId))
    }
}

/**
 * INTERIM channel config until task #8 resolves it from the room's `io.element.ptt.config` state
 * event. `10.0.2.2` is the Android emulator's alias for the host loopback — i.e. the local Murmur.
 */
private fun interimMumbleConfig(sessionId: SessionId, roomId: RoomId) = PttChannelConfig(
    sessionId = sessionId,
    roomId = roomId,
    transport = PttTransportType.Mumble,
    transportMetadata = mapOf(
        "host" to "10.0.2.2",
        "port" to "64738",
        "username" to sessionId.value.substringAfter("@").substringBefore(":"),
    ),
)
