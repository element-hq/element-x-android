/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.poll

import io.element.android.libraries.matrix.api.poll.PollKind
import org.matrix.rustcomponents.sdk.PollKind as RustPollKind

fun RustPollKind.map(): PollKind = when (this) {
    RustPollKind.DISCLOSED -> PollKind.Disclosed
    RustPollKind.UNDISCLOSED -> PollKind.Undisclosed
}

fun PollKind.toInner(): RustPollKind = when (this) {
    PollKind.Disclosed -> RustPollKind.DISCLOSED
    PollKind.Undisclosed -> RustPollKind.UNDISCLOSED
}
