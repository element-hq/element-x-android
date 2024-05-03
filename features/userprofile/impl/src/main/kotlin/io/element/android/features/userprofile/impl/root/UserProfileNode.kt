/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.userprofile.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.userprofile.shared.UserProfileNodeHelper
import io.element.android.features.userprofile.shared.UserProfileView
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.services.analytics.api.AnalyticsService

@ContributesNode(SessionScope::class)
class UserProfileNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val analyticsService: AnalyticsService,
    private val permalinkBuilder: PermalinkBuilder,
    presenterFactory: UserProfilePresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class UserProfileInputs(
        val userId: UserId
    ) : NodeInputs

    private val inputs = inputs<UserProfileInputs>()
    private val callback = inputs<UserProfileNodeHelper.Callback>()
    private val presenter = presenterFactory.create(inputs.userId)
    private val userProfileNodeHelper = UserProfileNodeHelper(inputs.userId)

    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.User))
            }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        val context = LocalContext.current

        fun onShareUser() {
            userProfileNodeHelper.onShareUser(context, permalinkBuilder)
        }

        fun onStartDM(roomId: RoomId) {
            callback.onStartDM(roomId)
        }

        val state = presenter.present()

        LaunchedEffect(state.startDmActionState) {
            val result = state.startDmActionState
            if (result is AsyncAction.Success) {
                onStartDM(result.data)
            }
        }
        UserProfileView(
            state = state,
            modifier = modifier,
            goBack = this::navigateUp,
            onShareUser = ::onShareUser,
            onDMStarted = ::onStartDM,
            openAvatarPreview = callback::openAvatarPreview,
        )
    }
}
