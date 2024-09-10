/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.airbnb.android.showkase.models.Showkase
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.designsystem.showkase.getBrowserIntent
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
class DeveloperSettingsNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: DeveloperSettingsPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun openConfigureTracing()
    }

    private fun onOpenConfigureTracing() {
        plugins<Callback>().forEach { it.openConfigureTracing() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val activity = LocalContext.current as Activity
        fun openShowkase() {
            val intent = Showkase.getBrowserIntent(activity)
            activity.startActivity(intent)
        }

        val state = presenter.present()
        DeveloperSettingsView(
            state = state,
            modifier = modifier,
            onOpenShowkase = ::openShowkase,
            onOpenConfigureTracing = ::onOpenConfigureTracing,
            onBackClick = ::navigateUp
        )
    }
}
