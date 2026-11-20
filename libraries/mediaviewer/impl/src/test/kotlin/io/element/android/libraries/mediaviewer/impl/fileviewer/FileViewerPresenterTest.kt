/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.fileviewer

import android.content.ActivityNotFoundException
import androidx.core.net.toFile
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.filesize.FakeFileSizeFormatter
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.mediaviewer.api.FileViewerEntryPoint
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.impl.local.LocalMediaActions
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaActions
import io.element.android.libraries.mediaviewer.test.util.FileExtensionExtractorWithoutValidation
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.robolectric.RobolectricTest
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import io.element.android.libraries.androidutils.R as UtilsR

private const val A_FILENAME = "push_rules@alice_server.org.json"
private const val A_CONTENT = """{"global":{"override":[]}}"""

class FileViewerPresenterTest : RobolectricTest() {
    @Test
    fun `present - writes the content to a file and exposes it as a local media`() = runTest {
        val presenter = createFileViewerPresenter()
        presenter.test {
            assertThat(awaitItem().localMedia).isEqualTo(AsyncData.Uninitialized)
            consumeItemsUntilPredicate { it.localMedia.isSuccess() }.last().also { state ->
                assertThat(state.filename).isEqualTo(A_FILENAME)
                val localMedia = state.localMedia.dataOrNull()!!
                assertThat(localMedia.info.filename).isEqualTo(A_FILENAME)
                assertThat(localMedia.info.mimeType).isEqualTo(MimeTypes.Json)
                assertThat(localMedia.info.fileExtension).isEqualTo("json")
                val file = localMedia.uri.toFile()
                assertThat(file.name).isEqualTo(A_FILENAME)
                assertThat(file.readText()).isEqualTo(A_CONTENT)
            }
        }
    }

    @Test
    fun `present - SaveOnDisk event saves the file and posts a snackbar message`() = runTest {
        val saveOnDiskResult = lambdaRecorder<LocalMedia, Result<Unit>> { Result.success(Unit) }
        val presenter = createFileViewerPresenter(
            localMediaActions = FakeLocalMediaActions(saveOnDiskResult = saveOnDiskResult),
        )
        presenter.test {
            consumeItemsUntilPredicate { it.localMedia.isSuccess() }.last().eventSink(FileViewerEvent.SaveOnDisk)
            consumeItemsUntilPredicate { it.snackbarMessage != null }.last().also { state ->
                assertThat(state.snackbarMessage?.messageResId).isEqualTo(CommonStrings.common_file_saved_on_disk_android)
            }
            saveOnDiskResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - Share event failure posts a snackbar message`() = runTest {
        val shareResult = lambdaRecorder<LocalMedia, Result<Unit>> { Result.failure(ActivityNotFoundException()) }
        val presenter = createFileViewerPresenter(
            localMediaActions = FakeLocalMediaActions(shareResult = shareResult),
        )
        presenter.test {
            consumeItemsUntilPredicate { it.localMedia.isSuccess() }.last().eventSink(FileViewerEvent.Share)
            consumeItemsUntilPredicate { it.snackbarMessage != null }.last().also { state ->
                assertThat(state.snackbarMessage?.messageResId).isEqualTo(UtilsR.string.error_no_compatible_app_found)
            }
            shareResult.assertions().isCalledOnce()
        }
    }

    private fun TestScope.createFileViewerPresenter(
        params: FileViewerEntryPoint.Params = FileViewerEntryPoint.Params(
            filename = A_FILENAME,
            mimeType = MimeTypes.Json,
            content = A_CONTENT,
        ),
        localMediaActions: LocalMediaActions = FakeLocalMediaActions(),
    ) = FileViewerPresenter(
        params = params,
        context = RuntimeEnvironment.getApplication(),
        coroutineDispatchers = testCoroutineDispatchers(),
        fileSizeFormatter = FakeFileSizeFormatter(),
        fileExtensionExtractor = FileExtensionExtractorWithoutValidation(),
        localMediaActions = localMediaActions,
    )
}
