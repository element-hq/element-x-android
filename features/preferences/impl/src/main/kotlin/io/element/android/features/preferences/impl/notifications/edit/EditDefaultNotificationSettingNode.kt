/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId

@ContributesNode(SessionScope::class)
@AssistedInject
class EditDefaultNotificationSettingNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: EditDefaultNotificationSettingPresenter.Factory
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun navigateToRoomNotificationSettings(roomId: RoomId)
    }

    data class Inputs(
        val isOneToOne: Boolean
    ) : NodeInputs

    private val callback: Callback = callback()
    private val inputs = inputs<Inputs>()
    private val presenter = presenterFactory.create(inputs.isOneToOne)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        EditDefaultNotificationSettingView(
            state = state,
            openRoomNotificationSettings = callback::navigateToRoomNotificationSettings,
            onBackClick = ::navigateUp,
            modifier = modifier,
        )
    }
}
