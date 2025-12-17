/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class PinUnlockNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: PinUnlockPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onUnlock()
    }

    private val callback: Callback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        LaunchedEffect(state.isUnlocked) {
            if (state.isUnlocked) {
                callback.onUnlock()
            }
        }
        PinUnlockView(
            state = state,
            // UnlockNode is only used for in-app unlock, so we can safely set isInAppUnlock to true.
            // It's set to false in PinUnlockActivity.
            isInAppUnlock = true,
            modifier = modifier
        )
    }
}
