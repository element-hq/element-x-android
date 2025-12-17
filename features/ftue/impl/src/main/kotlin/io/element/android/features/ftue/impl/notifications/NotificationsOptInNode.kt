/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs

@ContributesNode(AppScope::class)
@AssistedInject
class NotificationsOptInNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: NotificationsOptInPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    interface Callback : NodeInputs {
        fun onNotificationsOptInFinished()
    }

    private val callback = inputs<Callback>()

    private val presenter: NotificationsOptInPresenter = presenterFactory.create(callback)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        NotificationsOptInView(
            state = state,
            onBack = { callback.onNotificationsOptInFinished() },
            modifier = modifier
        )
    }
}
