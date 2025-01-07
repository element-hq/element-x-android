/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomaliasresolver.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.roomaliasesolver.api.RoomAliasResolverEntryPoint
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias

@ContributesNode(SessionScope::class)
class RoomAliasResolverNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: RoomAliasResolverPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    private val inputs = inputs<RoomAliasResolverEntryPoint.Params>()

    private val presenter = presenterFactory.create(
        inputs.roomAlias
    )

    private fun onAliasResolved(data: ResolvedRoomAlias) {
        plugins<RoomAliasResolverEntryPoint.Callback>().forEach { it.onAliasResolved(data) }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        RoomAliasResolverView(
            state = state,
            onSuccess = ::onAliasResolved,
            onBackClick = ::navigateUp,
            modifier = modifier
        )
    }
}
