/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.location.LiveLocationShare

class LiveLocationShareComparator(private val currentUser: UserId) : Comparator<LiveLocationShare> {
    override fun compare(p0: LiveLocationShare, p1: LiveLocationShare): Int {
        val p0IsCurrentUser = p0.userId == currentUser
        val p1IsCurrentUser = p1.userId == currentUser
        if (p0IsCurrentUser != p1IsCurrentUser) return if (p0IsCurrentUser) -1 else 1
        return p1.startTimestamp.compareTo(p0.startTimestamp)
    }
}
