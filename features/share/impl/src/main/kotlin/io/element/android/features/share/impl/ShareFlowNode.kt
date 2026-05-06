/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.mediapreview.api.MediaPreviewConfig
import io.element.android.features.mediapreview.api.MediaPreviewEntryPoint
import io.element.android.features.mediapreview.api.SendMode
import io.element.android.features.share.api.ShareEntryPoint
import io.element.android.features.share.api.ShareIntentData
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.mediaviewer.api.local.LocalMediaFactory
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.roomselect.api.RoomSelectEntryPoint
import io.element.android.libraries.roomselect.api.RoomSelectMode
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(SessionScope::class)
@AssistedInject
class ShareFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: SharePresenter.Factory,
    private val roomSelectEntryPoint: RoomSelectEntryPoint,
    private val mediaPreviewEntryPoint: MediaPreviewEntryPoint,
    private val localMediaFactory: LocalMediaFactory,
    private val matrixClient: MatrixClient,
    private val activeRoomsHolder: ActiveRoomsHolder,
) : BaseFlowNode<ShareFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.RoomSelect,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object MediaPreview : NavTarget

        @Parcelize
        data object RoomSelect : NavTarget
    }

    data class Inputs(val shareIntentData: ShareIntentData) : NodeInputs

    private val inputs = inputs<Inputs>()
    private val callback: ShareEntryPoint.Callback = callback()
    private val sharePresenter: SharePresenter = presenterFactory.create(inputs.shareIntentData)

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.RoomSelect -> {
                val roomSelectCallback = object : RoomSelectEntryPoint.Callback {
                    override fun onRoomSelected(roomIds: List<RoomId>) {
                        sharePresenter.onRoomSelected(roomIds)
                        if (inputs.shareIntentData is ShareIntentData.Uris) {
                            backstack.push(NavTarget.MediaPreview)
                        } else {
                            callback.onDone(roomIds)
                        }
                    }

                    override fun onCancel() {
                        callback.onDone(emptyList())
                    }
                }

                roomSelectEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = RoomSelectEntryPoint.Params(mode = RoomSelectMode.Share),
                    callback = roomSelectCallback,
                )
            }
            is NavTarget.MediaPreview -> {
                val shareIntentData = inputs.shareIntentData as ShareIntentData.Uris
                val uriToShare = shareIntentData.uris.firstOrNull()
                val localMedia = uriToShare?.let {
                    Timber.d("ShareMediaPreview: Creating LocalMedia for uri: ${it.uri}, mimeType: ${it.mimeType}")
                    localMediaFactory.createFromUri(it.uri, it.mimeType, null, null)
                }

                if (localMedia == null) {
                    Timber.e("ShareMediaPreview: localMedia is null!")
                    callback.onDone(emptyList())
                    return this
                }

                val roomIds = sharePresenter.onRoomSelectedForMediaPreview()
                val joinedRoom = roomIds?.firstOrNull()?.let { roomId ->
                    activeRoomsHolder.getActiveRoom(matrixClient.sessionId)
                        ?.takeIf { it.roomId == roomId }
                        ?: runCatching { kotlinx.coroutines.runBlocking { matrixClient.getJoinedRoom(roomId) } }.getOrNull()
                }

                val previewCallback = object : MediaPreviewEntryPoint.Callback {
                    override fun onSend(
                        caption: String?,
                        optimizeImage: Boolean,
                        videoPreset: VideoCompressionPreset?,
                        onComplete: () -> Unit,
                    ) {
                        val roomIds = sharePresenter.onProceedFromPreview(caption, optimizeImage, videoPreset)
                        if (roomIds != null) {
                            onComplete()
                            callback.onDone(roomIds)
                        } else {
                            onComplete()
                            callback.onDone(emptyList())
                        }
                    }

                    override fun onCancel() {
                        backstack.pop()
                    }
                }

                mediaPreviewEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = MediaPreviewEntryPoint.Params(
                        localMedia = localMedia,
                        config = MediaPreviewConfig(
                            initialCaption = shareIntentData.text,
                            sendMode = SendMode.PREPROCESS,
                            joinedRoom = joinedRoom,
                        ),
                    ),
                    callback = previewCallback,
                )
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(modifier = modifier)
    }
}
