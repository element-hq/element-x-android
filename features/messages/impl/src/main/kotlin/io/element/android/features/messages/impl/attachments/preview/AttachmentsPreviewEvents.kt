/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.runtime.Immutable

@Immutable
sealed interface AttachmentsPreviewEvents {
    data object SendAttachment : AttachmentsPreviewEvents
    data object CancelAndDismiss : AttachmentsPreviewEvents
    data object CancelAndClearSendState : AttachmentsPreviewEvents
}
