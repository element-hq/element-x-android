/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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

    private fun onCreateNewRoom() {
        plugins<Callback>().forEach { it.onCreateNewRoom() }
    }

    private fun onStartChatSuccess(roomId: RoomId) {
        plugins<Callback>().forEach { it.onStartChatSuccess(roomId) }
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
            onCloseClick = this::navigateUp,
            onNewRoomClick = ::onCreateNewRoom,
            onOpenDM = ::onStartChatSuccess,
            onInviteFriendsClick = { invitePeople(activity) }
        )
    }

    private fun invitePeople(activity: Activity) {
        inviteFriendsUseCase.execute(activity)
    }
}
