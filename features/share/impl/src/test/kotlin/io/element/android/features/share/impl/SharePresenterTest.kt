/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.share.impl

import android.content.Intent
import android.net.Uri
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SharePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createSharePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.shareAction.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - on room selected error then clear error`() = runTest {
        val presenter = createSharePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.shareAction.isUninitialized()).isTrue()
            presenter.onRoomSelected(listOf(A_ROOM_ID))
            assertThat(awaitItem().shareAction.isLoading()).isTrue()
            val failure = awaitItem()
            assertThat(failure.shareAction.isFailure()).isTrue()
            failure.eventSink.invoke(ShareEvents.ClearError)
            assertThat(awaitItem().shareAction.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - on room selected ok`() = runTest {
        val presenter = createSharePresenter(
            shareIntentHandler = FakeShareIntentHandler { _, _, _ -> true }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.shareAction.isUninitialized()).isTrue()
            presenter.onRoomSelected(listOf(A_ROOM_ID))
            assertThat(awaitItem().shareAction.isLoading()).isTrue()
            val success = awaitItem()
            assertThat(success.shareAction.isSuccess()).isTrue()
            assertThat(success.shareAction).isEqualTo(AsyncAction.Success(listOf(A_ROOM_ID)))
        }
    }

    @Test
    fun `present - send text ok`() = runTest {
        val matrixRoom = FakeMatrixRoom(
            sendMessageResult = { _, _, _ -> Result.success(Unit) },
        )
        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, matrixRoom)
        }
        val presenter = createSharePresenter(
            matrixClient = matrixClient,
            shareIntentHandler = FakeShareIntentHandler { _, _, onText ->
                onText(A_MESSAGE)
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.shareAction.isUninitialized()).isTrue()
            presenter.onRoomSelected(listOf(A_ROOM_ID))
            assertThat(awaitItem().shareAction.isLoading()).isTrue()
            val success = awaitItem()
            assertThat(success.shareAction.isSuccess()).isTrue()
            assertThat(success.shareAction).isEqualTo(AsyncAction.Success(listOf(A_ROOM_ID)))
        }
    }

    @Test
    fun `present - send media ok`() = runTest {
        val matrixRoom = FakeMatrixRoom(
            sendMediaResult = { Result.success(FakeMediaUploadHandler()) },
        )
        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, matrixRoom)
        }
        val presenter = createSharePresenter(
            matrixClient = matrixClient,
            shareIntentHandler = FakeShareIntentHandler { _, onFile, _ ->
                onFile(
                    listOf(
                        ShareIntentHandler.UriToShare(
                            uri = Uri.parse("content://image.jpg"),
                            mimeType = MimeTypes.Jpeg,
                        )
                    )
                )
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.shareAction.isUninitialized()).isTrue()
            presenter.onRoomSelected(listOf(A_ROOM_ID))
            assertThat(awaitItem().shareAction.isLoading()).isTrue()
            val success = awaitItem()
            assertThat(success.shareAction.isSuccess()).isTrue()
            assertThat(success.shareAction).isEqualTo(AsyncAction.Success(listOf(A_ROOM_ID)))
        }
    }

    private fun TestScope.createSharePresenter(
        intent: Intent = Intent(),
        shareIntentHandler: ShareIntentHandler = FakeShareIntentHandler(),
        matrixClient: MatrixClient = FakeMatrixClient(),
        mediaPreProcessor: MediaPreProcessor = FakeMediaPreProcessor()
    ): SharePresenter {
        return SharePresenter(
            intent = intent,
            appCoroutineScope = this,
            shareIntentHandler = shareIntentHandler,
            matrixClient = matrixClient,
            mediaPreProcessor = mediaPreProcessor
        )
    }
}
