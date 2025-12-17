/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.history

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.poll.api.create.CreatePollEntryPoint
import io.element.android.features.poll.api.create.CreatePollMode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
@AssistedInject
class PollHistoryFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val createPollEntryPoint: CreatePollEntryPoint,
) : BaseFlowNode<PollHistoryFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class EditPoll(val pollStartEventId: EventId) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.EditPoll -> {
                createPollEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = CreatePollEntryPoint.Params(
                        timelineMode = Timeline.Mode.Live,
                        mode = CreatePollMode.EditPoll(eventId = navTarget.pollStartEventId)
                    )
                )
            }
            NavTarget.Root -> {
                val callback = object : PollHistoryNode.Callback {
                    override fun navigateToEditPoll(pollStartEventId: EventId) {
                        backstack.push(NavTarget.EditPoll(pollStartEventId))
                    }
                }
                createNode<PollHistoryNode>(
                    buildContext = buildContext,
                    plugins = listOf(callback)
                )
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
