/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.root

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.architecture.BackstackWithOverlayBox
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.overlay.Overlay
import io.element.android.libraries.architecture.overlay.operation.hide
import io.element.android.libraries.architecture.overlay.operation.show
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaviewer.api.MediaGalleryEntryPoint
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.mediaviewer.impl.gallery.MediaGalleryNode
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.eventId
import io.element.android.libraries.mediaviewer.impl.model.mediaInfo
import io.element.android.libraries.mediaviewer.impl.model.mediaSource
import io.element.android.libraries.mediaviewer.impl.model.thumbnailSource
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
@AssistedInject
class MediaGalleryFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val mediaViewerEntryPoint: MediaViewerEntryPoint,
) : BaseFlowNode<MediaGalleryFlowNode.NavTarget>(
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
            val mode: MediaViewerEntryPoint.MediaViewerMode,
            val eventId: EventId?,
            val mediaInfo: MediaInfo,
            val mediaSource: MediaSource,
            val thumbnailSource: MediaSource?,
        ) : NavTarget
    }

    private val callback: MediaGalleryEntryPoint.Callback = callback()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : MediaGalleryNode.Callback {
                    override fun onBackClick() {
                        callback.onBackClick()
                    }

                    override fun viewInTimeline(eventId: EventId) {
                        callback.viewInTimeline(eventId)
                    }

                    override fun forward(eventId: EventId) {
                        callback.forward(eventId, fromPinnedEvents = false)
                    }

                    override fun showItem(item: MediaItem.Event) {
                        val mode = when (item) {
                            is MediaItem.Audio,
                            is MediaItem.Voice,
                            is MediaItem.File -> MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios(Timeline.Mode.Media)
                            is MediaItem.Image,
                            is MediaItem.Video -> MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(Timeline.Mode.Media)
                        }
                        overlay.show(
                            NavTarget.MediaViewer(
                                mode = mode,
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

                    override fun viewInTimeline(eventId: EventId) {
                        callback.viewInTimeline(eventId)
                    }

                    override fun forwardEvent(eventId: EventId, fromPinnedEvents: Boolean) {
                        // Need to go to the parent because of the overlay
                        callback.forward(eventId, fromPinnedEvents)
                    }
                }
                mediaViewerEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = MediaViewerEntryPoint.Params(
                        mode = navTarget.mode,
                        eventId = navTarget.eventId,
                        mediaInfo = navTarget.mediaInfo,
                        mediaSource = navTarget.mediaSource,
                        thumbnailSource = navTarget.thumbnailSource,
                        canShowInfo = true,
                    ),
                    callback = callback,
                )
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackWithOverlayBox(modifier)
    }
}
