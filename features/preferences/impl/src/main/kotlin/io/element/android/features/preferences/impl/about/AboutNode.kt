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

package io.element.android.features.preferences.impl.about

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.api.OpenSourceLicensesProvider
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
class AboutNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: AboutPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun openOssLicenses()
    }
    private fun onElementLegalClick(
        activity: Activity,
        darkTheme: Boolean,
        elementLegal: ElementLegal,
    ) {
        activity.openUrlInChromeCustomTab(null, darkTheme, elementLegal.url)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val activity = LocalContext.current as Activity
        val isDark = ElementTheme.isLightTheme.not()
        val state = presenter.present()
        AboutView(
            state = state,
            onBackClick = ::navigateUp,
            onElementLegalClick = { elementLegal ->
                onElementLegalClick(activity, isDark, elementLegal)
            },
            onOpenSourceLicensesClick = {
                plugins.filterIsInstance<Callback>().forEach { it.openOssLicenses() }
            },
            modifier = modifier
        )
    }
}
