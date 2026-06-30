/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.threads.list

import io.element.android.libraries.matrix.api.room.threads.ThreadListItem

data class ThreadListRowItem(
    val item: ThreadListItem,
    val rootEventText: String?,
    val latestEventText: String?,
    val formattedTimestamp: String,
)
