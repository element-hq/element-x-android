/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.file.TemporaryUriDeleter
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.ThumbnailInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfig
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import io.element.android.tests.testutils.fake.FakeTemporaryUriDeleter
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.time.Duration

@RunWith(RobolectricTestRunner::class)
class AndroidMediaPreProcessorTest {
    private suspend fun TestScope.process(
        asset: Asset,
        mediaOptimizationConfig: MediaOptimizationConfig,
        sdkIntVersion: Int = Build.VERSION_CODES.P,
        deleteOriginal: Boolean = false,
    ): MediaUploadInfo {
        val context = InstrumentationRegistry.getInstrumentation().context
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val sut = createAndroidMediaPreProcessor(
            context = context,
            sdkIntVersion = sdkIntVersion,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        val file = getFileFromAssets(context, asset.filename)
        val result = sut.process(
            uri = file.toUri(),
            mimeType = asset.mimeType,
            deleteOriginal = deleteOriginal,
            mediaOptimizationConfig = mediaOptimizationConfig,
        )
        val data = result.getOrThrow()
        assertThat(data.file.path).endsWith(asset.filename)
        deleteCallback.assertions().isCalledExactly(if (deleteOriginal) 0 else 1)
        return data
    }

    @Test
    @Ignore("Ignore now that min API for enterprise is 33")
    fun `test processing png`() = runTest {
        val mediaUploadInfo = process(
            asset = assetImagePng,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = true,
                videoCompressionPreset = VideoCompressionPreset.STANDARD,
            ),
        )
        val info = mediaUploadInfo as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNotNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = assetImagePng.height,
                width = assetImagePng.width,
                mimetype = assetImagePng.mimeType,
                size = 2_026_433,
                ThumbnailInfo(height = 25, width = 25, mimetype = MimeTypes.Png, size = 91),
                thumbnailSource = null,
                blurhash = "K00000fQfQfQfQfQfQfQfQ"
            )
        )
    }

    @Test
    fun `test processing png api Q`() = runTest {
        val mediaUploadInfo = process(
            asset = assetImagePng,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = true,
                videoCompressionPreset = VideoCompressionPreset.STANDARD,
            ),
            sdkIntVersion = Build.VERSION_CODES.Q,
        )
        val info = mediaUploadInfo as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = assetImagePng.height,
                width = assetImagePng.width,
                mimetype = assetImagePng.mimeType,
                size = 2_026_433,
                thumbnailInfo = null,
                thumbnailSource = null,
                blurhash = null,
            )
        )
    }

    @Test
    @Ignore("Ignore now that min API for enterprise is 33")
    fun `test processing png no compression`() = runTest {
        val mediaUploadInfo = process(
            asset = assetImagePng,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = false,
                videoCompressionPreset = VideoCompressionPreset.STANDARD,
            ),
        )
        val info = mediaUploadInfo as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNotNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = assetImagePng.height,
                width = assetImagePng.width,
                mimetype = assetImagePng.mimeType,
                size = assetImagePng.size,
                thumbnailInfo = ThumbnailInfo(height = 25, width = 25, mimetype = MimeTypes.Png, size = 91),
                thumbnailSource = null,
                blurhash = "K00000fQfQfQfQfQfQfQfQ",
            )
        )
    }

    @Test
    @Ignore("Ignore now that min API for enterprise is 33")
    fun `test processing png and delete`() = runTest {
        val mediaUploadInfo = process(
            asset = assetImagePng,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = false,
                videoCompressionPreset = VideoCompressionPreset.STANDARD,
            ),
            deleteOriginal = true,
        )
        val info = mediaUploadInfo as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNotNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = assetImagePng.height,
                width = assetImagePng.width,
                mimetype = assetImagePng.mimeType,
                size = assetImagePng.size,
                thumbnailInfo = ThumbnailInfo(height = 25, width = 25, mimetype = MimeTypes.Png, size = 91),
                thumbnailSource = null,
                blurhash = "K00000fQfQfQfQfQfQfQfQ",
            )
        )
        // Does not work
        // assertThat(file.exists()).isFalse()
    }

    @Test
    @Ignore("Ignore now that min API for enterprise is 33")
    fun `test processing jpeg`() = runTest {
        val mediaUploadInfo = process(
            asset = assetImageJpeg,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = true,
                videoCompressionPreset = VideoCompressionPreset.STANDARD,
            ),
        )
        val info = mediaUploadInfo as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNotNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = 979,
                width = 3006,
                mimetype = MimeTypes.Jpeg,
                size = 84_845,
                ThumbnailInfo(height = 244, width = 751, mimetype = MimeTypes.Jpeg, size = 7_178),
                thumbnailSource = null,
                blurhash = "K07gBzX=j_D4xZjoaSe,s:"
            )
        )
    }

    @Test
    fun `test processing jpeg api Q`() = runTest {
        val mediaUploadInfo = process(
            asset = assetImageJpeg,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = true,
                videoCompressionPreset = VideoCompressionPreset.STANDARD,
            ),
            sdkIntVersion = Build.VERSION_CODES.Q,
        )
        val info = mediaUploadInfo as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = 979,
                width = 3_006,
                mimetype = MimeTypes.Jpeg,
                size = 84_845,
                thumbnailInfo = null,
                thumbnailSource = null,
                blurhash = null,
            )
        )
    }

    @Test
    @Ignore("Ignore now that min API for enterprise is 33")
    fun `test processing jpeg no compression`() = runTest {
        val mediaUploadInfo = process(
            asset = assetImageJpeg,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = false,
                videoCompressionPreset = VideoCompressionPreset.STANDARD,
            ),
        )
        val info = mediaUploadInfo as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNotNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = assetImageJpeg.height,
                width = assetImageJpeg.width,
                mimetype = assetImageJpeg.mimeType,
                size = assetImageJpeg.size,
                thumbnailInfo = ThumbnailInfo(height = 6, width = 6, mimetype = MimeTypes.Jpeg, size = 631),
                thumbnailSource = null,
                blurhash = "K00000fQfQfQfQfQfQfQfQ",
            )
        )
    }

    @Test
    @Ignore("Ignore now that min API for enterprise is 33")
    fun `test processing jpeg and delete`() = runTest {
        val mediaUploadInfo = process(
            asset = assetImageJpeg,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = false,
                videoCompressionPreset = VideoCompressionPreset.STANDARD,
            ),
            deleteOriginal = true,
        )
        val info = mediaUploadInfo as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNotNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = assetImageJpeg.height,
                width = assetImageJpeg.width,
                mimetype = assetImageJpeg.mimeType,
                size = assetImageJpeg.size,
                thumbnailInfo = ThumbnailInfo(height = 6, width = 6, mimetype = MimeTypes.Jpeg, size = 631),
                thumbnailSource = null,
                blurhash = "K00000fQfQfQfQfQfQfQfQ",
            )
        )
        // Does not work
        // assertThat(file.exists()).isFalse()
    }

    @Test
    @Ignore("Ignore now that min API for enterprise is 33")
    fun `test processing gif`() = runTest {
        val mediaUploadInfo = process(
            asset = assetAnimatedGif,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = true,
                videoCompressionPreset = VideoCompressionPreset.STANDARD,
            ),
        )
        val info = mediaUploadInfo as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNotNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = assetAnimatedGif.height,
                width = assetAnimatedGif.width,
                mimetype = assetAnimatedGif.mimeType,
                size = assetAnimatedGif.size,
                thumbnailInfo = ThumbnailInfo(height = 50, width = 50, mimetype = MimeTypes.Jpeg, size = 691),
                thumbnailSource = null,
                blurhash = "K00000fQfQfQfQfQfQfQfQ",
            )
        )
    }

    @Test
    fun `test processing file`() = runTest {
        val mediaUploadInfo = process(
            asset = assetText,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = true,
                videoCompressionPreset = VideoCompressionPreset.STANDARD,
            ),
        )
        val info = mediaUploadInfo as MediaUploadInfo.AnyFile
        assertThat(info.fileInfo).isEqualTo(
            FileInfo(
                mimetype = assetText.mimeType,
                size = assetText.size,
                thumbnailInfo = null,
                thumbnailSource = null,
            )
        )
    }

    @Ignore("Compressing video is not working with Robolectric")
    @Test
    fun `test processing video`() = runTest {
        val mediaUploadInfo = process(
            asset = assetVideo,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = true,
                videoCompressionPreset = VideoCompressionPreset.STANDARD,
            ),
        )
        val info = mediaUploadInfo as MediaUploadInfo.Video
        assertThat(info.thumbnailFile).isNotNull()
        assertThat(info.videoInfo).isEqualTo(
            VideoInfo(
                // Not available with Robolectric?
                duration = Duration.ZERO,
                height = 1_178,
                width = 1_818,
                mimetype = MimeTypes.Mp4,
                size = 114_867,
                thumbnailInfo = null,
                thumbnailSource = null,
                blurhash = null,
            )
        )
    }

    @Ignore("Compressing video is not working with Robolectric")
    @Test
    fun `test processing video no compression`() = runTest {
        val mediaUploadInfo = process(
            asset = assetVideo,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = true,
                videoCompressionPreset = VideoCompressionPreset.HIGH,
            ),
        )
        val info = mediaUploadInfo as MediaUploadInfo.Video
        // Computing thumbnailFile is failing with Robolectric
        assertThat(info.thumbnailFile).isNull()
        assertThat(info.videoInfo).isEqualTo(
            VideoInfo(
                // Not available with Robolectric?
                duration = Duration.ZERO,
                // Not available with Robolectric?
                height = 0,
                // Not available with Robolectric?
                width = 0,
                mimetype = MimeTypes.Mp4,
                size = 1_673_712,
                thumbnailInfo = null,
                thumbnailSource = null,
                blurhash = null,
            )
        )
    }

    @Test
    fun `test processing audio`() = runTest {
        val mediaUploadInfo = process(
            asset = assetAudio,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = true,
                videoCompressionPreset = VideoCompressionPreset.STANDARD,
            ),
        )
        val info = mediaUploadInfo as MediaUploadInfo.Audio
        assertThat(info.audioInfo).isEqualTo(
            AudioInfo(
                // Not available with Robolectric?
                duration = Duration.ZERO,
                size = 52_079,
                mimetype = MimeTypes.Mp3,
            )
        )
    }

    @Test
    fun `test file which does not exist`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sut = createAndroidMediaPreProcessor(context)
        val file = File(context.cacheDir, "not found.txt")
        val result = sut.process(
            uri = file.toUri(),
            mimeType = MimeTypes.PlainText,
            deleteOriginal = false,
            mediaOptimizationConfig = MediaOptimizationConfig(
                compressImages = true,
                videoCompressionPreset = VideoCompressionPreset.STANDARD,
            ),
        )
        assertThat(result.isFailure).isTrue()
        val failure = result.exceptionOrNull()
        assertThat(failure).isInstanceOf(MediaPreProcessor.Failure::class.java)
        assertThat(failure?.cause).isInstanceOf(FileNotFoundException::class.java)
    }

    private fun TestScope.createAndroidMediaPreProcessor(
        context: Context,
        sdkIntVersion: Int = Build.VERSION_CODES.P,
        temporaryUriDeleter: TemporaryUriDeleter = FakeTemporaryUriDeleter(),
    ) = AndroidMediaPreProcessor(
        context = context,
        thumbnailFactory = ThumbnailFactory(context, FakeBuildVersionSdkIntProvider(sdkIntVersion)),
        imageCompressor = ImageCompressor(context, testCoroutineDispatchers()),
        videoCompressor = VideoCompressor(context),
        coroutineDispatchers = testCoroutineDispatchers(),
        temporaryUriDeleter = temporaryUriDeleter,
    )

    @Throws(IOException::class)
    private fun getFileFromAssets(context: Context, fileName: String): File = File(context.cacheDir, fileName)
        .also {
            if (!it.exists()) {
                it.outputStream().use { cache ->
                    context.assets.open(fileName).use { inputStream ->
                        inputStream.copyTo(cache)
                    }
                }
            }
        }
}
