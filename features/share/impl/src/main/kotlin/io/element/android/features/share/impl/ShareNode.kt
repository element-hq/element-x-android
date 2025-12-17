/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import android.content.Intent
import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.share.api.ShareEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.roomselect.api.RoomSelectEntryPoint
import io.element.android.libraries.roomselect.api.RoomSelectMode
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@AssistedInject
class ShareNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: SharePresenter.Factory,
    private val roomSelectEntryPoint: RoomSelectEntryPoint,
) : ParentNode<ShareNode.NavTarget>(
    navModel = PermanentNavModel(
        navTargets = setOf(NavTarget),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    @Parcelize
    object NavTarget : Parcelable

    data class Inputs(val intent: Intent) : NodeInputs

    private val inputs = inputs<Inputs>()
    private val presenter = presenterFactory.create(inputs.intent)
    private val callback: ShareEntryPoint.Callback = callback()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        val callback = object : RoomSelectEntryPoint.Callback {
            override fun onRoomSelected(roomIds: List<RoomId>) {
                presenter.onRoomSelected(roomIds)
            }

            override fun onCancel() {
                callback.onDone(emptyList())
            }
        }

        return roomSelectEntryPoint.createNode(
            parentNode = this,
            buildContext = buildContext,
            params = RoomSelectEntryPoint.Params(mode = RoomSelectMode.Share),
            callback = callback,
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        Box(modifier = modifier) {
            // Will render to room select screen
            Children(
                navModel = navModel,
            )

            val state = presenter.present()
            ShareView(
                state = state,
                onShareSuccess = callback::onDone,
            )
        }
    }
}
