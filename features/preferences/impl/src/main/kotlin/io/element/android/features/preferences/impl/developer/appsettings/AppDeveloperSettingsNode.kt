/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.appsettings

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.airbnb.android.showkase.models.Showkase
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.designsystem.showkase.getBrowserIntent

@ContributesNode(AppScope::class)
@AssistedInject
class AppDeveloperSettingsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: AppDeveloperSettingsPresenter,
) : Node(buildContext, plugins = plugins) {
    private val callback: PreferencesEntryPoint.DeveloperSettingsCallback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        val activity = requireNotNull(LocalActivity.current)
        fun openShowkase() {
            val intent = Showkase.getBrowserIntent(activity)
            activity.startActivity(intent)
        }

        val state = presenter.present()
        AppDeveloperSettingsPage(
            state = state,
            modifier = modifier,
            onOpenShowkase = ::openShowkase,
            onBackClick = callback::onDone,
        )
    }
}
