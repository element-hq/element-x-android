/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.invite.api.response.AcceptDeclineInviteView
import io.element.android.features.joinroom.api.JoinRoomEntryPoint
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
class JoinRoomNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: JoinRoomPresenter.Factory,
    private val acceptDeclineInviteView: AcceptDeclineInviteView,
) : Node(buildContext, plugins = plugins) {
    private val inputs: JoinRoomEntryPoint.Inputs = inputs()
    private val presenter = presenterFactory.create(
        inputs.roomId,
        inputs.roomIdOrAlias,
        inputs.roomDescription,
        inputs.serverNames,
        inputs.trigger,
    )

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        JoinRoomView(
            state = state,
            onBackClick = ::navigateUp,
            onJoinSuccess = ::navigateUp,
            onCancelKnockSuccess = ::navigateUp,
            onKnockSuccess = { },
            modifier = modifier
        )
        acceptDeclineInviteView.Render(
            state = state.acceptDeclineInviteState,
            onAcceptInvite = {},
            onDeclineInvite = { navigateUp() },
            modifier = Modifier
        )
    }
}
