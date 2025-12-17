/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.api

import io.element.android.libraries.matrix.api.core.RoomId

/**
 * Value for the local current call.
 */
sealed interface CurrentCall {
    data object None : CurrentCall

    data class RoomCall(
        val roomId: RoomId,
    ) : CurrentCall

    data class ExternalUrl(
        val url: String,
    ) : CurrentCall
}
