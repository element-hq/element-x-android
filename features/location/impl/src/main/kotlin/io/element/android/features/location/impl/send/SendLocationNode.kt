/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.send

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.di.RoomScope
import io.element.android.services.analytics.api.AnalyticsService

@ContributesNode(RoomScope::class)
class SendLocationNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: SendLocationPresenter,
    analyticsService: AnalyticsService,
) : Node(buildContext, plugins = plugins) {
    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.LocationSend))
            }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        SendLocationView(
            state = presenter.present(),
            modifier = modifier,
            navigateUp = ::navigateUp,
        )
    }
}
