/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.accountselect.api.AccountSelectEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.matrix.api.permalink.PermalinkData

@ContributesNode(AppScope::class)
@AssistedInject
class AccountSelectNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: AccountSelectPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val permalinkData: PermalinkData,
    ) : NodeInputs

    private val callback: AccountSelectEntryPoint.Callback = callback()
    private val presenter = presenterFactory.create(inputs<Inputs>().permalinkData)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        AccountSelectView(
            state = state,
            onDismiss = callback::onCancel,
            onSelectAccount = callback::onAccountSelected,
            modifier = modifier,
        )
    }
}
