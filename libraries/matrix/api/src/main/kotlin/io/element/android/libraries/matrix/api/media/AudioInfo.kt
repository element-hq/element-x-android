/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

import kotlin.time.Duration

data class AudioInfo(
    val duration: Duration?,
    val size: Long?,
    val mimetype: String?,
)
