/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.create

internal sealed class CreatePollException : Exception() {
    data class GetPollFailed(
        override val message: String?,
        override val cause: Throwable?
    ) : CreatePollException()

    data class SavePollFailed(
        override val message: String?,
        override val cause: Throwable?
    ) : CreatePollException()
}
