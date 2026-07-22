/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
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
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.zacsweers.metro.Inject
import io.element.android.features.ptt.api.PttConnectionState
import io.element.android.features.ptt.api.PttFloorState
import io.element.android.features.ptt.api.PttRoomService
import io.element.android.features.ptt.api.PttSessionManager
import io.element.android.features.ptt.impl.lockscreen.PttLockScreenAlert
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.permissions.api.PermissionsEvent
import io.element.android.libraries.permissions.api.PermissionsPresenter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Long enough to fully lock the device and let the display settle before the alert fires, so the
// full-screen-intent launch isn't racing the lock animation (which can demote it to a heads-up).
private const val LOCK_SCREEN_ALERT_DELAY_MS = 6_000L

/**
 * Stage 1 PTT prototype presenter.
 *
 * Drives the [PttSessionManager] (the transport-agnostic session facade) rather than a specific
 * backend: joining requests the microphone permission then starts the foreground session; the talk
 * button takes/releases the half-duplex floor (with go/deny tones). State is derived from the live
 * session state.
 */
@Inject
class PttPrototypePresenter(
    @ApplicationContext private val context: Context,
    private val room: JoinedRoom,
    private val pttRoomService: PttRoomService,
    private val pttSessionManager: PttSessionManager,
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
        var canUseFullScreenIntent by remember {
            mutableStateOf(NotificationManagerCompat.from(context).canUseFullScreenIntent())
        }
        var isIgnoringBatteryOptimizations by remember { mutableStateOf(isIgnoringBatteryOptimizations()) }
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    canDrawOverlays = Settings.canDrawOverlays(context)
                    canUseFullScreenIntent = NotificationManagerCompat.from(context).canUseFullScreenIntent()
                    isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        val isPttEnabled by pttRoomService.isPttEnabledFlow().collectAsState(initial = false)
        val sessionState by pttSessionManager.sessionState.collectAsState()
        val isHearingEnabled by pttSessionManager.isHearingEnabled.collectAsState()
        val isCovert by pttSessionManager.isCovert.collectAsState()

        val hasLiveChannel = sessionState != null
        val isUserInChannel = sessionState?.connection is PttConnectionState.Connected
        val isTransmitting = sessionState?.floor is PttFloorState.HeldByMe
        val participantCount = sessionState?.participantCount ?: 0

        // Once the microphone permission is granted, honour a pending join.
        LaunchedEffect(permissionsState.permissionGranted) {
            if (permissionsState.permissionGranted && pendingJoin) {
                pendingJoin = false
                pttSessionManager.start(room.sessionId, room.roomId)
            }
        }

        fun handleEvent(event: PttPrototypeEvent) {
            when (event) {
                PttPrototypeEvent.JoinPttChannel -> {
                    if (permissionsState.permissionGranted) {
                        pttSessionManager.start(room.sessionId, room.roomId)
                    } else {
                        pendingJoin = true
                        permissionsState.eventSink(PermissionsEvent.RequestPermissions)
                    }
                }
                PttPrototypeEvent.LeavePttChannel -> pttSessionManager.stop()
                PttPrototypeEvent.StartTransmitting -> pttSessionManager.pressToTalk()
                PttPrototypeEvent.StopTransmitting -> pttSessionManager.releaseToTalk()
                is PttPrototypeEvent.SetPttEnabled -> coroutineScope.launch {
                    pttRoomService.setPttEnabled(event.enabled)
                }
                is PttPrototypeEvent.SetHearingEnabled -> pttSessionManager.setHearingEnabled(event.enabled)
                is PttPrototypeEvent.SetCovertMode -> pttSessionManager.setCovert(event.enabled)
                PttPrototypeEvent.GrantOverlayPermission -> {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}"),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                PttPrototypeEvent.SimulateLockScreenAlert -> coroutineScope.launch {
                    // Delay so the tester can lock the screen before the full-screen alert fires.
                    delay(LOCK_SCREEN_ALERT_DELAY_MS)
                    PttLockScreenAlert.post(context, room.sessionId, room.roomId, room.roomId.value)
                }
                PttPrototypeEvent.GrantFullScreenIntent -> {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                        Uri.parse("package:${context.packageName}"),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                PttPrototypeEvent.ToggleBatteryOptimizationExemption -> {
                    if (isIgnoringBatteryOptimizations) {
                        // No API to re-enable optimization directly — send the user to the list to turn it off.
                        context.startActivity(
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } else {
                        requestIgnoreBatteryOptimizations()
                    }
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
            isHearingEnabled = isHearingEnabled,
            isCovert = isCovert,
            permissionsState = permissionsState,
            canDrawOverlays = canDrawOverlays,
            canUseFullScreenIntent = canUseFullScreenIntent,
            isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
            eventSink = ::handleEvent,
        )
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService(PowerManager::class.java)
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    // Opt-in only, gated behind the user toggle — the direct "ignore battery optimizations" request.
    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimizations() {
        context.startActivity(
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
