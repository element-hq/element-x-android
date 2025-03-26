/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.create

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
import io.element.android.features.poll.api.create.CreatePollMode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.RoomScope
import io.element.android.services.analytics.api.AnalyticsService
import java.util.concurrent.atomic.AtomicBoolean

@ContributesNode(RoomScope::class)
class CreatePollNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: CreatePollPresenter.Factory,
    analyticsService: AnalyticsService,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(val mode: CreatePollMode) : NodeInputs

    private val inputs: Inputs = inputs()

    private var isNavigatingUp = AtomicBoolean(false)

    private val presenter = presenterFactory.create(
        backNavigator = {
            if (isNavigatingUp.compareAndSet(false, true)) {
                navigateUp()
            }
        },
        mode = inputs.mode,
    )

    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.CreatePollView))
            }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        CreatePollView(
            state = presenter.present(),
            modifier = modifier,
        )
    }
}
