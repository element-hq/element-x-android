/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId

@ContributesNode(SessionScope::class)
class EditDefaultNotificationSettingNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: EditDefaultNotificationSettingPresenter.Factory
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun openRoomNotificationSettings(roomId: RoomId)
    }

    data class Inputs(
        val isOneToOne: Boolean
    ) : NodeInputs

    private val inputs = inputs<Inputs>()
    private val callbacks = plugins<Callback>()
    private val presenter = presenterFactory.create(inputs.isOneToOne)

    private fun openRoomNotificationSettings(roomId: RoomId) {
        callbacks.forEach { it.openRoomNotificationSettings(roomId) }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        EditDefaultNotificationSettingView(
            state = state,
            openRoomNotificationSettings = { openRoomNotificationSettings(it) },
            onBackClick = ::navigateUp,
            modifier = modifier,
        )
    }
}
