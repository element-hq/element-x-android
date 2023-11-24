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

package io.element.android.features.poll.impl.history

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
class PollHistoryNode @AssistedInject constructor(
    private val room: MatrixRoom,
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BackstackNode<PollHistoryNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.PollHistoryLoading,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object PollHistoryLoading : NavTarget

        @Parcelize
        data object PollHistoryLoaded : NavTarget
    }

    private var pollHistory: MatrixTimeline? = null

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                lifecycleScope.launch {
                    runCatching {
                        room.pollHistory()
                    }.onSuccess {
                        pollHistory = it
                        backstack.newRoot(NavTarget.PollHistoryLoaded)
                    }
                }
            },
            onDestroy = {
                pollHistory?.close()
            },
        )
    }

    override fun resolve(
        navTarget: NavTarget,
        buildContext: BuildContext
    ): Node = when (navTarget) {
        is NavTarget.PollHistoryLoading -> createNode<PollHistoryLoadingNode>(
            buildContext = buildContext,
        )
        is NavTarget.PollHistoryLoaded -> {
            createNode<PollHistoryLoadedNode>(
                buildContext = buildContext,
                plugins = listOf(
                    PollHistoryLoadedNode.Inputs(
                        pollHistory = pollHistory ?: error("Poll history not loaded"),
                    )
                ),
            )
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backstack,
            modifier = modifier,
            transitionHandler = rememberDefaultTransitionHandler(),
        )
    }
}
