/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.welcome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope

@ContributesNode(AppScope::class)
class WelcomeNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val buildMeta: BuildMeta,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onContinueClick()
    }

    private fun onContinueClick() {
        plugins.filterIsInstance<Callback>().forEach { it.onContinueClick() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        WelcomeView(
            applicationName = buildMeta.applicationName,
            onContinueClick = ::onContinueClick,
            modifier = modifier
        )
    }
}
