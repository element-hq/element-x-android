/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.mxc.MxcTools
import io.element.android.libraries.matrix.test.media.FakeMatrixMediaLoader
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DefaultVoiceMessageMediaRepoTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `cache miss - downloads and returns cached file successfully`() = runTest {
        val matrixMediaLoader = FakeMatrixMediaLoader().apply {
            path = temporaryFolder.createRustMediaFile().path
        }
        val repo = createDefaultVoiceMessageMediaRepo(
            temporaryFolder = temporaryFolder,
            matrixMediaLoader = matrixMediaLoader,
        )

        repo.getMediaFile().let { result ->
            assertThat(result.isSuccess).isTrue()
            result.getOrThrow().let { file ->
                assertThat(file.path).isEqualTo(temporaryFolder.cachedFilePath)
                assertThat(file.exists()).isTrue()
            }
        }
    }

    @Test
    fun `cache miss - download fails`() = runTest {
        val matrixMediaLoader = FakeMatrixMediaLoader().apply {
            shouldFail = true
        }
        val repo = createDefaultVoiceMessageMediaRepo(
            temporaryFolder = temporaryFolder,
            matrixMediaLoader = matrixMediaLoader,
        )

        repo.getMediaFile().let { result ->
            assertThat(result.isFailure).isTrue()
            result.exceptionOrNull()!!.let { exception ->
                assertThat(exception).isInstanceOf(RuntimeException::class.java)
            }
        }
    }

    @Test
    fun `cache miss - download succeeds but file move fails`() = runTest {
        val matrixMediaLoader = FakeMatrixMediaLoader().apply {
            path = temporaryFolder.createRustMediaFile().path
        }
        File(temporaryFolder.cachedFilePath).apply {
            parentFile?.mkdirs()
            // Deny access to parent folder so move to cache will fail.
            parentFile?.setReadable(false)
            parentFile?.setWritable(false)
            parentFile?.setExecutable(false)
        }
        val repo = createDefaultVoiceMessageMediaRepo(
            temporaryFolder = temporaryFolder,
            matrixMediaLoader = matrixMediaLoader,
        )

        repo.getMediaFile().let { result ->
            assertThat(result.isFailure).isTrue()
            result.exceptionOrNull()?.let { exception ->
                assertThat(exception).apply {
                    isInstanceOf(IllegalStateException::class.java)
                    hasMessageThat().isEqualTo("Failed to move file to cache.")
                }
            }
        }
    }

    @Test
    fun `cache hit - returns cached file successfully`() = runTest {
        temporaryFolder.createCachedFile()
        val matrixMediaLoader = FakeMatrixMediaLoader().apply {
            shouldFail = true // so that if we hit the media loader it will crash
        }
        val repo = createDefaultVoiceMessageMediaRepo(
            temporaryFolder = temporaryFolder,
            matrixMediaLoader = matrixMediaLoader,
        )

        repo.getMediaFile().let { result ->
            assertThat(result.isSuccess).isTrue()
            result.getOrThrow().let { file ->
                assertThat(file.path).isEqualTo(temporaryFolder.cachedFilePath)
                assertThat(file.exists()).isTrue()
            }
        }
    }

    @Test
    fun `invalid mxc uri returns a failure`() = runTest {
        val repo = createDefaultVoiceMessageMediaRepo(
            temporaryFolder = temporaryFolder,
            mxcUri = INVALID_MXC_URI,
        )
        repo.getMediaFile().let { result ->
            assertThat(result.isFailure).isTrue()
            result.exceptionOrNull()!!.let { exception ->
                assertThat(exception).isInstanceOf(RuntimeException::class.java)
                assertThat(exception).hasMessageThat().isEqualTo("Invalid mxcUri.")
            }
        }
    }
}

private fun createDefaultVoiceMessageMediaRepo(
    temporaryFolder: TemporaryFolder,
    matrixMediaLoader: MatrixMediaLoader = FakeMatrixMediaLoader(),
    mxcUri: String = MXC_URI,
) = DefaultVoiceMessageMediaRepo(
    cacheDir = temporaryFolder.root,
    mxcTools = MxcTools(),
    matrixMediaLoader = matrixMediaLoader,
    mediaSource = MediaSource(
        url = mxcUri,
        json = null
    ),
    mimeType = MimeTypes.Ogg,
    filename = "someBody.ogg"
)

private const val MXC_URI = "mxc://matrix.org/1234567890abcdefg"
private const val INVALID_MXC_URI = "notAnMxcUri"
private val TemporaryFolder.cachedFilePath get() = "${this.root.path}/temp/voice/matrix.org/1234567890abcdefg"
private fun TemporaryFolder.createCachedFile() = File(cachedFilePath).apply {
    parentFile?.mkdirs()
    createNewFile()
}

private fun TemporaryFolder.createRustMediaFile() = File(this.root, "rustMediaFile.ogg").apply { createNewFile() }
