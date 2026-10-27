/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.RoomListDynamicEntriesController
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind

class FakeFfiRoomListDynamicEntriesController : RoomListDynamicEntriesController(NoHandle) {
    val filters = mutableListOf<RoomListEntriesDynamicFilterKind>()
    var isDestroyed = false
        private set

    override fun setFilter(kind: RoomListEntriesDynamicFilterKind): Boolean {
        checkNotDestroyed()
        filters.add(kind)
        return true
    }

    override fun resetToOnePage() = checkNotDestroyed()

    override fun addOnePage() = checkNotDestroyed()

    override fun destroy() {
        isDestroyed = true
    }

    // Same failure as the generated bindings when a destroyed object is used
    private fun checkNotDestroyed() = check(!isDestroyed) { "RoomListDynamicEntriesController object has already been destroyed" }
}
