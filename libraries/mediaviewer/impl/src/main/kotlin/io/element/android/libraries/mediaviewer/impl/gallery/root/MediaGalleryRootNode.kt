/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.root

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.BackstackWithOverlayBox
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.overlay.Overlay
import io.element.android.libraries.architecture.overlay.operation.hide
import io.element.android.libraries.architecture.overlay.operation.show
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.MediaGalleryEntryPoint
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.mediaviewer.impl.gallery.MediaGalleryNode
import io.element.android.libraries.mediaviewer.impl.gallery.MediaItem
import io.element.android.libraries.mediaviewer.impl.gallery.eventId
import io.element.android.libraries.mediaviewer.impl.gallery.mediaInfo
import io.element.android.libraries.mediaviewer.impl.gallery.mediaSource
import io.element.android.libraries.mediaviewer.impl.gallery.thumbnailSource
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
class MediaGalleryRootNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val mediaViewerEntryPoint: MediaViewerEntryPoint
) : BaseFlowNode<MediaGalleryRootNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    overlay = Overlay(
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class MediaViewer(
            val eventId: EventId?,
            val mediaInfo: MediaInfo,
            val mediaSource: MediaSource,
            val thumbnailSource: MediaSource?,
        ) : NavTarget
    }

    private fun onBackClick() {
        plugins<MediaGalleryEntryPoint.Callback>().forEach {
            it.onBackClick()
        }
    }

    private fun onViewInTimeline(eventId: EventId) {
        plugins<MediaGalleryEntryPoint.Callback>().forEach {
            it.onViewInTimeline(eventId)
        }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : MediaGalleryNode.Callback {
                    override fun onBackClick() {
                        this@MediaGalleryRootNode.onBackClick()
                    }

                    override fun onViewInTimeline(eventId: EventId) {
                        this@MediaGalleryRootNode.onViewInTimeline(eventId)
                    }

                    override fun onItemClick(item: MediaItem.Event) {
                        overlay.show(
                            NavTarget.MediaViewer(
                                eventId = item.eventId(),
                                mediaInfo = item.mediaInfo(),
                                mediaSource = item.mediaSource(),
                                thumbnailSource = item.thumbnailSource(),
                            )
                        )
                    }
                }
                createNode<MediaGalleryNode>(buildContext = buildContext, plugins = listOf(callback))
            }
            is NavTarget.MediaViewer -> {
                val callback = object : MediaViewerEntryPoint.Callback {
                    override fun onDone() {
                        overlay.hide()
                    }

                    override fun onViewInTimeline(eventId: EventId) {
                        this@MediaGalleryRootNode.onViewInTimeline(eventId)
                    }
                }
                mediaViewerEntryPoint.nodeBuilder(this, buildContext)
                    .params(
                        MediaViewerEntryPoint.Params(
                            eventId = navTarget.eventId,
                            mediaInfo = navTarget.mediaInfo,
                            mediaSource = navTarget.mediaSource,
                            thumbnailSource = navTarget.thumbnailSource,
                            canShowInfo = true,
                        )
                    )
                    .callback(callback)
                    .build()
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackWithOverlayBox(modifier)
    }
}
