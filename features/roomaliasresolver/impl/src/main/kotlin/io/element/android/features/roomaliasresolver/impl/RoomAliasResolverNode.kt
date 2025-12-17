/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomaliasresolver.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.roomaliasesolver.api.RoomAliasResolverEntryPoint
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class RoomAliasResolverNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: RoomAliasResolverPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    private val callback: RoomAliasResolverEntryPoint.Callback = callback()
    private val inputs = inputs<RoomAliasResolverEntryPoint.Params>()

    private val presenter = presenterFactory.create(
        inputs.roomAlias
    )

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        RoomAliasResolverView(
            state = state,
            onSuccess = callback::onAliasResolved,
            onBackClick = ::navigateUp,
            modifier = modifier
        )
    }
}
