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

package io.element.android.features.messages.impl.timeline.voice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.multibindings.IntoMap
import io.element.android.features.messages.impl.timeline.TimelineItemEventContentKey
import io.element.android.features.messages.impl.timeline.TimelineItemPresenterFactory
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.voice.player.VoiceMessagePlayer
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@Module
@ContributesTo(RoomScope::class)
interface VoiceMessagePresenterModule {
    @Binds
    @IntoMap
    @TimelineItemEventContentKey(TimelineItemVoiceContent::class)
    fun bindVoiceMessagePresenterFactory(factory: VoiceMessagePresenter.Factory): TimelineItemPresenterFactory<*, *>
}

class VoiceMessagePresenter @AssistedInject constructor(
    private val mediaLoader: MatrixMediaLoader,
    private val voiceMessagePlayer: VoiceMessagePlayer,
    @Assisted private val content: TimelineItemVoiceContent,
) : Presenter<VoiceMessageState> {

    @AssistedFactory
    fun interface Factory : TimelineItemPresenterFactory<TimelineItemVoiceContent, VoiceMessageState> {
        override fun create(content: TimelineItemVoiceContent): VoiceMessagePresenter
    }

    @Composable
    override fun present(): VoiceMessageState {

        val scope = rememberCoroutineScope()
        val durationMinutes = remember { content.duration / 1000 / 60 }
        val durationSeconds = remember { content.duration / 1000 % 60 }
        val isPlaying by voiceMessagePlayer.isPlaying.collectAsState()

        var button by remember { mutableStateOf(VoiceMessageState.Button.Play) }
        var progress by remember { mutableStateOf(VoiceMessagePlayer.Progress.Zero) }
        var mediaFile: MediaFile? by remember { mutableStateOf(null) }

        DisposableEffect(Unit) {
            Timber.d("ABAB Presenter composed")
            onDispose {
                mediaFile?.close()
                Timber.d("ABAB Presenter disposed")
            }
        }

        LaunchedEffect(isPlaying) {
            Timber.d("ABAB Presenter isPlaying: $isPlaying")
            if (isPlaying) {
                button = VoiceMessageState.Button.Pause
                while (true) {
                    progress = voiceMessagePlayer.progress
                    delay(100)
                }
            } else {
                button = VoiceMessageState.Button.Play
            }
        }

        suspend fun downloadMediaAndPlay() {
            button = VoiceMessageState.Button.Downloading
            mediaLoader.downloadMediaFile(
                source = content.audioSource,
                mimeType = content.mimeType,
                body = content.body,
            ).onSuccess {
                mediaFile = it
                voiceMessagePlayer.playMediaUri(it.path())
            }.onFailure {
                button = VoiceMessageState.Button.Retry
            }
        }

        fun eventSink(event: VoiceMessageEvents) {
            when (event) {
                is VoiceMessageEvents.PlayPause -> scope.launch {
                    if (mediaFile == null) {
                        downloadMediaAndPlay()
                    } else {
                        if (!voiceMessagePlayer.isPlaying.value) {
                            voiceMessagePlayer.play()
                        } else {
                            voiceMessagePlayer.pause()
                        }
                    }
                }
                is VoiceMessageEvents.Seek -> {
                    voiceMessagePlayer.seekTo(event.percentage)
                    progress = voiceMessagePlayer.progress
                }
            }
        }

        return VoiceMessageState(
            button = button,
            progress = progress.percentage,
            elapsed = if (isPlaying) "%02d:%02d".format(progress.elapsedMinutes, progress.elapsedSeconds)
            else "%02d:%02d".format(durationMinutes, durationSeconds),
            eventSink = ::eventSink
        )
    }
}
