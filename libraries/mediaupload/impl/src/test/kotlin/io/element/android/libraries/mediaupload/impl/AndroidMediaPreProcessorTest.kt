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

package io.element.android.libraries.mediaupload.impl

import android.content.Context
import android.os.Build
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.ThumbnailInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
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
    @Test
    fun `test processing image`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sut = createAndroidMediaPreProcessor(context)
        val file = getFileFromAssets(context, "image.png")
        val result = sut.process(
            uri = file.toUri(),
            mimeType = MimeTypes.Png,
            deleteOriginal = false,
            compressIfPossible = true,
        )
        val data = result.getOrThrow()
        assertThat(data.file.path).endsWith("image.png")
        val info = data as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNotNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = 1_178,
                width = 1_818,
                mimetype = MimeTypes.Png,
                size = 114_867,
                ThumbnailInfo(height = 294, width = 454, mimetype = "image/jpeg", size = 4567),
                thumbnailSource = null,
                blurhash = "K13]7q%zWC00R4of%\$baad"
            )
        )
        assertThat(file.exists()).isTrue()
    }

    @Test
    fun `test processing image api Q`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sut = createAndroidMediaPreProcessor(context, sdkIntVersion = Build.VERSION_CODES.Q)
        val file = getFileFromAssets(context, "image.png")
        val result = sut.process(
            uri = file.toUri(),
            mimeType = MimeTypes.Png,
            deleteOriginal = false,
            compressIfPossible = true,
        )
        val data = result.getOrThrow()
        assertThat(data.file.path).endsWith("image.png")
        val info = data as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = 1_178,
                width = 1_818,
                mimetype = MimeTypes.Png,
                size = 114_867,
                thumbnailInfo = null,
                thumbnailSource = null,
                blurhash = null,
            )
        )
        assertThat(file.exists()).isTrue()
    }

    @Test
    fun `test processing image no compression`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sut = createAndroidMediaPreProcessor(context)
        val file = getFileFromAssets(context, "image.png")
        val result = sut.process(
            uri = file.toUri(),
            mimeType = MimeTypes.Png,
            deleteOriginal = false,
            compressIfPossible = false,
        ).getOrThrow()
        assertThat(result.file.path).endsWith("image.png")
        val info = result as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNotNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = 1_178,
                width = 1_818,
                mimetype = MimeTypes.Png,
                size = 1_856_786,
                thumbnailInfo = ThumbnailInfo(height = 25, width = 25, mimetype = MimeTypes.Jpeg, size = 643),
                thumbnailSource = null,
                blurhash = "K00000fQfQfQfQfQfQfQfQ",
            )
        )
        assertThat(file.exists()).isTrue()
    }

    @Test
    fun `test processing image and delete`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sut = createAndroidMediaPreProcessor(context)
        val file = getFileFromAssets(context, "image.png")
        val result = sut.process(
            uri = file.toUri(),
            mimeType = MimeTypes.Png,
            deleteOriginal = true,
            compressIfPossible = false,
        ).getOrThrow()
        assertThat(result.file.path).endsWith("image.png")
        val info = result as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNotNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = 1_178,
                width = 1_818,
                mimetype = MimeTypes.Png,
                size = 1_856_786,
                thumbnailInfo = ThumbnailInfo(height = 25, width = 25, mimetype = MimeTypes.Jpeg, size = 643),
                thumbnailSource = null,
                blurhash = "K00000fQfQfQfQfQfQfQfQ",
            )
        )
        // Does not work
        // assertThat(file.exists()).isFalse()
    }

    @Test
    fun `test processing gif`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sut = createAndroidMediaPreProcessor(context)
        val file = getFileFromAssets(context, "animated_gif.gif")
        val result = sut.process(
            uri = file.toUri(),
            mimeType = MimeTypes.Gif,
            deleteOriginal = false,
            compressIfPossible = true,
        ).getOrThrow()
        assertThat(result.file.path).endsWith("animated_gif.gif")
        val info = result as MediaUploadInfo.Image
        assertThat(info.thumbnailFile).isNotNull()
        assertThat(info.imageInfo).isEqualTo(
            ImageInfo(
                height = 600,
                width = 800,
                mimetype = MimeTypes.Gif,
                size = 687_979,
                thumbnailInfo = ThumbnailInfo(height = 50, width = 50, mimetype = MimeTypes.Jpeg, size = 691),
                thumbnailSource = null,
                blurhash = "K00000fQfQfQfQfQfQfQfQ",
            )
        )
        assertThat(file.exists()).isTrue()
    }

    @Test
    fun `test processing file`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sut = createAndroidMediaPreProcessor(context)
        val file = getFileFromAssets(context, "text.txt")
        val result = sut.process(
            uri = file.toUri(),
            mimeType = MimeTypes.PlainText,
            deleteOriginal = false,
            compressIfPossible = true,
        ).getOrThrow()
        assertThat(result.file.path).endsWith("text.txt")
        val info = result as MediaUploadInfo.AnyFile
        assertThat(info.fileInfo).isEqualTo(
            FileInfo(
                mimetype = MimeTypes.PlainText,
                size = 13,
                thumbnailInfo = null,
                thumbnailSource = null,
            )
        )
        assertThat(file.exists()).isTrue()
    }

    @Ignore("Compressing video is not working with Robolectric")
    @Test
    fun `test processing video`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sut = createAndroidMediaPreProcessor(context)
        val file = getFileFromAssets(context, "video.mp4")
        val result = sut.process(
            uri = file.toUri(),
            mimeType = MimeTypes.Mp4,
            deleteOriginal = false,
            compressIfPossible = true,
        ).getOrThrow()
        assertThat(result.file.path).endsWith("video.mp4")
        val info = result as MediaUploadInfo.Video
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
        assertThat(file.exists()).isTrue()
    }

    @Test
    fun `test processing video no compression`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sut = createAndroidMediaPreProcessor(context)
        val file = getFileFromAssets(context, "video.mp4")
        val result = sut.process(
            uri = file.toUri(),
            mimeType = MimeTypes.Mp4,
            deleteOriginal = false,
            compressIfPossible = false,
        ).getOrThrow()
        assertThat(result.file.path).endsWith("video.mp4")
        val info = result as MediaUploadInfo.Video
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
        assertThat(file.exists()).isTrue()
    }

    @Test
    fun `test processing audio`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sut = createAndroidMediaPreProcessor(context)
        val file = getFileFromAssets(context, "sample3s.mp3")
        val result = sut.process(
            uri = file.toUri(),
            mimeType = MimeTypes.Mp3,
            deleteOriginal = false,
            compressIfPossible = true,
        ).getOrThrow()
        assertThat(result.file.path).endsWith("sample3s.mp3")
        val info = result as MediaUploadInfo.Audio
        assertThat(info.audioInfo).isEqualTo(
            AudioInfo(
                // Not available with Robolectric?
                duration = Duration.ZERO,
                size = 52_079,
                mimetype = MimeTypes.Mp3,
            )
        )
        assertThat(file.exists()).isTrue()
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
            compressIfPossible = true,
        )
        assertThat(result.isFailure).isTrue()
        val failure = result.exceptionOrNull()
        assertThat(failure).isInstanceOf(MediaPreProcessor.Failure::class.java)
        assertThat(failure?.cause).isInstanceOf(FileNotFoundException::class.java)
    }

    private fun TestScope.createAndroidMediaPreProcessor(
        context: Context,
        sdkIntVersion: Int = Build.VERSION_CODES.P
    ) = AndroidMediaPreProcessor(
        context = context,
        thumbnailFactory = ThumbnailFactory(context, FakeBuildVersionSdkIntProvider(sdkIntVersion)),
        imageCompressor = ImageCompressor(context, testCoroutineDispatchers()),
        videoCompressor = VideoCompressor(context),
        coroutineDispatchers = testCoroutineDispatchers(),
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
