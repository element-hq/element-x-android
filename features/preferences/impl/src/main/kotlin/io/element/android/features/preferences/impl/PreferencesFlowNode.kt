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

package io.element.android.features.preferences.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.features.preferences.impl.about.AboutNode
import io.element.android.features.preferences.impl.analytics.AnalyticsSettingsNode
import io.element.android.features.preferences.impl.developer.DeveloperSettingsNode
import io.element.android.features.preferences.impl.notifications.NotificationSettingsNode
import io.element.android.features.preferences.impl.notifications.edit.EditDefaultNotificationSettingNode
import io.element.android.features.preferences.impl.developer.tracing.ConfigureTracingNode
import io.element.android.features.preferences.impl.root.PreferencesRootNode
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class PreferencesFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BackstackNode<PreferencesFlowNode.NavTarget>(
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
        data object DeveloperSettings : NavTarget

        @Parcelize
        data object ConfigureTracing : NavTarget

        @Parcelize
        data object AnalyticsSettings : NavTarget

        @Parcelize

        data object About : NavTarget

        @Parcelize
        data object NotificationSettings : NavTarget

        @Parcelize
        data class EditDefaultNotificationSetting(val isOneToOne: Boolean) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : PreferencesRootNode.Callback {
                    override fun onOpenBugReport() {
                        plugins<PreferencesEntryPoint.Callback>().forEach { it.onOpenBugReport() }
                    }

                    override fun onVerifyClicked() {
                        plugins<PreferencesEntryPoint.Callback>().forEach { it.onVerifyClicked() }
                    }

                    override fun onOpenAnalytics() {
                        backstack.push(NavTarget.AnalyticsSettings)
                    }

                    override fun onOpenAbout() {
                        backstack.push(NavTarget.About)
                    }

                    override fun onOpenDeveloperSettings() {
                        backstack.push(NavTarget.DeveloperSettings)
                    }

                    override fun onOpenNotificationSettings() {
                        backstack.push(NavTarget.NotificationSettings)
                    }
                }
                createNode<PreferencesRootNode>(buildContext, plugins = listOf(callback))
            }
            NavTarget.DeveloperSettings -> {
                val callback = object : DeveloperSettingsNode.Callback {
                    override fun openConfigureTracing() {
                        backstack.push(NavTarget.ConfigureTracing)
                    }
                }
                createNode<DeveloperSettingsNode>(buildContext, listOf(callback))
            }
            NavTarget.ConfigureTracing -> {
                createNode<ConfigureTracingNode>(buildContext)
            }
            NavTarget.About -> {
                createNode<AboutNode>(buildContext)
            }
            NavTarget.AnalyticsSettings -> {
                createNode<AnalyticsSettingsNode>(buildContext)
            }
            NavTarget.NotificationSettings -> {
                val notificationSettingsCallback = object : NotificationSettingsNode.Callback {
                    override fun editDefaultNotificationMode(isOneToOne: Boolean) {
                        backstack.push(NavTarget.EditDefaultNotificationSetting(isOneToOne))
                    }
                }
                createNode<NotificationSettingsNode>(buildContext, listOf(notificationSettingsCallback))
            }
            is NavTarget.EditDefaultNotificationSetting -> {
                val input = EditDefaultNotificationSettingNode.Inputs(navTarget.isOneToOne)
                createNode<EditDefaultNotificationSettingNode>(buildContext, plugins = listOf(input))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backstack,
            modifier = modifier,
            transitionHandler = rememberDefaultTransitionHandler()
        )
    }
}
