/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.analytics.impl

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
import io.element.android.appconfig.AnalyticsConfig
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.di.AppScope

@ContributesNode(AppScope::class)
class AnalyticsOptInNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: AnalyticsOptInPresenter,
) : Node(buildContext, plugins = plugins) {
    private fun onClickTerms(activity: Activity, darkTheme: Boolean) {
        activity.openUrlInChromeCustomTab(null, darkTheme, AnalyticsConfig.POLICY_LINK)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val activity = LocalContext.current as Activity
        val isDark = ElementTheme.isLightTheme.not()
        val state = presenter.present()
        AnalyticsOptInView(
            state = state,
            modifier = modifier,
            onClickTerms = { onClickTerms(activity, isDark) },
        )
    }
}
