/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.test.attachments.video

import android.net.Uri
import android.util.Size
import io.element.android.features.messages.impl.attachments.video.VideoMetadataExtractor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class FakeVideoMetadataExtractor(
    private val sizeResult: Result<Size> = Result.success(Size(1, 1)),
    private val duration: Result<Duration> = Result.success(1.milliseconds),
) : VideoMetadataExtractor {
    override fun getSize(): Result<Size> = sizeResult

    override fun getDuration(): Result<Duration> = duration

    override fun close() = Unit
}

class FakeVideoMetadataExtractorFactory(
    private val fakeVideoMetadataExtractor: FakeVideoMetadataExtractor = FakeVideoMetadataExtractor(),
) : VideoMetadataExtractor.Factory {
    override fun create(uri: Uri): VideoMetadataExtractor {
        return fakeVideoMetadataExtractor
    }
}
