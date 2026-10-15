/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class PttSettingsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: PttSettingsPresenter,
    private val bluetoothPresenter: PttBluetoothSettingsPresenter,
) : Node(buildContext, plugins = plugins) {
    @Composable
    override fun View(modifier: Modifier) {
        var showBluetoothSettings by rememberSaveable { mutableStateOf(false) }
        BackHandler(enabled = showBluetoothSettings) {
            showBluetoothSettings = false
        }
        if (showBluetoothSettings) {
            PttBluetoothSettingsView(
                state = bluetoothPresenter.present(),
                onBackClick = { showBluetoothSettings = false },
                modifier = modifier,
            )
        } else {
            PttSettingsView(
                state = presenter.present(),
                onBluetoothSettingsClick = { showBluetoothSettings = true },
                onBackClick = ::navigateUp,
                modifier = modifier,
            )
        }
    }
}
