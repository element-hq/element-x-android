/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.reportroom.impl.room
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.reportroom.impl.RoomIdPlugin
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.RoomId

@ContributesNode(AppScope::class)
class ReportRoomNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenterFactory: ReportRoomPresenter.Factory,
) : Node(buildContext, plugins = plugins) {

    private val roomId = plugins<RoomIdPlugin>().first().roomId
    private val presenter: ReportRoomPresenter = presenterFactory.create(roomId = roomId)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        ReportRoomView(
            state = state,
            modifier = modifier,
            onBackClick = ::navigateUp,
        )
    }
}
