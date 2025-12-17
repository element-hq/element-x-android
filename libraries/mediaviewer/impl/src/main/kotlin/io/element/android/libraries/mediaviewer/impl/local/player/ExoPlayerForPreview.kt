/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress(
    "OVERRIDE_DEPRECATION",
    "RedundantNullableReturnType",
    "DEPRECATION",
)

package io.element.android.libraries.mediaviewer.impl.local.player

import android.annotation.SuppressLint
import android.media.AudioDeviceInfo
import android.os.Looper
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.AuxEffectInfo
import androidx.media3.common.DeviceInfo
import androidx.media3.common.Effect
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.PriorityTaskManager
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Clock
import androidx.media3.common.util.Size
import androidx.media3.exoplayer.DecoderCounters
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.PlayerMessage
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.ScrubbingModeParameters
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.analytics.AnalyticsCollector
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.image.ImageOutput
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ShuffleOrder
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.TrackSelectionArray
import androidx.media3.exoplayer.trackselection.TrackSelector
import androidx.media3.exoplayer.video.VideoFrameMetadataListener
import androidx.media3.exoplayer.video.spherical.CameraMotionListener
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage

@SuppressLint("UnsafeOptInUsageError")
@ExcludeFromCoverage
class ExoPlayerForPreview(
    private val isPlaying: Boolean = false,
) : ExoPlayer {
    override fun getApplicationLooper(): Looper = throw NotImplementedError()
    override fun addListener(listener: Player.Listener) {}
    override fun removeListener(listener: Player.Listener) {}
    override fun setMediaItems(mediaItems: MutableList<MediaItem>) {}
    override fun setMediaItems(mediaItems: MutableList<MediaItem>, resetPosition: Boolean) {}
    override fun setMediaItems(mediaItems: MutableList<MediaItem>, startIndex: Int, startPositionMs: Long) {}
    override fun setMediaItem(mediaItem: MediaItem) {}
    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) {}
    override fun setMediaItem(mediaItem: MediaItem, resetPosition: Boolean) {}
    override fun addMediaItem(mediaItem: MediaItem) {}
    override fun addMediaItem(index: Int, mediaItem: MediaItem) {}
    override fun addMediaItems(mediaItems: MutableList<MediaItem>) {}
    override fun addMediaItems(index: Int, mediaItems: MutableList<MediaItem>) {}
    override fun moveMediaItem(currentIndex: Int, newIndex: Int) {}
    override fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {}
    override fun replaceMediaItem(index: Int, mediaItem: MediaItem) {}
    override fun replaceMediaItems(fromIndex: Int, toIndex: Int, mediaItems: MutableList<MediaItem>) {}
    override fun removeMediaItem(index: Int) {}
    override fun removeMediaItems(fromIndex: Int, toIndex: Int) {}
    override fun clearMediaItems() {}
    override fun isCommandAvailable(command: Int): Boolean = throw NotImplementedError()
    override fun canAdvertiseSession(): Boolean = throw NotImplementedError()
    override fun getAvailableCommands(): Player.Commands = throw NotImplementedError()
    override fun prepare(mediaSource: MediaSource) {}
    override fun prepare(mediaSource: MediaSource, resetPosition: Boolean, resetState: Boolean) {}
    override fun prepare() {}
    override fun getPlaybackState(): Int = throw NotImplementedError()
    override fun getPlaybackSuppressionReason(): Int = throw NotImplementedError()
    override fun isPlaying() = isPlaying
    override fun getPlayerError(): ExoPlaybackException? = null
    override fun play() {}
    override fun pause() {}
    override fun setPlayWhenReady(playWhenReady: Boolean) {}
    override fun getPlayWhenReady(): Boolean = throw NotImplementedError()
    override fun setRepeatMode(repeatMode: Int) {}
    override fun getRepeatMode(): Int = throw NotImplementedError()
    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {}
    override fun getShuffleModeEnabled(): Boolean = throw NotImplementedError()
    override fun isLoading(): Boolean = throw NotImplementedError()
    override fun seekToDefaultPosition() {}
    override fun seekToDefaultPosition(mediaItemIndex: Int) {}
    override fun seekTo(positionMs: Long) {}
    override fun seekTo(mediaItemIndex: Int, positionMs: Long) {}
    override fun getSeekBackIncrement(): Long = throw NotImplementedError()
    override fun seekBack() {}
    override fun getSeekForwardIncrement(): Long = throw NotImplementedError()
    override fun seekForward() {}
    override fun hasPreviousMediaItem(): Boolean = throw NotImplementedError()
    override fun seekToPreviousMediaItem() {}
    override fun getMaxSeekToPreviousPosition(): Long = throw NotImplementedError()
    override fun seekToPrevious() {}
    override fun hasNextMediaItem(): Boolean = throw NotImplementedError()
    override fun seekToNextMediaItem() {}
    override fun seekToNext() {}
    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {}
    override fun setPlaybackSpeed(speed: Float) {}
    override fun getPlaybackParameters(): PlaybackParameters = throw NotImplementedError()
    override fun stop() {}
    override fun release() {}
    override fun getCurrentTracks(): Tracks = throw NotImplementedError()
    override fun getTrackSelectionParameters(): TrackSelectionParameters = throw NotImplementedError()
    override fun setTrackSelectionParameters(parameters: TrackSelectionParameters) {}
    override fun getMediaMetadata(): MediaMetadata = throw NotImplementedError()
    override fun getPlaylistMetadata(): MediaMetadata = throw NotImplementedError()
    override fun setPlaylistMetadata(mediaMetadata: MediaMetadata) {}
    override fun getCurrentManifest(): Any? = throw NotImplementedError()
    override fun getCurrentTimeline(): Timeline = throw NotImplementedError()
    override fun getCurrentPeriodIndex(): Int = throw NotImplementedError()
    override fun getCurrentWindowIndex(): Int = throw NotImplementedError()
    override fun getCurrentMediaItemIndex(): Int = throw NotImplementedError()
    override fun getNextWindowIndex(): Int = throw NotImplementedError()
    override fun getNextMediaItemIndex(): Int = throw NotImplementedError()
    override fun getPreviousWindowIndex(): Int = throw NotImplementedError()
    override fun getPreviousMediaItemIndex(): Int = throw NotImplementedError()
    override fun getCurrentMediaItem(): MediaItem? = throw NotImplementedError()
    override fun getMediaItemCount(): Int = throw NotImplementedError()
    override fun getMediaItemAt(index: Int): MediaItem = throw NotImplementedError()
    override fun getDuration(): Long = throw NotImplementedError()
    override fun getCurrentPosition(): Long = throw NotImplementedError()
    override fun getBufferedPosition(): Long = throw NotImplementedError()
    override fun getBufferedPercentage(): Int = throw NotImplementedError()
    override fun getTotalBufferedDuration(): Long = throw NotImplementedError()
    override fun isCurrentWindowDynamic(): Boolean = throw NotImplementedError()
    override fun isCurrentMediaItemDynamic(): Boolean = throw NotImplementedError()
    override fun isCurrentWindowLive(): Boolean = throw NotImplementedError()
    override fun isCurrentMediaItemLive(): Boolean = throw NotImplementedError()
    override fun getCurrentLiveOffset(): Long = throw NotImplementedError()
    override fun isCurrentWindowSeekable(): Boolean = throw NotImplementedError()
    override fun isCurrentMediaItemSeekable(): Boolean = throw NotImplementedError()
    override fun isPlayingAd(): Boolean = throw NotImplementedError()
    override fun getCurrentAdGroupIndex(): Int = throw NotImplementedError()
    override fun getCurrentAdIndexInAdGroup(): Int = throw NotImplementedError()
    override fun getContentDuration(): Long = throw NotImplementedError()
    override fun getContentPosition(): Long = throw NotImplementedError()
    override fun getContentBufferedPosition(): Long = throw NotImplementedError()
    override fun getAudioAttributes(): AudioAttributes = throw NotImplementedError()
    override fun setVolume(volume: Float) = throw NotImplementedError()
    override fun getVolume(): Float = throw NotImplementedError()
    override fun clearVideoSurface() {}
    override fun clearVideoSurface(surface: Surface?) {}
    override fun setVideoSurface(surface: Surface?) {}
    override fun setVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {}
    override fun clearVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {}
    override fun setVideoSurfaceView(surfaceView: SurfaceView?) {}
    override fun clearVideoSurfaceView(surfaceView: SurfaceView?) {}
    override fun setVideoTextureView(textureView: TextureView?) {}
    override fun clearVideoTextureView(textureView: TextureView?) {}
    override fun getVideoSize(): VideoSize = throw NotImplementedError()
    override fun getSurfaceSize(): Size = throw NotImplementedError()
    override fun getCurrentCues(): CueGroup = throw NotImplementedError()
    override fun getDeviceInfo(): DeviceInfo = throw NotImplementedError()
    override fun getDeviceVolume(): Int = throw NotImplementedError()
    override fun isDeviceMuted(): Boolean = throw NotImplementedError()
    override fun setDeviceVolume(volume: Int) {}
    override fun setDeviceVolume(volume: Int, flags: Int) {}
    override fun increaseDeviceVolume() {}
    override fun increaseDeviceVolume(flags: Int) {}
    override fun decreaseDeviceVolume() {}
    override fun decreaseDeviceVolume(flags: Int) {}
    override fun setDeviceMuted(muted: Boolean) {}
    override fun setDeviceMuted(muted: Boolean, flags: Int) {}
    override fun setAudioAttributes(audioAttributes: AudioAttributes, handleAudioFocus: Boolean) {}
    override fun addAudioOffloadListener(listener: ExoPlayer.AudioOffloadListener) {}
    override fun removeAudioOffloadListener(listener: ExoPlayer.AudioOffloadListener) {}
    override fun getAnalyticsCollector(): AnalyticsCollector = throw NotImplementedError()
    override fun addAnalyticsListener(listener: AnalyticsListener) {}
    override fun removeAnalyticsListener(listener: AnalyticsListener) {}
    override fun getRendererCount(): Int = throw NotImplementedError()
    override fun getRendererType(index: Int): Int = throw NotImplementedError()
    override fun getRenderer(index: Int): Renderer = throw NotImplementedError()
    override fun getTrackSelector(): TrackSelector? = throw NotImplementedError()
    override fun getCurrentTrackGroups(): TrackGroupArray = throw NotImplementedError()
    override fun getCurrentTrackSelections(): TrackSelectionArray = throw NotImplementedError()
    override fun getPlaybackLooper(): Looper = throw NotImplementedError()
    override fun getClock(): Clock = throw NotImplementedError()
    override fun setMediaSources(mediaSources: MutableList<MediaSource>) {}
    override fun setMediaSources(mediaSources: MutableList<MediaSource>, resetPosition: Boolean) {}
    override fun setMediaSources(mediaSources: MutableList<MediaSource>, startMediaItemIndex: Int, startPositionMs: Long) {}
    override fun setMediaSource(mediaSource: MediaSource) {}
    override fun setMediaSource(mediaSource: MediaSource, startPositionMs: Long) {}
    override fun setMediaSource(mediaSource: MediaSource, resetPosition: Boolean) {}
    override fun addMediaSource(mediaSource: MediaSource) {}
    override fun addMediaSource(index: Int, mediaSource: MediaSource) {}
    override fun addMediaSources(mediaSources: MutableList<MediaSource>) {}
    override fun addMediaSources(index: Int, mediaSources: MutableList<MediaSource>) {}
    override fun setShuffleOrder(shuffleOrder: ShuffleOrder) {}
    override fun getShuffleOrder(): ShuffleOrder = ShuffleOrder.DefaultShuffleOrder(0)
    override fun setPreloadConfiguration(preloadConfiguration: ExoPlayer.PreloadConfiguration) {}
    override fun getPreloadConfiguration(): ExoPlayer.PreloadConfiguration = throw NotImplementedError()
    override fun setAudioSessionId(audioSessionId: Int) {}
    override fun getAudioSessionId(): Int = throw NotImplementedError()
    override fun setAuxEffectInfo(auxEffectInfo: AuxEffectInfo) {}
    override fun clearAuxEffectInfo() {}
    override fun setPreferredAudioDevice(audioDeviceInfo: AudioDeviceInfo?) {}
    override fun setSkipSilenceEnabled(skipSilenceEnabled: Boolean) {}
    override fun getSkipSilenceEnabled(): Boolean = throw NotImplementedError()
    override fun setScrubbingModeEnabled(scrubbingModeEnabled: Boolean) {}
    override fun isScrubbingModeEnabled(): Boolean = false
    override fun setScrubbingModeParameters(scrubbingModeParameters: ScrubbingModeParameters) {}
    override fun getScrubbingModeParameters(): ScrubbingModeParameters = ScrubbingModeParameters.DEFAULT
    override fun setVideoEffects(videoEffects: MutableList<Effect>) {}
    override fun setVideoScalingMode(videoScalingMode: Int) {}
    override fun getVideoScalingMode(): Int = throw NotImplementedError()
    override fun setVideoChangeFrameRateStrategy(videoChangeFrameRateStrategy: Int) {}
    override fun getVideoChangeFrameRateStrategy(): Int = throw NotImplementedError()
    override fun setVideoFrameMetadataListener(listener: VideoFrameMetadataListener) {}
    override fun clearVideoFrameMetadataListener(listener: VideoFrameMetadataListener) {}
    override fun setCameraMotionListener(listener: CameraMotionListener) {}
    override fun clearCameraMotionListener(listener: CameraMotionListener) {}
    override fun createMessage(target: PlayerMessage.Target): PlayerMessage = throw NotImplementedError()
    override fun setSeekParameters(seekParameters: SeekParameters?) {}
    override fun getSeekParameters(): SeekParameters = throw NotImplementedError()
    override fun setForegroundMode(foregroundMode: Boolean) {}
    override fun setPauseAtEndOfMediaItems(pauseAtEndOfMediaItems: Boolean) {}
    override fun getPauseAtEndOfMediaItems(): Boolean = throw NotImplementedError()
    override fun getAudioFormat(): Format? = throw NotImplementedError()
    override fun getVideoFormat(): Format? = throw NotImplementedError()
    override fun getAudioDecoderCounters(): DecoderCounters? = throw NotImplementedError()
    override fun getVideoDecoderCounters(): DecoderCounters? = throw NotImplementedError()
    override fun setHandleAudioBecomingNoisy(handleAudioBecomingNoisy: Boolean) {}
    override fun setWakeMode(wakeMode: Int) {}
    override fun setPriority(priority: Int) {}
    override fun setPriorityTaskManager(priorityTaskManager: PriorityTaskManager?) {}
    override fun isSleepingForOffload(): Boolean = throw NotImplementedError()
    override fun isTunnelingEnabled(): Boolean = throw NotImplementedError()
    override fun isReleased(): Boolean = throw NotImplementedError()
    override fun setImageOutput(imageOutput: ImageOutput?) {}
}
