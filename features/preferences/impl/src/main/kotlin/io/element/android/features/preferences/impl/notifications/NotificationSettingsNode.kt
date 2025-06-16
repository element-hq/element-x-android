/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
class NotificationSettingsNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: NotificationSettingsPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun editDefaultNotificationMode(isOneToOne: Boolean)
        fun onTroubleshootNotificationsClick()
        fun onPushHistoryClick()
    }

    private val callbacks = plugins<Callback>()

    private fun openEditDefault(isOneToOne: Boolean) {
        callbacks.forEach { it.editDefaultNotificationMode(isOneToOne) }
    }

    private fun onTroubleshootNotificationsClick() {
        callbacks.forEach { it.onTroubleshootNotificationsClick() }
    }

    private fun onPushHistoryClick() {
        callbacks.forEach { it.onPushHistoryClick() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        NotificationSettingsView(
            state = state,
            onOpenEditDefault = { openEditDefault(isOneToOne = it) },
            onBackClick = ::navigateUp,
            onTroubleshootNotificationsClick = ::onTroubleshootNotificationsClick,
            onPushHistoryClick = ::onPushHistoryClick,
            modifier = modifier,
        )
    }
}
