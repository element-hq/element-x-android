/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.impl.show

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
import io.element.android.features.location.api.ShowLocationEntryPoint
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.RoomScope
import io.element.android.services.analytics.api.AnalyticsService

@ContributesNode(RoomScope::class)
class ShowLocationNode @AssistedInject constructor(
    presenterFactory: ShowLocationPresenter.Factory,
    analyticsService: AnalyticsService,
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : Node(buildContext, plugins = plugins) {
    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.LocationView))
            }
        )
    }

    private val inputs: ShowLocationEntryPoint.Inputs = inputs()
    private val presenter = presenterFactory.create(inputs.location, inputs.description)

    @Composable
    override fun View(modifier: Modifier) {
        ShowLocationView(
            state = presenter.present(),
            modifier = modifier,
            onBackClick = ::navigateUp
        )
    }
}
