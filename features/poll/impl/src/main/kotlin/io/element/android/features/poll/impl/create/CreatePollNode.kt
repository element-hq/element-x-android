/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.di.RoomScope

@ContributesNode(RoomScope::class)
class CreatePollNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: CreatePollPresenter.Factory,
    // analyticsService: AnalyticsService, // TODO Polls: add analytics
) : Node(buildContext, plugins = plugins) {

    private val presenter = presenterFactory.create(backNavigator = ::navigateUp)

    init {
        lifecycle.subscribe(
            onResume = {
                // TODO Polls: add analytics
                // analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.PollView))
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
