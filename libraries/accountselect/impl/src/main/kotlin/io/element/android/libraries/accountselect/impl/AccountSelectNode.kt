/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.accountselect.api.AccountSelectEntryPoint
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.SessionId

@ContributesNode(AppScope::class)
class AccountSelectNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: AccountSelectPresenter,
) : Node(buildContext, plugins = plugins) {
    private val callbacks = plugins.filterIsInstance<AccountSelectEntryPoint.Callback>()

    private fun onDismiss() {
        callbacks.forEach { it.onCancel() }
    }

    private fun onSelectAccount(sessionId: SessionId) {
        callbacks.forEach { it.onSelectAccount(sessionId) }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        AccountSelectView(
            state = state,
            onDismiss = ::onDismiss,
            onSelectAccount = ::onSelectAccount,
            modifier = modifier,
        )
    }
}
