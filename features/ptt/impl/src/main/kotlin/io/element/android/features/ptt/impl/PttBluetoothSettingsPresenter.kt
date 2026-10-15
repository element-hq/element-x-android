/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.ptt.api.PttBluetoothController
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.annotations.ApplicationContext

@Inject
class PttBluetoothSettingsPresenter(
    @ApplicationContext private val context: Context,
    private val controller: PttBluetoothController,
) : Presenter<PttBluetoothSettingsState> {
    @Composable
    override fun present(): PttBluetoothSettingsState {
        val device by controller.state.collectAsState()
        var pendingFind by rememberSaveable { mutableStateOf(false) }
        val chooser = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode != Activity.RESULT_OK) controller.cancelSelection()
        }
        fun find() = controller.findDevice { chooser.launch(IntentSenderRequest.Builder(it).build()) }
        val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                if (pendingFind) find() else controller.connect()
            } else {
                controller.connect() // Updates the screen with permission recovery guidance.
            }
        }
        val activity = LocalActivity.current
        DisposableEffect(controller, activity) {
            controller.enterSetup()
            onDispose {
                if (activity?.isChangingConfigurations != true) controller.leaveSetup()
            }
        }
        return PttBluetoothSettingsState(device = device) { event ->
            when (event) {
                PttBluetoothSettingsEvent.Find, PttBluetoothSettingsEvent.Connect -> {
                    pendingFind = event == PttBluetoothSettingsEvent.Find
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    } else if (pendingFind) {
                        find()
                    } else {
                        controller.connect()
                    }
                }
                PttBluetoothSettingsEvent.Disconnect -> controller.disconnect()
                PttBluetoothSettingsEvent.Forget -> controller.forget()
                is PttBluetoothSettingsEvent.SetAutoConnect -> controller.setAutoConnect(event.enabled)
                PttBluetoothSettingsEvent.OpenBluetoothSettings -> openSettings(Settings.ACTION_BLUETOOTH_SETTINGS)
                PttBluetoothSettingsEvent.OpenAppSettings -> openSettings(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, forPackage = true)
            }
        }
    }

    private fun openSettings(action: String, forPackage: Boolean = false) {
        context.startActivity(Intent(action).apply {
            if (forPackage) data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
