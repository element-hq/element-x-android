/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.ui.utils.time.formatShort
import io.element.android.libraries.voiceplayer.api.VoiceMessageEvents
import io.element.android.libraries.voiceplayer.api.VoiceMessageException
import io.element.android.libraries.voiceplayer.api.VoiceMessageState
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class VoiceMessagePresenter(
    private val analyticsService: AnalyticsService,
    private val sessionCoroutineScope: CoroutineScope,
    private val player: VoiceMessagePlayer,
    private val eventId: EventId?,
    private val duration: Duration,
) : Presenter<VoiceMessageState> {
    private val play = mutableStateOf<AsyncData<Unit>>(AsyncData.Uninitialized)

    @Composable
    override fun present(): VoiceMessageState {
        val playerState by player.state.collectAsState(
            VoiceMessagePlayer.State(
                isReady = false,
                isPlaying = false,
                isEnded = false,
                currentPosition = 0L,
                duration = null
            )
        )

        val button by remember {
            derivedStateOf {
                when {
                    eventId == null -> VoiceMessageState.Button.Disabled
                    playerState.isPlaying -> VoiceMessageState.Button.Pause
                    play.value is AsyncData.Loading -> VoiceMessageState.Button.Downloading
                    play.value is AsyncData.Failure -> VoiceMessageState.Button.Retry
                    else -> VoiceMessageState.Button.Play
                }
            }
        }
        val duration by remember {
            derivedStateOf { playerState.duration ?: duration.inWholeMilliseconds }
        }
        val progress by remember {
            derivedStateOf {
                playerState.currentPosition / duration.toFloat()
            }
        }
        val time by remember {
            derivedStateOf {
                when {
                    playerState.isReady && !playerState.isEnded -> playerState.currentPosition
                    playerState.currentPosition > 0 -> playerState.currentPosition
                    else -> duration
                }.milliseconds.formatShort()
            }
        }
        val showCursor by remember {
            derivedStateOf {
                !play.value.isUninitialized() && !playerState.isEnded
            }
        }

        fun handleEvent(event: VoiceMessageEvents) {
            when (event) {
                is VoiceMessageEvents.PlayPause -> {
                    if (playerState.isPlaying) {
                        player.pause()
                    } else if (playerState.isReady) {
                        player.play()
                    } else {
                        sessionCoroutineScope.launch {
                            play.runUpdatingState(
                                errorTransform = {
                                    analyticsService.trackError(
                                        VoiceMessageException.PlayMessageError("Error while trying to play voice message", it)
                                    )
                                    it
                                },
                            ) {
                                player.prepare().flatMap {
                                    runCatchingExceptions { player.play() }
                                }
                            }
                        }
                    }
                }
                is VoiceMessageEvents.Seek -> {
                    player.seekTo((event.percentage * duration).toLong())
                }
            }
        }

        return VoiceMessageState(
            button = button,
            progress = progress,
            time = time,
            showCursor = showCursor,
            eventSink = ::handleEvent,
        )
    }
}
