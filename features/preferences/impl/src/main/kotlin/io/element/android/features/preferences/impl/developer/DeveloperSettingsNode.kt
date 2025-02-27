/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.airbnb.android.showkase.models.Showkase
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
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
    @Composable
    override fun View(modifier: Modifier) {
        val activity = requireNotNull(LocalActivity.current)
        fun openShowkase() {
            val intent = Showkase.getBrowserIntent(activity)
            activity.startActivity(intent)
        }

        val state = presenter.present()
        DeveloperSettingsView(
            state = state,
            modifier = modifier,
            onOpenShowkase = ::openShowkase,
            onBackClick = ::navigateUp
        )
    }
}
