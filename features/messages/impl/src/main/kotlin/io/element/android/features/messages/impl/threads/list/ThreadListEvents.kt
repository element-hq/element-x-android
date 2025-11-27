/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.threads.list

import io.element.android.libraries.matrix.api.core.ThreadId

sealed interface ThreadListEvents {
    data class OnThreadClick(val threadId: ThreadId) : ThreadListEvents
}
