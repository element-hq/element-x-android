/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.root

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.createroom.CreateRoomNavigator
import io.element.android.libraries.deeplink.usecase.InviteFriendsUseCase
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.services.analytics.api.AnalyticsService

@ContributesNode(SessionScope::class)
class CreateRoomRootNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: CreateRoomRootPresenter,
    private val analyticsService: AnalyticsService,
    private val inviteFriendsUseCase: InviteFriendsUseCase,
) : Node(buildContext, plugins = plugins) {
    private val navigator = plugins<CreateRoomNavigator>().first()

    init {
        lifecycle.subscribe(
            onResume = { analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.StartChat)) }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = requireNotNull(LocalActivity.current)
        CreateRoomRootView(
            state = state,
            modifier = modifier,
            onCloseClick = this::navigateUp,
            onNewRoomClick = navigator::onCreateNewRoom,
            onOpenDM = {
                navigator.onOpenRoom(roomIdOrAlias = it.toRoomIdOrAlias(), serverNames = emptyList())
            },
            onJoinByAddressClick = navigator::onShowJoinRoomByAddress,
            onInviteFriendsClick = { invitePeople(activity) },
            onRoomDirectorySearchClick = navigator::onOpenRoomDirectory
        )
    }

    private fun invitePeople(activity: Activity) {
        inviteFriendsUseCase.execute(activity)
    }
}
