/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.invite

import androidx.compose.runtime.Composable
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
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.apperror.api.AppErrorStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@ContributesNode(RoomScope::class)
class RoomInviteMembersNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    coroutineDispatchers: CoroutineDispatchers,
    private val room: JoinedRoom,
    private val presenter: RoomInviteMembersPresenter,
    private val appErrorStateService: AppErrorStateService,
    private val analyticsService: AnalyticsService,
) : Node(buildContext, plugins = plugins) {
    private val coroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatchers.io)

    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.Invites))
            }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val context = LocalContext.current.applicationContext

        RoomInviteMembersView(
            state = state,
            modifier = modifier,
            onBackClick = { navigateUp() },
            onSubmitClick = { users ->
                navigateUp()

                coroutineScope.launch {
                    val anyInviteFailed = users
                        .map { room.inviteUserById(it.userId) }
                        .any { it.isFailure }

                    if (anyInviteFailed) {
                        appErrorStateService.showError(
                            title = context.getString(CommonStrings.common_unable_to_invite_title),
                            body = context.getString(CommonStrings.common_unable_to_invite_message),
                        )
                    }
                }
            }
        )
    }
}
