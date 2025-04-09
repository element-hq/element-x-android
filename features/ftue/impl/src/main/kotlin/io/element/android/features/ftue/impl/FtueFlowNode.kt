/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.replace
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.analytics.api.AnalyticsEntryPoint
import io.element.android.features.ftue.impl.notifications.NotificationsOptInNode
import io.element.android.features.ftue.impl.sessionverification.FtueSessionVerificationFlowNode
import io.element.android.features.ftue.impl.state.DefaultFtueService
import io.element.android.features.ftue.impl.state.FtueStep
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SessionScope
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class FtueFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val ftueState: DefaultFtueService,
    private val analyticsEntryPoint: AnalyticsEntryPoint,
    private val analyticsService: AnalyticsService,
    private val lockScreenEntryPoint: LockScreenEntryPoint,
) : BaseFlowNode<FtueFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Placeholder,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Placeholder : NavTarget

        @Parcelize
        data object SessionVerification : NavTarget

        @Parcelize
        data object NotificationsOptIn : NavTarget

        @Parcelize
        data object AnalyticsOptIn : NavTarget

        @Parcelize
        data object LockScreenSetup : NavTarget
    }

    override fun onBuilt() {
        super.onBuilt()

        lifecycle.subscribe(onCreate = {
            moveToNextStepIfNeeded()
        })

        analyticsService.didAskUserConsentFlow
            .distinctUntilChanged()
            .onEach { moveToNextStepIfNeeded() }
            .launchIn(lifecycleScope)

        ftueState.isVerificationStatusKnown
            .filter { it }
            .onEach { moveToNextStepIfNeeded() }
            .launchIn(lifecycleScope)
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Placeholder -> {
                createNode<PlaceholderNode>(buildContext)
            }
            is NavTarget.SessionVerification -> {
                val callback = object : FtueSessionVerificationFlowNode.Callback {
                    override fun onDone() {
                        moveToNextStepIfNeeded()
                    }
                }
                createNode<FtueSessionVerificationFlowNode>(buildContext, listOf(callback))
            }
            NavTarget.NotificationsOptIn -> {
                val callback = object : NotificationsOptInNode.Callback {
                    override fun onNotificationsOptInFinished() {
                        moveToNextStepIfNeeded()
                    }
                }
                createNode<NotificationsOptInNode>(buildContext, listOf(callback))
            }
            NavTarget.AnalyticsOptIn -> {
                analyticsEntryPoint.createNode(this, buildContext)
            }
            NavTarget.LockScreenSetup -> {
                val callback = object : LockScreenEntryPoint.Callback {
                    override fun onSetupDone() {
                        moveToNextStepIfNeeded()
                    }
                }
                lockScreenEntryPoint.nodeBuilder(this, buildContext, LockScreenEntryPoint.Target.Setup)
                    .callback(callback)
                    .build()
            }
        }
    }

    private fun moveToNextStepIfNeeded() = lifecycleScope.launch {
        when (ftueState.getNextStep()) {
            FtueStep.WaitingForInitialState -> {
                backstack.newRoot(NavTarget.Placeholder)
            }
            FtueStep.SessionVerification -> {
                backstack.newRoot(NavTarget.SessionVerification)
            }
            FtueStep.NotificationsOptIn -> {
                backstack.newRoot(NavTarget.NotificationsOptIn)
            }
            FtueStep.AnalyticsOptIn -> {
                backstack.replace(NavTarget.AnalyticsOptIn)
            }
            FtueStep.LockscreenSetup -> {
                backstack.newRoot(NavTarget.LockScreenSetup)
            }
            null -> Unit
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }

    @ContributesNode(AppScope::class)
    class PlaceholderNode @AssistedInject constructor(
        @Assisted buildContext: BuildContext,
        @Assisted plugins: List<Plugin>,
    ) : Node(buildContext, plugins = plugins) {
        @Composable
        override fun View(modifier: Modifier) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
