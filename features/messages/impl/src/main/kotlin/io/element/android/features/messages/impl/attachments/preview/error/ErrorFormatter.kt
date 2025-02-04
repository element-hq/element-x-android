/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview.error

import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.ui.strings.CommonStrings

fun sendAttachmentError(
    throwable: Throwable
): Int {
    return if (throwable is MediaPreProcessor.Failure) {
        CommonStrings.screen_media_upload_preview_error_failed_processing
    } else {
        CommonStrings.screen_media_upload_preview_error_failed_sending
    }
}
