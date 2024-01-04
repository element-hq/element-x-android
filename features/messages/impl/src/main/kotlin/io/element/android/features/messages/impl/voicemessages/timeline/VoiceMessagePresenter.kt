/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.voicemessages.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.multibindings.IntoMap
import io.element.android.features.messages.impl.timeline.di.TimelineItemEventContentKey
import io.element.android.features.messages.impl.timeline.di.TimelineItemPresenterFactory
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.voicemessages.VoiceMessageException
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.ui.utils.time.formatShort
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Module
@ContributesTo(RoomScope::class)
interface VoiceMessagePresenterModule {
    @Binds
    @IntoMap
    @TimelineItemEventContentKey(TimelineItemVoiceContent::class)
    fun bindVoiceMessagePresenterFactory(factory: VoiceMessagePresenter.Factory): TimelineItemPresenterFactory<*, *>
}

class VoiceMessagePresenter @AssistedInject constructor(
    voiceMessagePlayerFactory: VoiceMessagePlayer.Factory,
    private val analyticsService: AnalyticsService,
    private val scope: CoroutineScope,
    @Assisted private val content: TimelineItemVoiceContent,
) : Presenter<VoiceMessageState> {

    @AssistedFactory
    fun interface Factory : TimelineItemPresenterFactory<TimelineItemVoiceContent, VoiceMessageState> {
        override fun create(content: TimelineItemVoiceContent): VoiceMessagePresenter
    }

    private val player = voiceMessagePlayerFactory.create(
        eventId = content.eventId,
        mediaSource = content.mediaSource,
        mimeType = content.mimeType,
        body = content.body,
    )

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
                    content.eventId == null -> VoiceMessageState.Button.Disabled
                    playerState.isPlaying -> VoiceMessageState.Button.Pause
                    play.value is AsyncData.Loading -> VoiceMessageState.Button.Downloading
                    play.value is AsyncData.Failure -> VoiceMessageState.Button.Retry
                    else -> VoiceMessageState.Button.Play
                }
            }
        }
        val duration by remember {
            derivedStateOf { playerState.duration ?: content.duration.inWholeMilliseconds }
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

        fun eventSink(event: VoiceMessageEvents) {
            when (event) {
                is VoiceMessageEvents.PlayPause -> {
                    if (playerState.isPlaying) {
                        player.pause()
                    } else if (playerState.isReady) {
                        player.play()
                    } else {
                        scope.launch {
                            play.runUpdatingState(
                                errorTransform = {
                                    analyticsService.trackError(
                                        VoiceMessageException.PlayMessageError("Error while trying to play voice message", it)
                                    )
                                    it
                                },
                            ) {
                                player.prepare().apply {
                                    player.play()
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
            eventSink = { eventSink(it) },
        )
    }
}
