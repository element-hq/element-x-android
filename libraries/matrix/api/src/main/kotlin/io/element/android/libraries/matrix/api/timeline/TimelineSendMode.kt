/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline

import android.os.Parcelable
import io.element.android.libraries.matrix.api.core.ThreadId
import kotlinx.parcelize.Parcelize

sealed interface TimelineSendMode : Parcelable {
    @Parcelize
    data object Live : TimelineSendMode

    @Parcelize
    data class Thread(val threadRootId: ThreadId) : TimelineSendMode
}
