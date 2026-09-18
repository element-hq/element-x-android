/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.circlerecorder.impl

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.appconfig.VoiceMessageConfig
import io.element.android.libraries.androidutils.bitmap.blur
import io.element.android.libraries.androidutils.bitmap.writeBitmap
import io.element.android.libraries.circlerecorder.api.CircleRecorder
import io.element.android.libraries.circlerecorder.api.CircleRecorderState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.di.CacheDirectory
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import io.element.android.libraries.core.mimetype.MimeTypes as ElementMimeTypes

@SingleIn(RoomScope::class)
@ContributesBinding(RoomScope::class)
class DefaultCircleRecorder(
    @ApplicationContext private val context: Context,
    @CacheDirectory private val cacheDir: File,
    private val dispatchers: CoroutineDispatchers,
    private val timeSource: TimeSource,
    @SessionCoroutineScope sessionCoroutineScope: CoroutineScope,
) : CircleRecorder {
    private val recorderScope = sessionCoroutineScope.childScope(dispatchers.main, "CircleRecorder-${UUID.randomUUID()}")
    private val recordingMutex = Mutex()

    private val _state = MutableStateFlow<CircleRecorderState>(CircleRecorderState.Idle)
    override val state: StateFlow<CircleRecorderState> = _state

    private val recorderExecutor = Executors.newSingleThreadExecutor()
    private val previewUseCase = Preview.Builder().build()
    private val videoCapture: VideoCapture<Recorder> = VideoCapture.withOutput(
        Recorder.Builder()
            .setQualitySelector(
                QualitySelector.fromOrderedList(
                    listOf(Quality.FHD, Quality.HD, Quality.SD),
                    FallbackStrategy.lowerQualityThan(Quality.FHD),
                )
            )
            .build()
    )

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewView: PreviewView? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var recording: Recording? = null
    private var recordingFinalized: CompletableDeferred<Unit>? = null
    private var outputFile: File? = null
    private val segments = mutableListOf<ConcatItem>()
    private var camera: Camera? = null
    private var linearZoom: Float = 0f
    private var useFrontCamera: Boolean = true
    private var isSwitchingCamera: Boolean = false
    private var startedAt: TimeMark? = null
    private var elapsedJob: Job? = null
    private var isBoundToLifecycle: Boolean = false

    override fun bindPreview(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
    ) {
        this.previewView = previewView
        this.lifecycleOwner = lifecycleOwner
        previewUseCase.surfaceProvider = previewView.surfaceProvider
        previewView.scaleX = if (useFrontCamera) -1f else 1f
        recorderScope.launch {
            recordingMutex.withLock {
                withContext(dispatchers.main) {
                    bindCamera(forceRebind = false)
                }
            }
        }
    }

    override fun setUseFrontCamera(useFrontCamera: Boolean) {
        if (this.useFrontCamera == useFrontCamera) return
        recorderScope.launch {
            recordingMutex.withLock {
                this@DefaultCircleRecorder.useFrontCamera = useFrontCamera
                withContext(dispatchers.main) {
                    val isRecording = _state.value is CircleRecorderState.Recording
                    if (isRecording) {
                        isSwitchingCamera = true
                        emitRecordingState()
                        val switchFrame = previewView?.bitmap
                        awaitStopRecording()
                        commitCurrentSegment()
                        commitSwitchStill(switchFrame)
                        outputFile = newSegmentFile()
                        linearZoom = 0f
                    }
                    previewView?.scaleX = if (useFrontCamera) -1f else 1f
                    bindCamera(forceRebind = true)
                    if (isRecording) {
                        isSwitchingCamera = false
                        awaitPreviewStreaming()
                        startActiveRecording()
                    }
                    emitRecordingState()
                }
            }
        }
    }

    override fun setLinearZoom(linearZoom: Float) {
        this.linearZoom = linearZoom.coerceIn(0f, 1f)
        camera?.cameraControl?.setLinearZoom(this.linearZoom)
    }

    @RequiresPermission(allOf = [Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO])
    override suspend fun startRecord() {
        recordingMutex.withLock {
            if (_state.value is CircleRecorderState.Recording) {
                Timber.w("Circle recorder is already recording, ignoring this start")
                return
            }
            deleteSegmentFiles()
            outputFile = newSegmentFile()
            startedAt = timeSource.markNow()
            isSwitchingCamera = false
            linearZoom = 0f
            emitRecordingState()
            withContext(dispatchers.main) {
                bindCamera(forceRebind = true)
            }
        }
        elapsedJob?.cancel()
        elapsedJob = recorderScope.launch {
            val startedAt = startedAt ?: return@launch
            while (_state.value is CircleRecorderState.Recording) {
                val elapsed = startedAt.elapsedNow()
                if (elapsed > VoiceMessageConfig.maxVoiceMessageDuration) {
                    Timber.w("Circle message time limit reached")
                    stopRecord(cancelled = false)
                    break
                }
                emitRecordingState(elapsedTime = elapsed)
                delay(100)
            }
        }
    }

    override suspend fun stopRecord(cancelled: Boolean) {
        elapsedJob?.cancel()
        elapsedJob = null
        withContext(NonCancellable) {
            recordingMutex.withLock {
                if (_state.value !is CircleRecorderState.Recording && recording == null && segments.isEmpty()) {
                    return@withLock
                }
                withContext(dispatchers.main) {
                    awaitStopRecording()
                    cameraProvider?.unbindAll()
                    isBoundToLifecycle = false
                }
                commitCurrentSegment()
                val duration = startedAt?.elapsedNow() ?: Duration.ZERO
                startedAt = null
                isSwitchingCamera = false
                if (cancelled || segments.isEmpty()) {
                    deleteSegmentFiles()
                    _state.emit(CircleRecorderState.Idle)
                    return@withLock
                }
                val file = concatenateSegments()
                if (file == null) {
                    deleteSegmentFiles()
                    _state.emit(CircleRecorderState.Idle)
                    return@withLock
                }
                outputFile = file
                _state.emit(
                    CircleRecorderState.Finished(
                        file = file,
                        mimeType = ElementMimeTypes.Mp4,
                        duration = duration,
                    )
                )
            }
        }
    }

    override suspend fun deleteRecording() {
        recordingMutex.withLock {
            deleteSegmentFiles()
            outputFile?.delete()
            outputFile = null
            _state.emit(CircleRecorderState.Idle)
        }
    }

    private fun emitRecordingState(elapsedTime: Duration? = null) {
        val current = _state.value as? CircleRecorderState.Recording
        _state.value = CircleRecorderState.Recording(
            elapsedTime = elapsedTime ?: current?.elapsedTime ?: Duration.ZERO,
            useFrontCamera = useFrontCamera,
            isSwitchingCamera = isSwitchingCamera,
        )
    }

    private fun newSegmentFile(): File {
        val outputDirectory = File(cacheDir, CACHE_SUBDIR).apply { mkdirs() }
        return File(outputDirectory, "${UUID.randomUUID()}.mp4")
    }

    private fun newStillFile(): File {
        val outputDirectory = File(cacheDir, CACHE_SUBDIR).apply { mkdirs() }
        return File(outputDirectory, "${UUID.randomUUID()}.jpg")
    }

    private fun commitCurrentSegment() {
        val file = outputFile
        outputFile = null
        if (file != null && file.exists() && file.length() > 0L) {
            segments.add(ConcatItem.Video(file))
        } else {
            file?.delete()
        }
    }

    private fun commitSwitchStill(frame: Bitmap?) {
        if (frame == null) return
        val stillFile = newStillFile()
        stillFile.writeBitmap(frame.blur(radius = 24f), Bitmap.CompressFormat.JPEG, 90)
        segments.add(ConcatItem.Still(stillFile))
    }

    private fun deleteSegmentFiles() {
        segments.forEach { it.file.delete() }
        segments.clear()
        outputFile?.delete()
        outputFile = null
    }

    @OptIn(UnstableApi::class)
    private suspend fun concatenateSegments(): File? {
        val items = segments.toList()
        if (items.isEmpty()) return null
        val onlyVideo = items.singleOrNull() as? ConcatItem.Video
        if (onlyVideo != null) {
            segments.clear()
            return onlyVideo.file
        }
        val output = newSegmentFile()
        if (exportConcat(items, output)) {
            segments.clear()
            items.forEach { it.file.delete() }
            return output
        }
        Timber.w("Retrying circle camera concat without still frames")
        output.delete()
        val videosOnly = items.filterIsInstance<ConcatItem.Video>()
        items.filterIsInstance<ConcatItem.Still>().forEach { it.file.delete() }
        if (videosOnly.isEmpty()) {
            segments.clear()
            return null
        }
        if (videosOnly.size == 1) {
            segments.clear()
            return videosOnly.first().file
        }
        val retryOutput = newSegmentFile()
        val retry = exportConcat(videosOnly, retryOutput)
        segments.clear()
        return if (retry) {
            videosOnly.forEach { it.file.delete() }
            retryOutput
        } else {
            Timber.e("Failed to concatenate circle camera segments")
            retryOutput.delete()
            videosOnly.dropLast(1).forEach { it.file.delete() }
            videosOnly.last().file
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun exportConcat(items: List<ConcatItem>, output: File): Boolean {
        val result = runCatching {
            withContext(dispatchers.main) {
                suspendCancellableCoroutine { continuation ->
                    val editedItems = items.map { item ->
                        val builder = EditedMediaItem.Builder(MediaItem.fromUri(Uri.fromFile(item.file)))
                        if (item is ConcatItem.Still) {
                            builder.setDurationUs(CAMERA_SWITCH_STILL_DURATION_US)
                        }
                        builder.build()
                    }
                    val sequence = EditedMediaItemSequence.Builder(
                        setOf(C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_VIDEO)
                    ).apply {
                        editedItems.forEach { addItem(it) }
                    }.build()
                    val transformer = Transformer.Builder(context)
                        .setVideoMimeType(MimeTypes.VIDEO_H264)
                        .setAudioMimeType(MimeTypes.AUDIO_AAC)
                        .addListener(object : Transformer.Listener {
                            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                                if (continuation.isActive) continuation.resume(Unit)
                            }

                            override fun onError(
                                composition: Composition,
                                exportResult: ExportResult,
                                exportException: ExportException,
                            ) {
                                if (continuation.isActive) continuation.resumeWithException(exportException)
                            }
                        })
                        .build()
                    transformer.start(Composition.Builder(sequence).build(), output.path)
                    continuation.invokeOnCancellation { transformer.cancel() }
                }
            }
        }
        if (result.isFailure) {
            Timber.e(result.exceptionOrNull(), "Circle camera concat export failed")
        }
        return result.isSuccess && output.exists() && output.length() > 0L
    }

    private suspend fun bindCamera(forceRebind: Boolean) {
        val lifecycleOwner = lifecycleOwner ?: return
        val previewView = previewView ?: return
        val provider = cameraProvider
        if (provider == null) {
            ProcessCameraProvider.getInstance(context).also { future ->
                future.addListener(
                    {
                        cameraProvider = future.get()
                        recorderScope.launch { bindCamera(forceRebind = true) }
                    },
                    ContextCompat.getMainExecutor(context),
                )
            }
            return
        }
        previewUseCase.surfaceProvider = previewView.surfaceProvider
        if (recording != null && !forceRebind) {
            return
        }
        if (recording != null) {
            awaitStopRecording()
        }
        if (isBoundToLifecycle && !forceRebind) {
            if (shouldStartRecording()) {
                startActiveRecording()
            }
            return
        }
        try {
            provider.unbindAll()
            isBoundToLifecycle = false
            camera = null
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(
                    if (useFrontCamera) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                )
                .build()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                previewUseCase,
                videoCapture,
            )
            camera?.cameraControl?.setLinearZoom(linearZoom)
            isBoundToLifecycle = true
            if (shouldStartRecording()) {
                awaitPreviewStreaming()
                startActiveRecording()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to bind circle camera")
        }
    }

    private fun shouldStartRecording(): Boolean {
        return _state.value is CircleRecorderState.Recording &&
            recording == null &&
            !isSwitchingCamera &&
            outputFile != null
    }

    private suspend fun awaitPreviewStreaming() {
        val view = previewView ?: return
        val liveData = view.previewStreamState
        if (liveData.value == PreviewView.StreamState.STREAMING) return
        withTimeoutOrNull(PREVIEW_STREAM_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val observer = object : Observer<PreviewView.StreamState> {
                    override fun onChanged(value: PreviewView.StreamState) {
                        if (value == PreviewView.StreamState.STREAMING && continuation.isActive) {
                            liveData.removeObserver(this)
                            continuation.resume(Unit)
                        }
                    }
                }
                liveData.observeForever(observer)
                continuation.invokeOnCancellation { liveData.removeObserver(observer) }
                if (liveData.value == PreviewView.StreamState.STREAMING && continuation.isActive) {
                    liveData.removeObserver(observer)
                    continuation.resume(Unit)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startActiveRecording() {
        val file = outputFile ?: return
        if (recording != null) {
            Timber.w("Circle recording already active, skipping start")
            return
        }
        val finalized = CompletableDeferred<Unit>()
        recordingFinalized = finalized
        try {
            recording = videoCapture.output
                .prepareRecording(context, FileOutputOptions.Builder(file).build())
                .withAudioEnabled()
                .start(recorderExecutor) { event ->
                    if (event is VideoRecordEvent.Finalize) {
                        recording = null
                        if (event.hasError()) {
                            Timber.e(event.cause, "Circle recording failed")
                        }
                        if (!finalized.isCompleted) {
                            finalized.complete(Unit)
                        }
                    }
                }
        } catch (e: IllegalStateException) {
            Timber.e(e, "Failed to start circle recording")
            recording = null
            if (!finalized.isCompleted) {
                finalized.complete(Unit)
            }
        }
    }

    private suspend fun awaitStopRecording() {
        val activeRecording = recording
        val finalized = recordingFinalized
        if (activeRecording == null) {
            finalized?.await()
            recordingFinalized = null
            return
        }
        if (finalized == null || finalized.isCompleted) {
            runCatching { activeRecording.stop() }
            recording = null
            recordingFinalized = null
            return
        }
        runCatching { activeRecording.stop() }
        finalized.await()
        recording = null
        recordingFinalized = null
    }
}

private const val CACHE_SUBDIR = "circle_recordings"
private const val CAMERA_SWITCH_STILL_DURATION_US = 400_000L
private const val PREVIEW_STREAM_TIMEOUT_MS = 1_500L

private sealed interface ConcatItem {
    val file: File

    data class Video(override val file: File) : ConcatItem
    data class Still(override val file: File) : ConcatItem
}
