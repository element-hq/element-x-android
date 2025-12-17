/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.test.file

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.viewfolder.impl.file.ColorationMode
import io.element.android.features.viewfolder.impl.file.FileContentReader
import io.element.android.features.viewfolder.impl.file.FileSave
import io.element.android.features.viewfolder.impl.file.FileShare
import io.element.android.features.viewfolder.impl.file.ViewFileEvents
import io.element.android.features.viewfolder.impl.file.ViewFilePresenter
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ViewFilePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val fileContentReader = FakeFileContentReader().apply {
            givenResult(Result.success(listOf("aLine")))
        }
        val presenter = createPresenter(fileContentReader = fileContentReader)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.name).isEqualTo("aName")
            assertThat(initialState.lines).isInstanceOf(AsyncData.Loading::class.java)
            assertThat(initialState.colorationMode).isEqualTo(ColorationMode.None)
            val loadedState = awaitItem()
            val lines = (loadedState.lines as AsyncData.Success).data
            assertThat(lines.size).isEqualTo(1)
            assertThat(lines.first()).isEqualTo("aLine")
        }
    }

    @Test
    fun `present - coloration mode for logcat`() = runTest {
        val presenter = createPresenter(name = "logcat.log")
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.colorationMode).isEqualTo(ColorationMode.Logcat)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `present - coloration mode for logs`() = runTest {
        val presenter = createPresenter(name = "logs.date")
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.colorationMode).isEqualTo(ColorationMode.RustLogs)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `present - share should not have any side effect`() = runTest {
        val fileContentReader = FakeFileContentReader().apply {
            givenResult(Result.success(listOf("aLine")))
        }
        val fileShare = FakeFileShare()
        val fileSave = FakeFileSave()
        val presenter = createPresenter(fileContentReader = fileContentReader, fileShare = fileShare, fileSave = fileSave)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink(ViewFileEvents.Share)
            assertThat(fileShare.hasBeenCalled).isTrue()
            assertThat(fileSave.hasBeenCalled).isFalse()
        }
    }

    @Test
    fun `present - with error loading file`() = runTest {
        val fileContentReader = FakeFileContentReader().apply {
            givenResult(Result.failure(AN_EXCEPTION))
        }
        val presenter = createPresenter(fileContentReader = fileContentReader)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val errorState = awaitItem()
            assertThat(errorState.lines).isInstanceOf(AsyncData.Failure::class.java)
        }
    }

    @Test
    fun `present - save should not have any side effect`() = runTest {
        val fileContentReader = FakeFileContentReader().apply {
            givenResult(Result.success(listOf("aLine")))
        }
        val fileShare = FakeFileShare()
        val fileSave = FakeFileSave()
        val presenter = createPresenter(fileContentReader = fileContentReader, fileShare = fileShare, fileSave = fileSave)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink(ViewFileEvents.SaveOnDisk)
            assertThat(fileShare.hasBeenCalled).isFalse()
            assertThat(fileSave.hasBeenCalled).isTrue()
        }
    }

    private fun createPresenter(
        path: String = "aPath",
        name: String = "aName",
        fileContentReader: FileContentReader = FakeFileContentReader(),
        fileShare: FileShare = FakeFileShare(),
        fileSave: FileSave = FakeFileSave(),
    ) = ViewFilePresenter(
        path = path,
        name = name,
        fileContentReader = fileContentReader,
        fileShare = fileShare,
        fileSave = fileSave,
    )
}
