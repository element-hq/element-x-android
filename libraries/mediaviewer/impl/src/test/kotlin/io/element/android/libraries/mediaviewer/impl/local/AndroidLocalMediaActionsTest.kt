/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local

import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia
import io.element.android.tests.testutils.fake.registerFakeMediaStoreContentProvider
import io.element.android.tests.testutils.robolectric.RobolectricTest
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import java.io.File
import java.io.IOException

class AndroidLocalMediaActionsTest : RobolectricTest() {
    @Test
    fun `present - AndroidLocalMediaAction configure`() = runTest {
        val sut = createAndroidLocalMediaActions()
        moleculeFlow(RecompositionMode.Immediate) {
            CompositionLocalProvider(
                LocalContext provides RuntimeEnvironment.getApplication(),
                LocalActivityResultRegistryOwner provides NoOpActivityResultRegistryOwner()
            ) {
                sut.Configure()
            }
        }.test {
            awaitItem()
        }
    }

    @Test
    fun `test AndroidLocalMediaAction share`() = runTest {
        val sut = createAndroidLocalMediaActions()
        val result = sut.share(aLocalMedia(Uri.parse("file://afile")))
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `test AndroidLocalMediaAction open`() = runTest {
        val sut = createAndroidLocalMediaActions()
        val result = sut.open(aLocalMedia(Uri.parse("file://afile")))
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `test AndroidLocalMediaAction save on disk`() = runTest {
        val sut = createAndroidLocalMediaActions()
        val result = sut.saveOnDisk(aLocalMedia(Uri.parse("file://afile")))
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `test AndroidLocalMediaAction save on disk inserts the media under its own name`() = runTest {
        val mediaStore = registerFakeMediaStoreContentProvider()
        val sut = createAndroidLocalMediaActions()
        val result = sut.saveOnDisk(aLocalMedia(aMediaFile().toUri()))
        assertThat(result.exceptionOrNull()).isNull()
        assertThat(mediaStore.insertedDisplayNames).containsExactly("an image file.jpg")
    }

    @Test
    fun `test AndroidLocalMediaAction save on disk retries with a suffixed name when the name cannot be made unique`() = runTest {
        val mediaStore = registerFakeMediaStoreContentProvider().apply {
            conflictingDisplayNames = setOf("an image file.jpg")
        }
        val sut = createAndroidLocalMediaActions()
        val result = sut.saveOnDisk(aLocalMedia(aMediaFile().toUri()))
        assertThat(result.exceptionOrNull()).isNull()
        assertThat(mediaStore.insertedDisplayNames).hasSize(2)
        assertThat(mediaStore.insertedDisplayNames.last()).matches("an image file_\\d+\\.jpg")
    }

    @Test
    fun `test AndroidLocalMediaAction save on disk fails when the destination cannot be created`() = runTest {
        registerFakeMediaStoreContentProvider().apply {
            returnsNoUriOnInsert = true
        }
        val sut = createAndroidLocalMediaActions()
        val result = sut.saveOnDisk(aLocalMedia(aMediaFile().toUri()))
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
    }

    private fun aMediaFile(): File {
        return File(RuntimeEnvironment.getApplication().cacheDir, "an image file.jpg").apply {
            writeText("Some content")
        }
    }

    private fun TestScope.createAndroidLocalMediaActions() = AndroidLocalMediaActions(
        RuntimeEnvironment.getApplication(),
        testCoroutineDispatchers(),
        aBuildMeta()
    )
}
