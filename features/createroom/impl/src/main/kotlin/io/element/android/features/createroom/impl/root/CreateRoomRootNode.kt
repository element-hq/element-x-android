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

package io.element.android.features.createroom.impl.root

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.deeplink.usecase.InviteFriendsUseCase
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.services.analytics.api.AnalyticsService

@ContributesNode(SessionScope::class)
class CreateRoomRootNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: CreateRoomRootPresenter,
    private val analyticsService: AnalyticsService,
    private val inviteFriendsUseCase: InviteFriendsUseCase,
) : Node(buildContext, plugins = plugins) {

    interface Callback : Plugin {
        fun onCreateNewRoom()
        fun onStartChatSuccess(roomId: RoomId)
    }

    private val callback = object : Callback {
        override fun onCreateNewRoom() {
            plugins<Callback>().forEach { it.onCreateNewRoom() }
        }

        override fun onStartChatSuccess(roomId: RoomId) {
            plugins<Callback>().forEach { it.onStartChatSuccess(roomId) }
        }
    }

    init {
        lifecycle.subscribe(
            onResume = { analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.StartChat)) }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = LocalContext.current as Activity
        CreateRoomRootView(
            state = state,
            modifier = modifier,
            onClosePressed = this::navigateUp,
            onNewRoomClicked = callback::onCreateNewRoom,
            onOpenDM = callback::onStartChatSuccess,
            onInviteFriendsClicked = { invitePeople(activity) }
        )
    }

    private fun invitePeople(activity: Activity) {
        inviteFriendsUseCase.execute(activity)
    }
}
