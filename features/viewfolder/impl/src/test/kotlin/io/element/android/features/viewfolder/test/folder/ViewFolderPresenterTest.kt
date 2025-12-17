/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.test.folder

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.viewfolder.impl.folder.FolderExplorer
import io.element.android.features.viewfolder.impl.folder.ViewFolderPresenter
import io.element.android.features.viewfolder.impl.model.Item
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ViewFolderPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.title).isEqualTo("aPath")
            assertThat(initialState.content).isEmpty()
        }
    }

    @Test
    fun `present - title is built regarding the applicationId`() = runTest {
        val presenter = createPresenter(
            path = "/data/user/O/appId/cache/logs",
            buildMeta = aBuildMeta(
                applicationId = "appId",
            )
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.title).isEqualTo("…/cache/logs")
        }
    }

    @Test
    fun `present - list items from root`() = runTest {
        val items = listOf(
            Item.Folder("aFilePath", "aFilename"),
            Item.File("aFolderPath", "aFolderName", "aSize"),
        )
        val folderExplorer = FakeFolderExplorer().apply {
            givenResult(items)
        }
        val presenter = createPresenter(folderExplorer = folderExplorer)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.title).isEqualTo("aPath")
            assertThat(initialState.content.toList()).isEqualTo(items)
        }
    }

    @Test
    fun `present - list items from a folder`() = runTest {
        val items = listOf(
            Item.Folder("aFilePath", "aFilename"),
            Item.File("aFolderPath", "aFolderName", "aSize"),
        )
        val folderExplorer = FakeFolderExplorer().apply {
            givenResult(items)
        }
        val presenter = createPresenter(
            canGoUp = true,
            folderExplorer = folderExplorer
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.title).isEqualTo("aPath")
            assertThat(initialState.content.toList()).isEqualTo(listOf(Item.Parent) + items)
        }
    }

    private fun createPresenter(
        canGoUp: Boolean = false,
        path: String = "aPath",
        folderExplorer: FolderExplorer = FakeFolderExplorer(),
        buildMeta: BuildMeta = aBuildMeta(
            applicationId = "appId",
        ),
    ) = ViewFolderPresenter(
        path = path,
        canGoUp = canGoUp,
        folderExplorer = folderExplorer,
        buildMeta = buildMeta,
    )
}
