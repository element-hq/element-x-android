/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
package io.element.android.libraries.matrix.api.poll

enum class PollKind {
    /** Voters should see results as soon as they have voted. */
    Disclosed,

    /** Results should be only revealed when the poll is ended. */
    Undisclosed,
}

val PollKind.isDisclosed: Boolean
    get() = this == PollKind.Disclosed
