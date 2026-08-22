/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
// Modified by Feral: route "No distributors available" to the private notifications setup flow.

package io.element.android.appnav.loggedin

import androidx.compose.runtime.Composable
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
class LoggedInNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val loggedInPresenter: LoggedInPresenter,
) : Node(
    buildContext = buildContext,
    plugins = plugins
) {
    interface Callback : Plugin {
        fun navigateToNotificationTroubleshoot()
        /**
         * Tries the built-in Feral provider first (silent), then the ntfy flow.
         * Returns false when neither could handle it (caller falls back to the upstream dialog).
         */
        suspend fun navigateToPrivatePushSetup(): Boolean
    }

    private val callback: Callback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        val loggedInState = loggedInPresenter.present()
        LoggedInView(
            state = loggedInState,
            navigateToNotificationTroubleshoot = callback::navigateToNotificationTroubleshoot,
            navigateToPrivatePushSetup = callback::navigateToPrivatePushSetup,
            modifier = modifier
        )
    }
}
