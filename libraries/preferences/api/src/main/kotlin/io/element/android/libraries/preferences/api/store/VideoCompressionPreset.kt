/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

/**
 * Video compression presets to use when processing videos before uploading them.
 */
enum class VideoCompressionPreset {
    /** High quality compression, suitable for high-resolution videos. */
    HIGH,

    /** Standard quality compression, suitable for most videos. */
    STANDARD,

    /** Low quality compression, suitable for low-resolution videos or when bandwidth is a concern. */
    LOW
}
