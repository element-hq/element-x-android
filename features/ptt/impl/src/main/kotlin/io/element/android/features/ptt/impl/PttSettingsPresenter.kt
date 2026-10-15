/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.zacsweers.metro.Inject
import io.element.android.features.ptt.api.PttInputSourceFactory
import io.element.android.features.ptt.api.PttInputSourceId
import io.element.android.features.ptt.api.PttSessionManager
import io.element.android.features.ptt.impl.input.PttInputCoordinator
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.annotations.ApplicationContext

@Inject
class PttSettingsPresenter(
    @ApplicationContext private val context: Context,
    private val settings: PttLocalSettings,
    private val sessionManager: PttSessionManager,
    private val inputCoordinator: PttInputCoordinator,
    private val inputSourceFactories: @JvmSuppressWildcards Map<PttInputSourceId, PttInputSourceFactory>,
) : Presenter<PttSettingsState> {
    @Composable
    override fun present(): PttSettingsState {
        val covert by sessionManager.isCovert.collectAsState()
        val overlayPreference by settings.overlayEnabled.collectAsState()
        var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
        val lockScreenEnabled by settings.lockScreenEnabled.collectAsState()
        var fullScreenGranted by remember { mutableStateOf(NotificationManagerCompat.from(context).canUseFullScreenIntent()) }
        var batteryExempt by remember { mutableStateOf(isBatteryExempt()) }
        fun refresh() {
            overlayGranted = Settings.canDrawOverlays(context)
            fullScreenGranted = NotificationManagerCompat.from(context).canUseFullScreenIntent()
            batteryExempt = isBatteryExempt()
            inputCoordinator.refresh()
        }
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh() }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
        return PttSettingsState(
            isCovert = covert,
            overlayEnabled = overlayPreference && overlayGranted,
            lockScreenEnabled = lockScreenEnabled && fullScreenGranted,
            isIgnoringBatteryOptimizations = batteryExempt,
            bluetoothSupported = PttInputSourceId.BleButton in inputSourceFactories,
            eventSink = { event ->
                when (event) {
                    is PttSettingsEvent.SetCovert -> sessionManager.setCovert(event.enabled)
                    is PttSettingsEvent.SetOverlay -> {
                        settings.setOverlayEnabled(event.enabled)
                        if (event.enabled && !overlayGranted) openSettings(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, true)
                    }
                    is PttSettingsEvent.SetLockScreen -> {
                        settings.setLockScreenEnabled(event.enabled)
                        if (event.enabled && !fullScreenGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            openSettings(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, true)
                        }
                    }
                    PttSettingsEvent.ToggleBatteryExemption -> toggleBatteryExemption(batteryExempt)
                    PttSettingsEvent.RefreshPermissions -> refresh()
                    PttSettingsEvent.OpenAppSettings -> openSettings(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, true)
                }
            },
        )
    }

    private fun isBatteryExempt() = context.getSystemService(PowerManager::class.java)?.isIgnoringBatteryOptimizations(context.packageName) == true

    @SuppressLint("BatteryLife")
    private fun toggleBatteryExemption(exempt: Boolean) {
        openSettings(
            if (exempt) Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS else Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            forPackage = !exempt,
        )
    }

    private fun openSettings(action: String, forPackage: Boolean = false) {
        context.startActivity(Intent(action).apply {
            if (forPackage) data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
