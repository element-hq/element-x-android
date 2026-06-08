/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.lockscreen.api.DeviceUnlockPrompt
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.di.SessionScope
import timber.log.Timber

@ContributesNode(SessionScope::class)
@AssistedInject
class LinkNewDeviceRootNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: LinkNewDeviceRootPresenter,
    private val deviceUnlockPrompt: DeviceUnlockPrompt,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onDone()
        fun linkDesktopDevice()
        fun linkMobileDevice()
        fun onUnlockApplication(type: LinkDeviceType)
    }

    private val callback: Callback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()

        var linkDeviceType by remember { mutableStateOf<LinkDeviceType?>(null) }
        if (linkDeviceType != null) {
            deviceUnlockPrompt.ShowPrompt()
        }

        deviceUnlockPrompt.OnUnlockEffect { result ->
            if (result) {
                when (linkDeviceType) {
                    LinkDeviceType.Mobile -> callback.linkMobileDevice()
                    LinkDeviceType.Desktop -> callback.linkDesktopDevice()
                    null -> Timber.w("Unlock callback invoked but no device type has been set")
                }
            }
            linkDeviceType = null
        }

        LinkNewDeviceRootView(
            state = state,
            modifier = modifier,
            onBackClick = callback::onDone,
            onLinkDesktopDeviceClick = callback::linkDesktopDevice,
            onLinkMobileDeviceClick = callback::linkMobileDevice,
            onUnlockApplication = callback::onUnlockApplication,
            onUnlockDevice = { linkDeviceType = it },
        )
    }
}
