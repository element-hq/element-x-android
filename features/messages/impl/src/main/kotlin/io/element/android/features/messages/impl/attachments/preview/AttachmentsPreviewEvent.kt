/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import io.element.android.features.messages.impl.attachments.preview.imageeditor.NormalizedCropRect

sealed interface AttachmentsPreviewEvent {
    data object SendAttachment : AttachmentsPreviewEvent
    data object CancelAndDismiss : AttachmentsPreviewEvent
    data object CancelAndClearSendState : AttachmentsPreviewEvent
    data object OpenImageEditor : AttachmentsPreviewEvent
    data object CloseImageEditor : AttachmentsPreviewEvent
    data object RotateImage : AttachmentsPreviewEvent
    data object ApplyImageEdits : AttachmentsPreviewEvent
    data class UpdateImageCropRect(val cropRect: NormalizedCropRect) : AttachmentsPreviewEvent
    data object ClearImageEditError : AttachmentsPreviewEvent
}
