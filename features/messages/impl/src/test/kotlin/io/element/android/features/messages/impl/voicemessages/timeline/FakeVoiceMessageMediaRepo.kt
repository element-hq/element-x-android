/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.timeline

import io.element.android.tests.testutils.simulateLongTask
import java.io.File

/**
 * A fake implementation of [VoiceMessageMediaRepo] for testing purposes.
 */
class FakeVoiceMessageMediaRepo : VoiceMessageMediaRepo {
    var shouldFail = false

    override suspend fun getMediaFile(): Result<File> = simulateLongTask {
        if (shouldFail) {
            Result.failure(IllegalStateException("Failed to get media file"))
        } else {
            Result.success(File(""))
        }
    }
}
