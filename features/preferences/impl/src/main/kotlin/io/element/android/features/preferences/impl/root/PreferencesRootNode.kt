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

package io.element.android.features.preferences.impl.root

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.theme.ElementTheme
import timber.log.Timber

@ContributesNode(SessionScope::class)
class PreferencesRootNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: PreferencesRootPresenter,
) : Node(buildContext, plugins = plugins) {

    interface Callback : Plugin {
        fun onOpenBugReport()
        fun onVerifyClicked()
        fun onOpenAnalytics()
        fun onOpenAbout()
        fun onOpenDeveloperSettings()
        fun onOpenNotificationSettings()
        fun onOpenAdvancedSettings()
    }

    private fun onOpenBugReport() {
        plugins<Callback>().forEach { it.onOpenBugReport() }
    }

    private fun onVerifyClicked() {
        plugins<Callback>().forEach { it.onVerifyClicked() }
    }

    private fun onOpenDeveloperSettings() {
        plugins<Callback>().forEach { it.onOpenDeveloperSettings() }
    }

    private fun onOpenAdvancedSettings() {
        plugins<Callback>().forEach { it.onOpenAdvancedSettings() }
    }

    private fun onOpenAnalytics() {
        plugins<Callback>().forEach { it.onOpenAnalytics() }
    }

    private fun onOpenAbout() {
        plugins<Callback>().forEach { it.onOpenAbout() }
    }

    private fun onManageAccountClicked(
        activity: Activity,
        url: String?,
        isDark: Boolean,
    ) {
        url?.let {
            activity.openUrlInChromeCustomTab(
                null,
                darkTheme = isDark,
                url = it
            )
        }
    }

    private fun onOpenNotificationSettings() {
        plugins<Callback>().forEach { it.onOpenNotificationSettings() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = LocalContext.current as Activity
        val isDark = ElementTheme.isLightTheme.not()
        PreferencesRootView(
            state = state,
            modifier = modifier,
            onBackPressed = this::navigateUp,
            onOpenRageShake = this::onOpenBugReport,
            onOpenAnalytics = this::onOpenAnalytics,
            onOpenAbout = this::onOpenAbout,
            onVerifyClicked = this::onVerifyClicked,
            onOpenDeveloperSettings = this::onOpenDeveloperSettings,
            onOpenAdvancedSettings = this::onOpenAdvancedSettings,
            onSuccessLogout = { onSuccessLogout(activity, it) },
            onManageAccountClicked = { onManageAccountClicked(activity, it, isDark) },
            onOpenNotificationSettings = this::onOpenNotificationSettings
        )
    }

    private fun onSuccessLogout(activity: Activity, url: String?) {
        Timber.d("Success logout with result url: $url")
        url?.let {
            activity.openUrlInChromeCustomTab(null, false, it)
        }
    }
}
