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
import io.element.android.libraries.matrix.api.room.MatrixRoom
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
    private val room: MatrixRoom,
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
            onBackPressed = { navigateUp() },
            onSubmitPressed = { users ->
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

                    room.updateMembers()
                }
            }
        )
    }
}
