/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.compound.theme.ForcedDarkElementTheme
import io.element.android.features.viewfolder.api.TextFileViewer
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.mediaviewer.api.local.LocalMediaFactory
import io.element.android.libraries.mediaviewer.impl.datasource.FocusedTimelineMediaGalleryDataSourceFactory
import io.element.android.libraries.mediaviewer.impl.datasource.TimelineMediaGalleryDataSource
import io.element.android.libraries.mediaviewer.impl.model.hasEvent
import io.element.android.services.toolbox.api.systemclock.SystemClock

@ContributesNode(RoomScope::class)
class MediaViewerNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: MediaViewerPresenter.Factory,
    timelineMediaGalleryDataSource: TimelineMediaGalleryDataSource,
    focusedTimelineMediaGalleryDataSourceFactory: FocusedTimelineMediaGalleryDataSourceFactory,
    mediaLoader: MatrixMediaLoader,
    localMediaFactory: LocalMediaFactory,
    coroutineDispatchers: CoroutineDispatchers,
    systemClock: SystemClock,
    pagerKeysHandler: PagerKeysHandler,
    private val textFileViewer: TextFileViewer,
    private val audioFocus: AudioFocus,
) : Node(buildContext, plugins = plugins),
    MediaViewerNavigator {
    private val inputs = inputs<MediaViewerEntryPoint.Params>()

    private fun onDone() {
        plugins<MediaViewerEntryPoint.Callback>().forEach {
            it.onDone()
        }
    }

    override fun onViewInTimelineClick(eventId: EventId) {
        plugins<MediaViewerEntryPoint.Callback>().forEach {
            it.onViewInTimeline(eventId)
        }
    }

    override fun onItemDeleted() {
        onDone()
    }

    private val mediaGallerySource = if (inputs.mode == MediaViewerEntryPoint.MediaViewerMode.SingleMedia) {
        SingleMediaGalleryDataSource.createFrom(inputs)
    } else {
        val eventId = inputs.eventId
        if (eventId == null) {
            // Should not happen
            timelineMediaGalleryDataSource
        } else {
            // Can we use a specific timeline?
            val timelineMode = when (val mode = inputs.mode) {
                is MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos -> mode.timelineMode
                is MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios -> mode.timelineMode
                else -> null
            }
            when (timelineMode) {
                null -> timelineMediaGalleryDataSource
                Timeline.Mode.LIVE,
                Timeline.Mode.FOCUSED_ON_EVENT -> {
                    // Does timelineMediaGalleryDataSource knows the eventId?
                    val lastData = timelineMediaGalleryDataSource.getLastData().dataOrNull()
                    val isEventKnown = lastData?.hasEvent(eventId) == true
                    if (isEventKnown) {
                        timelineMediaGalleryDataSource
                    } else {
                        focusedTimelineMediaGalleryDataSourceFactory.createFor(
                            eventId = eventId,
                            mediaItem = inputs.toMediaItem(),
                            onlyPinnedEvents = false,
                        )
                    }
                }
                Timeline.Mode.PINNED_EVENTS -> {
                    focusedTimelineMediaGalleryDataSourceFactory.createFor(
                        eventId = eventId,
                        mediaItem = inputs.toMediaItem(),
                        onlyPinnedEvents = true,
                    )
                }
                Timeline.Mode.MEDIA -> timelineMediaGalleryDataSource
            }
        }
    }

    private val presenter = presenterFactory.create(
        inputs = inputs,
        navigator = this,
        dataSource = MediaViewerDataSource(
            mode = inputs.mode,
            dispatcher = coroutineDispatchers.computation,
            galleryDataSource = mediaGallerySource,
            mediaLoader = mediaLoader,
            localMediaFactory = localMediaFactory,
            systemClock = systemClock,
            pagerKeysHandler = pagerKeysHandler,
        )
    )

    @Composable
    override fun View(modifier: Modifier) {
        ForcedDarkElementTheme {
            val state = presenter.present()
            MediaViewerView(
                state = state,
                textFileViewer = textFileViewer,
                modifier = modifier,
                audioFocus = audioFocus,
                onBackClick = ::onDone,
            )
        }
    }
}
