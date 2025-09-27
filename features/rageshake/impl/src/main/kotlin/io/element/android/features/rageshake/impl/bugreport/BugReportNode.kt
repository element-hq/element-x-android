/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.bugreport

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.rageshake.api.reporter.BugReporter
import io.element.android.libraries.androidutils.system.toast
import io.element.android.libraries.ui.strings.CommonStrings

@ContributesNode(AppScope::class)
@AssistedInject
class BugReportNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: BugReportPresenter,
    private val bugReporter: BugReporter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onDone()
        fun onViewLogs(basePath: String)
    }

    private fun onViewLogs(basePath: String) {
        plugins<Callback>().forEach { it.onViewLogs(basePath) }
    }

    private fun onDone() {
        plugins<Callback>().forEach { it.onDone() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = LocalActivity.current
        BugReportView(
            state = state,
            modifier = modifier,
            onBackClick = { navigateUp() },
            onSuccess = {
                activity?.toast(CommonStrings.common_report_submitted)
                onDone()
            },
            onViewLogs = {
                // Force a logcat dump
                bugReporter.saveLogCat()
                onViewLogs(bugReporter.logDirectory().absolutePath)
            }
        )
    }
}
