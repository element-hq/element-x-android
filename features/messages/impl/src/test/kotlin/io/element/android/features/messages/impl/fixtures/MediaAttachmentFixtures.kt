/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.fixtures

import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.libraries.mediaviewer.api.local.LocalMedia

fun aMediaAttachment(localMedia: LocalMedia, compressIfPossible: Boolean = true) = Attachment.Media(
    localMedia = localMedia,
    compressIfPossible = compressIfPossible,
)
