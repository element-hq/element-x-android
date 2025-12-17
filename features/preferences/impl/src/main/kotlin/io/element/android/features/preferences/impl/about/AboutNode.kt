/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.about

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class AboutNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: AboutPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun navigateToOssLicenses()
    }

    private val callback: Callback = callback()

    private fun onElementLegalClick(
        activity: Activity,
        darkTheme: Boolean,
        elementLegal: ElementLegal,
    ) {
        activity.openUrlInChromeCustomTab(null, darkTheme, elementLegal.url)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val activity = requireNotNull(LocalActivity.current)
        val isDark = ElementTheme.isLightTheme.not()
        val state = presenter.present()
        AboutView(
            state = state,
            onBackClick = ::navigateUp,
            onElementLegalClick = { elementLegal ->
                onElementLegalClick(activity, isDark, elementLegal)
            },
            onOpenSourceLicensesClick = callback::navigateToOssLicenses,
            modifier = modifier
        )
    }
}
