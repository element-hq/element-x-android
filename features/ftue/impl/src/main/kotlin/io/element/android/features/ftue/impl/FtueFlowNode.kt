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

package io.element.android.features.ftue.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.backpresshandlerstrategies.BaseBackPressHandlerStrategy
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.replace
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.analytics.api.AnalyticsEntryPoint
import io.element.android.features.ftue.api.FtueEntryPoint
import io.element.android.features.ftue.api.FtueState
import io.element.android.features.ftue.api.FtueStep
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
class FtueFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val ftueState: FtueState,
    private val analyticsEntryPoint: AnalyticsEntryPoint,
    private val analyticsService: AnalyticsService,
) : BackstackNode<FtueFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Placeholder,
        savedStateMap = buildContext.savedStateMap,
        backPressHandler = NoOpBackstackHandlerStrategy<NavTarget>(),
    ),
    buildContext = buildContext,
    plugins = plugins,
) {

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object Placeholder : NavTarget

        @Parcelize
        object WelcomeScreen : NavTarget

        @Parcelize
        object AnalyticsOptIn : NavTarget
    }

    private val callback = plugins.filterIsInstance<FtueEntryPoint.Callback>().firstOrNull()

    override fun onBuilt() {
        super.onBuilt()

        lifecycle.subscribe(onCreate = {
            lifecycleScope.launch { moveToNextStep() }
        })

        analyticsService.didAskUserConsent()
            .drop(1) // We only care about consent passing from not asked to asked state
            .onEach { didAskUserConsent ->
                if (didAskUserConsent) {
                    lifecycleScope.launch { moveToNextStep() }
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Placeholder -> {
                createNode<PlaceholderNode>(buildContext)
            }
            NavTarget.WelcomeScreen -> {
                val callback = object : WelcomeNode.Callback {
                    override fun onContinueClicked() {
                        ftueState.setWelcomeScreenShown()
                        lifecycleScope.launch { moveToNextStep() }
                    }
                }
                createNode<WelcomeNode>(buildContext, listOf(callback))
            }
            NavTarget.AnalyticsOptIn -> {
                analyticsEntryPoint.createNode(this, buildContext)
            }
        }
    }

    private suspend fun moveToNextStep() {
        when (ftueState.getNextStep()) {
            is FtueStep.WelcomeScreen -> {
                backstack.newRoot(NavTarget.WelcomeScreen)
            }
            is FtueStep.AnalyticsOptIn -> {
                backstack.replace(NavTarget.AnalyticsOptIn)
            }
            null -> callback?.onFtueFlowFinished()
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

    @ContributesNode(AppScope::class)
    class PlaceholderNode @AssistedInject constructor(
        @Assisted buildContext: BuildContext,
        @Assisted plugins: List<Plugin>,
    ) : Node(buildContext, plugins = plugins)
}

private class NoOpBackstackHandlerStrategy<NavTarget : Any> : BaseBackPressHandlerStrategy<NavTarget, BackStack.State>() {
    override val canHandleBackPressFlow: StateFlow<Boolean> = MutableStateFlow(true)

    override fun onBackPressed() {
        // No-op
    }
}
