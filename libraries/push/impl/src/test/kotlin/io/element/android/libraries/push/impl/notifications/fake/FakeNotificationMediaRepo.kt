/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.fake

import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.push.impl.notifications.NotificationMediaRepo
import java.io.File

class FakeNotificationMediaRepo : NotificationMediaRepo {
    override suspend fun getMediaFile(
        mediaSource: MediaSource,
        mimeType: String?,
        filename: String?,
    ): Result<File> {
        return Result.failure(IllegalStateException("Fake class"))
    }
}
