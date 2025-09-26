/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.root

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.libraries.androidutils.R
import io.element.android.libraries.androidutils.system.startSharePlainTextIntent
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesNode(SessionScope::class)
@AssistedInject
class SpaceNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: SpacePresenter.Factory,
    private val matrixClient: MatrixClient,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onOpenRoom(roomId: RoomId, viaParameters: List<String>)
        fun onLeaveSpace()
    }

    private val inputs: SpaceEntryPoint.Inputs = inputs()
    private val callback = plugins.filterIsInstance<Callback>().single()
    private val presenter = presenterFactory.create(inputs)

    private fun onShareRoom(context: Context) = lifecycleScope.launch {
        matrixClient.getRoom(inputs.roomId)?.use { room ->
            room.getPermalink()
                .onSuccess { permalink ->
                    context.startSharePlainTextIntent(
                        activityResultLauncher = null,
                        chooserTitle = context.getString(CommonStrings.common_share_space),
                        text = permalink,
                        noActivityFoundMessage = context.getString(R.string.error_no_compatible_app_found)
                    )
                }
                .onFailure {
                    Timber.e(it)
                }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val context = LocalContext.current
        SpaceView(
            state = state,
            onBackClick = ::navigateUp,
            onLeaveSpaceClick = {
                callback.onLeaveSpace()
            },
            onRoomClick = { spaceRoom ->
                callback.onOpenRoom(spaceRoom.roomId, spaceRoom.via)
            },
            onShareSpace = {
                onShareRoom(context)
            },
            modifier = modifier
        )
    }
}
