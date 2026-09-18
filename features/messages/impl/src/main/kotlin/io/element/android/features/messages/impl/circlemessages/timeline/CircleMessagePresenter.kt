/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.circlemessages.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.messages.impl.timeline.di.TimelineItemPresenterFactory
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemCircleContent
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AssistedInject
class CircleMessagePresenter(
    @Assisted private val content: TimelineItemCircleContent,
    private val circleMessagePlayer: CircleMessagePlayer,
    mediaRepoFactory: CircleMessageMediaRepo.Factory,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : Presenter<CircleMessageState> {
    @AssistedFactory
    fun interface Factory : TimelineItemPresenterFactory<TimelineItemCircleContent, CircleMessageState> {
        override fun create(content: TimelineItemCircleContent): CircleMessagePresenter
    }

    private val mediaRepo = mediaRepoFactory.create(
        mediaSource = content.mediaSource,
        mimeType = content.mimeType,
        filename = content.filename,
    )
    private val download = mutableStateOf<AsyncData<Unit>>(AsyncData.Uninitialized)
    private val mediaId = content.eventId?.value ?: content.mediaSource.safeUrl

    @Composable
    override fun present(): CircleMessageState {
        val playerState by circleMessagePlayer.state.collectAsState()
        val isThisMedia = playerState.mediaId == mediaId
        val isPlaying = isThisMedia && playerState.isPlaying
        val downloadState = download.value
        val savedPlayback = playerState.savedPlayback[mediaId]

        LaunchedEffect(isThisMedia, isPlaying) {
            if (!isThisMedia || !isPlaying) return@LaunchedEffect
            while (isActive) {
                circleMessagePlayer.tick()
                delay(16)
            }
        }

        val button = when {
            isPlaying -> CircleMessageState.Button.Pause
            downloadState is AsyncData.Loading -> CircleMessageState.Button.Downloading
            downloadState is AsyncData.Failure -> CircleMessageState.Button.Retry
            else -> CircleMessageState.Button.Play
        }
        val positionMs = when {
            isThisMedia -> playerState.currentPositionMs
            savedPlayback?.isEnded == true -> savedPlayback.durationMs ?: savedPlayback.positionMs
            else -> savedPlayback?.positionMs ?: 0L
        }
        val durationMs = when {
            isThisMedia -> playerState.durationMs?.takeIf { it > 0 } ?: content.duration.inWholeMilliseconds
            else -> savedPlayback?.durationMs?.takeIf { it > 0 } ?: content.duration.inWholeMilliseconds
        }
        val progress = if (durationMs <= 0) {
            0f
        } else {
            (positionMs / durationMs.toFloat()).coerceIn(0f, 1f)
        }
        val isExpanded = isPlaying
        val lastFrame = if (isThisMedia) playerState.lastFrame else savedPlayback?.lastFrame

        fun handleEvent(event: CircleMessageEvent) {
            when (event) {
                CircleMessageEvent.PlayPause -> {
                    val snapshot = circleMessagePlayer.state.value
                    if (snapshot.mediaId == mediaId && snapshot.isPlaying) {
                        circleMessagePlayer.pause()
                    } else {
                        sessionCoroutineScope.launch {
                            download.runUpdatingState {
                                mediaRepo.getMediaFile().flatMap { file ->
                                    runCatchingExceptions {
                                        circleMessagePlayer.play(mediaId, file)
                                    }
                                }
                            }
                        }
                    }
                }
                is CircleMessageEvent.BindPlayerView -> circleMessagePlayer.bindPlayerView(event.view)
                is CircleMessageEvent.UnbindPlayerView -> circleMessagePlayer.unbindPlayerView(event.view)
            }
        }

        return CircleMessageState(
            button = button,
            progress = progress,
            isPlaying = isPlaying,
            durationMs = durationMs,
            isExpanded = isExpanded,
            lastFrame = lastFrame,
            exoPlayer = if (isThisMedia && isPlaying) circleMessagePlayer.exoPlayer else null,
            eventSink = ::handleEvent,
        )
    }
}
