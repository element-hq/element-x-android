/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

/**
 * Represents the values for media preview settings.
 * - [On] means that media preview are enabled
 * - [Off] means that media preview are disabled
 * - [Private] means that media preview are enabled only for private chats.
 */
enum class MediaPreviewValue {
    On,
    Off,
    Private
}
