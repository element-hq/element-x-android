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
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AndroidLocalMediaActionsTest {
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

    private fun TestScope.createAndroidLocalMediaActions() = AndroidLocalMediaActions(
        RuntimeEnvironment.getApplication(),
        testCoroutineDispatchers(),
        aBuildMeta()
    )
}
