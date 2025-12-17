/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.media.FakeMatrixMediaLoader
import io.element.android.libraries.mediaplayer.test.FakeAudioFocus
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.mediaviewer.impl.datasource.createTimelineMediaGalleryDataSource
import io.element.android.libraries.mediaviewer.impl.viewer.MediaViewerNode
import io.element.android.libraries.mediaviewer.impl.viewer.PagerKeysHandler
import io.element.android.libraries.mediaviewer.impl.viewer.createMediaViewerEntryPointParams
import io.element.android.libraries.mediaviewer.impl.viewer.createMediaViewerPresenter
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaFactory
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultMediaViewerEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultMediaViewerEntryPoint()
        val mockMediaUri: Uri = mockk("localMediaUri")
        val localMediaFactory = FakeLocalMediaFactory(mockMediaUri)
        val parentNode = TestParentNode.create { buildContext, plugins ->
            MediaViewerNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { _, _, _ ->
                    createMediaViewerPresenter(
                        localMediaFactory = localMediaFactory,
                    )
                },
                timelineMediaGalleryDataSource = createTimelineMediaGalleryDataSource(),
                focusedTimelineMediaGalleryDataSourceFactory = { _, _, _ ->
                    lambdaError()
                },
                mediaLoader = FakeMatrixMediaLoader(),
                localMediaFactory = FakeLocalMediaFactory(mockMediaUri),
                coroutineDispatchers = testCoroutineDispatchers(),
                systemClock = FakeSystemClock(),
                pagerKeysHandler = PagerKeysHandler(),
                textFileViewer = { _, _ -> lambdaError() },
                audioFocus = FakeAudioFocus(),
                sessionId = A_SESSION_ID,
                enterpriseService = FakeEnterpriseService(),
            )
        }
        val callback = object : MediaViewerEntryPoint.Callback {
            override fun onDone() = lambdaError()
            override fun viewInTimeline(eventId: EventId) = lambdaError()
            override fun forwardEvent(eventId: EventId, fromPinnedEvents: Boolean) = lambdaError()
        }
        val params = createMediaViewerEntryPointParams()
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(MediaViewerNode::class.java)
        assertThat(result.plugins).contains(params)
        assertThat(result.plugins).contains(callback)
    }

    @Test
    fun `test node builder avatar`() = runTest {
        val entryPoint = DefaultMediaViewerEntryPoint()
        val mockMediaUri: Uri = mockk("localMediaUri")
        val localMediaFactory = FakeLocalMediaFactory(mockMediaUri)
        val parentNode = TestParentNode.create { buildContext, plugins ->
            MediaViewerNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { _, _, _ ->
                    createMediaViewerPresenter(
                        localMediaFactory = localMediaFactory,
                    )
                },
                timelineMediaGalleryDataSource = createTimelineMediaGalleryDataSource(),
                focusedTimelineMediaGalleryDataSourceFactory = { _, _, _ ->
                    lambdaError()
                },
                mediaLoader = FakeMatrixMediaLoader(),
                localMediaFactory = FakeLocalMediaFactory(mockMediaUri),
                coroutineDispatchers = testCoroutineDispatchers(),
                systemClock = FakeSystemClock(),
                pagerKeysHandler = PagerKeysHandler(),
                textFileViewer = { _, _ -> lambdaError() },
                audioFocus = FakeAudioFocus(),
                sessionId = A_SESSION_ID,
                enterpriseService = FakeEnterpriseService(),
            )
        }
        val callback = object : MediaViewerEntryPoint.Callback {
            override fun onDone() = lambdaError()
            override fun viewInTimeline(eventId: EventId) = lambdaError()
            override fun forwardEvent(eventId: EventId, fromPinnedEvents: Boolean) = lambdaError()
        }
        val params = entryPoint.createParamsForAvatar(
            filename = "fn",
            avatarUrl = "avatarUrl",
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(MediaViewerNode::class.java)
        assertThat(result.plugins).contains(
            MediaViewerEntryPoint.Params(
                mode = MediaViewerEntryPoint.MediaViewerMode.SingleMedia,
                eventId = null,
                mediaInfo = MediaInfo(
                    filename = "fn",
                    fileSize = null,
                    caption = null,
                    mimeType = MimeTypes.Images,
                    formattedFileSize = "",
                    fileExtension = "",
                    senderId = UserId("@dummy:server.org"),
                    senderName = null,
                    senderAvatar = null,
                    dateSent = null,
                    dateSentFull = null,
                    waveform = null,
                    duration = null,
                ),
                mediaSource = MediaSource(url = "avatarUrl"),
                thumbnailSource = null,
                canShowInfo = false,
            )
        )
        assertThat(result.plugins).contains(callback)
    }
}
