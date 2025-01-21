/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.troubleshoot.api.NotificationTroubleShootEntryPoint
import io.element.android.services.analytics.api.ScreenTracker

@ContributesNode(SessionScope::class)
class TroubleshootNotificationsNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: TroubleshootNotificationsPresenter,
    private val screenTracker: ScreenTracker,
) : Node(buildContext, plugins = plugins) {
    private fun onDone() {
        plugins<NotificationTroubleShootEntryPoint.Callback>().forEach {
            it.onDone()
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        screenTracker.TrackScreen(MobileScreen.ScreenName.NotificationTroubleshoot)
        val state = presenter.present()
        TroubleshootNotificationsView(
            state = state,
            onBackClick = ::onDone,
            modifier = modifier,
        )
    }
}
