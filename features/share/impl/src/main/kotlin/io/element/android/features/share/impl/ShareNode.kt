/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
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
import io.element.android.features.share.api.ShareIntentData
import io.element.android.features.share.impl.di.ShareBindings
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.SessionGraphFactory
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.roomselect.api.RoomSelectEntryPoint
import io.element.android.libraries.roomselect.api.RoomSelectMode
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(SessionScope::class)
@AssistedInject
class ShareNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: SharePresenter.Factory,
    private val roomSelectEntryPoint: RoomSelectEntryPoint,
    private val sessionId: SessionId,
    private val matrixClientProvider: MatrixClientProvider,
    private val sessionGraphFactory: SessionGraphFactory,
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

    data class Inputs(val shareIntentData: ShareIntentData) : NodeInputs

    /**
     * The presenter used to share the data, and the session it belongs to.
     */
    private data class ActiveShare(
        val sessionId: SessionId,
        val presenter: SharePresenter,
    )

    private val inputs = inputs<Inputs>()
    private val callback: ShareEntryPoint.Callback = callback()
    private val currentSessionShare = ActiveShare(
        sessionId = sessionId,
        presenter = presenterFactory.create(inputs.shareIntentData),
    )
    private var activeShare by mutableStateOf(currentSessionShare)

    private fun onRoomSelected(sessionId: SessionId, roomIds: List<RoomId>) {
        if (sessionId == currentSessionShare.sessionId) {
            activeShare = currentSessionShare
            currentSessionShare.presenter.onRoomSelected(roomIds)
        } else if (sessionId == activeShare.sessionId) {
            // Retry with the same other session
            activeShare.presenter.onRoomSelected(roomIds)
        } else {
            lifecycleScope.launch {
                // Share using the graph of the selected session, the current session will be changed once the share is done
                matrixClientProvider.getOrRestore(sessionId)
                    .onSuccess { client ->
                        val presenterFactory = (sessionGraphFactory.create(client) as ShareBindings).sharePresenterFactory()
                        activeShare = ActiveShare(
                            sessionId = sessionId,
                            presenter = presenterFactory.create(inputs.shareIntentData),
                        ).also {
                            it.presenter.onRoomSelected(roomIds)
                        }
                    }
                    .onFailure {
                        Timber.e(it, "Failed to get the client for session $sessionId")
                    }
            }
        }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        val callback = object : RoomSelectEntryPoint.Callback {
            override fun onRoomSelected(sessionId: SessionId, roomIds: List<RoomId>) {
                this@ShareNode.onRoomSelected(sessionId, roomIds)
            }

            override fun onCancel() {
                callback.onDone(sessionId, emptyList())
            }
        }

        return roomSelectEntryPoint.createNode(
            parentNode = this,
            buildContext = buildContext,
            params = RoomSelectEntryPoint.Params(
                sessionId = sessionId,
                mode = RoomSelectMode.Share,
                maxNumberOfRooms = RoomSelectEntryPoint.DEFAULT_MAX_NUMBER_OF_ROOMS,
            ),
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

            val currentShare = activeShare
            key(currentShare) {
                val state = currentShare.presenter.present()
                ShareView(
                    state = state,
                    onShareSuccess = { roomIds -> callback.onDone(currentShare.sessionId, roomIds) },
                )
            }
        }
    }
}
