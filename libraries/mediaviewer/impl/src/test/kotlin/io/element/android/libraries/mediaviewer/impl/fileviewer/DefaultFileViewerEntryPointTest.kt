/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.fileviewer

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.filesize.FakeFileSizeFormatter
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.mediaviewer.api.FileViewerEntryPoint
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaActions
import io.element.android.libraries.mediaviewer.test.util.FileExtensionExtractorWithoutValidation
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultFileViewerEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultFileViewerEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            FileViewerNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { nodeParams ->
                    FileViewerPresenter(
                        params = nodeParams,
                        context = mockk<Context>(relaxed = true),
                        coroutineDispatchers = testCoroutineDispatchers(),
                        fileSizeFormatter = FakeFileSizeFormatter(),
                        fileExtensionExtractor = FileExtensionExtractorWithoutValidation(),
                        localMediaActions = FakeLocalMediaActions(),
                    )
                },
                textFileViewer = { _, _ -> lambdaError() },
            )
        }
        val params = FileViewerEntryPoint.Params(
            filename = "a file.json",
            mimeType = MimeTypes.Json,
            content = "{}",
        )
        val callback = object : FileViewerEntryPoint.Callback {
            override fun onDone() = lambdaError()
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(FileViewerNode::class.java)
        assertThat(result.plugins).contains(params)
        assertThat(result.plugins).contains(callback)
    }
}
