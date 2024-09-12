/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
