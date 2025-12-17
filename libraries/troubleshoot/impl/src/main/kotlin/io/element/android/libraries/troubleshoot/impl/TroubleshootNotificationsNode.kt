/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.troubleshoot.api.NotificationTroubleShootEntryPoint
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootNavigator
import io.element.android.services.analytics.api.ScreenTracker

@ContributesNode(SessionScope::class)
@AssistedInject
class TroubleshootNotificationsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val screenTracker: ScreenTracker,
    factory: TroubleshootNotificationsPresenter.Factory,
) : Node(buildContext, plugins = plugins),
    NotificationTroubleshootNavigator {
    private val callback: NotificationTroubleShootEntryPoint.Callback = callback()
    private val presenter = factory.create(
        navigator = this,
    )

    override fun navigateToBlockedUsers() {
        callback.navigateToBlockedUsers()
    }

    @Composable
    override fun View(modifier: Modifier) {
        screenTracker.TrackScreen(MobileScreen.ScreenName.NotificationTroubleshoot)
        val state = presenter.present()
        TroubleshootNotificationsView(
            state = state,
            onBackClick = callback::onDone,
            modifier = modifier,
        )
    }
}
