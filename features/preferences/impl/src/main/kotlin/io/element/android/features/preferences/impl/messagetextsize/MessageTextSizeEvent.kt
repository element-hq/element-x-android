/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.messagetextsize

sealed interface MessageTextSizeEvent {
    /** The size the slider is on, which the sample follows while it is being moved. */
    data class SetTextSize(val sizeSp: Int) : MessageTextSizeEvent

    /** The slider has been let go, so the size it landed on can be stored. */
    data object CommitTextSize : MessageTextSizeEvent
}
