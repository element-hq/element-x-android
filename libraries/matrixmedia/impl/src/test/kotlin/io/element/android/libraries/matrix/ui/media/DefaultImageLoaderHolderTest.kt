/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import androidx.test.platform.app.InstrumentationRegistry
import coil3.ImageLoader
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.libraries.sessionstorage.test.observer.FakeSessionObserver
import io.element.android.libraries.sessionstorage.test.observer.NoOpSessionObserver
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultImageLoaderHolderTest {
    @Test
    fun `get - returns the same ImageLoader for the same client`() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val lambda = lambdaRecorder<MatrixMediaLoader, ImageLoader> { ImageLoader.Builder(context).build() }

        val holder = createDefaultImageLoaderHolder(
            imageLoaderFactory = FakeImageLoaderFactory(
                newMatrixImageLoaderLambda = lambda,
            ),
        )
        val client = FakeMatrixClient()
        val imageLoader1 = holder.get(client)
        val imageLoader2 = holder.get(client)
        assert(imageLoader1 === imageLoader2)
        lambda.assertions()
            .isCalledOnce()
            .with(value(client.matrixMediaLoader))
    }

    @Test
    fun `when session is deleted, the image loader is deleted`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val lambda =
            lambdaRecorder<MatrixMediaLoader, ImageLoader> { ImageLoader.Builder(context).build() }
        val sessionObserver = FakeSessionObserver()
        val holder = DefaultImageLoaderHolder(
            imageLoaderFactory = FakeImageLoaderFactory(
                newMatrixImageLoaderLambda = lambda,
            ),
            sessionObserver = sessionObserver,
        )
        assertThat(sessionObserver.listeners.size).isEqualTo(1)
        val client = FakeMatrixClient()
        holder.get(client)
        sessionObserver.onSessionDeleted(client.sessionId.value)
        holder.get(client)
        lambda.assertions()
            .isCalledExactly(2)
    }

    @Test
    fun `when session is created, nothing happen`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val lambda =
            lambdaRecorder<MatrixMediaLoader, ImageLoader> { ImageLoader.Builder(context).build() }
        val sessionObserver = FakeSessionObserver()
        DefaultImageLoaderHolder(
            imageLoaderFactory = FakeImageLoaderFactory(
                newMatrixImageLoaderLambda = lambda,
            ),
            sessionObserver = sessionObserver,
        )
        assertThat(sessionObserver.listeners.size).isEqualTo(1)
        sessionObserver.onSessionCreated(A_SESSION_ID.value)
    }
}

private fun createDefaultImageLoaderHolder(
    imageLoaderFactory: ImageLoaderFactory = FakeImageLoaderFactory(),
    sessionObserver: SessionObserver = NoOpSessionObserver(),
) = DefaultImageLoaderHolder(
    imageLoaderFactory = imageLoaderFactory,
    sessionObserver = sessionObserver,
)
