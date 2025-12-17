/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.invite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.annotations.ContributesNode
import io.element.android.features.invitepeople.api.InvitePeoplePresenter
import io.element.android.features.invitepeople.api.InvitePeopleRenderer
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.services.analytics.api.AnalyticsService

@ContributesNode(RoomScope::class)
@AssistedInject
class RoomInviteMembersNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val analyticsService: AnalyticsService,
    private val invitePeopleRenderer: InvitePeopleRenderer,
    room: JoinedRoom,
    invitePeoplePresenterFactory: InvitePeoplePresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.Invites))
            }
        )
    }

    private val invitePeoplePresenter = invitePeoplePresenterFactory.create(
        joinedRoom = room,
        roomId = room.roomId,
    )

    @Composable
    override fun View(modifier: Modifier) {
        val state = invitePeoplePresenter.present()

        // Once invites have been sent successfully, close the Invite view.
        LaunchedEffect(state.sendInvitesAction) {
            if (state.sendInvitesAction.isReady()) {
                navigateUp()
            }
        }

        RoomInviteMembersView(
            state = state,
            modifier = modifier,
            onBackClick = { navigateUp() }
        ) {
            invitePeopleRenderer.Render(state, Modifier)
        }
    }
}
