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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
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
import io.element.android.libraries.architecture.Async
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
    private val playerFactory: PlayerFactory,
    @Assisted private val content: TimelineItemVoiceContent,
) : Presenter<VoiceMessageState> {

    @AssistedFactory
    fun interface Factory : TimelineItemPresenterFactory<TimelineItemVoiceContent, VoiceMessageState> {
        override fun create(content: TimelineItemVoiceContent): VoiceMessagePresenter
    }

    @Composable
    override fun present(): VoiceMessageState {

        val scope = rememberCoroutineScope()

        var internalState: InternalState by remember { mutableStateOf(InternalState.UNINITIALIZED) }
        var player: Player? by remember { mutableStateOf(null) }
        var mediaFile: MediaFile? by remember { mutableStateOf(null) }
        var elapsedMillis: Long by remember { mutableLongStateOf(0L) }
        val asyncMediaFile: MutableState<Async<MediaFile>> = remember { mutableStateOf(Async.Uninitialized) }

        val listener = remember {
            object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    internalState = if (playing) InternalState.PLAYING
                    else InternalState.PAUSED
                }
            }
        }

        DisposableEffect(Unit) {
            Timber.d("ABAB Presenter composed")
            onDispose {
                player?.let {
                    it.removeListener(listener)
                    it.release()
                }
                mediaFile?.close()
                Timber.d("ABAB Presenter disposed")
            }
        }

        val progress: Float by remember { derivedStateOf { elapsedMillis.toProgress(content.duration) } }
        val durationMinutes = content.duration / 1000 / 60
        val durationSeconds = content.duration / 1000 % 60
        val elapsedMinutes by remember { derivedStateOf { elapsedMillis / 1000 / 60 } }
        val elapsedSeconds by remember { derivedStateOf { elapsedMillis / 1000 % 60 } }

        LaunchedEffect(internalState) {
            while (internalState == InternalState.PLAYING) {
                player?.let {
                    elapsedMillis = it.contentPosition
                }
                delay(100)
            }
        }

        fun pause() {
            player?.pause()
        }

        fun play() {
            player?.let {
                if (it.playbackState == Player.STATE_ENDED) it.seekTo(0)
                it.play()
            }
        }

        suspend fun createPlayerAndDownloadMedia() {
            internalState = InternalState.DOWNLOADING
            mediaLoader.downloadMediaFile(
                source = content.audioSource,
                mimeType = content.mimeType,
                body = content.body,
            ).onSuccess {
                mediaFile = it
                player = playerFactory.create()
                player?.addListener(listener)
                player?.setMediaItem(MediaItem.fromUri(it.path()))
                player?.prepare()
                play()
            }.onFailure {
                internalState = InternalState.DOWNLOAD_ERROR
            }
        }

        fun eventSink(event: VoiceMessageEvents) {
            when (event) {
                is VoiceMessageEvents.PlayPause -> scope.launch {
                    when (internalState) {
                        InternalState.UNINITIALIZED -> createPlayerAndDownloadMedia()
                        InternalState.DOWNLOADING -> {
                            // Do nothing. Ideally it should stop a very long download.
                        }
                        InternalState.DOWNLOAD_ERROR -> createPlayerAndDownloadMedia()
                        InternalState.PLAYING -> pause()
                        InternalState.PAUSED -> play()
                    }
                }
                is VoiceMessageEvents.Seek -> player?.let {
                    it.seekTo(event.progress.toElapsedMillis(content.duration))
                    elapsedMillis = it.contentPosition
                }
            }
        }

        return VoiceMessageState(
            isLoading = asyncMediaFile.value.isLoading(),
            isError = asyncMediaFile.value.isFailure(),
            isPlaying = internalState == InternalState.PLAYING,
            progress = progress,
            elapsed = if (internalState == InternalState.PLAYING) "%02d:%02d".format(elapsedMinutes, elapsedSeconds)
            else "%02d:%02d".format(durationMinutes, durationSeconds),
            eventSink = ::eventSink
        )
    }
}

private fun Long.toProgress(durationMillis: Long): Float {
    return this.toFloat() / durationMillis.toFloat()
}

private fun Float.toElapsedMillis(durationMillis: Long): Long {
    return (this * durationMillis).toLong()
}

private enum class InternalState {
    UNINITIALIZED,
    DOWNLOADING,
    DOWNLOAD_ERROR,
    PLAYING,
    PAUSED,
}
