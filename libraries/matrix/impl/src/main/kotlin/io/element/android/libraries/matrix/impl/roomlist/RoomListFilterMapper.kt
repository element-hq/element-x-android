/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind.All
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind.Any
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind.Category
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind.DeduplicateVersions
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind.Favourite
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind.Invite
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind.NonLeft
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind.NonSpace
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind.None
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind.NormalizedMatchRoomName
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind.Space
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind.Unread
import org.matrix.rustcomponents.sdk.RoomListFilterCategory

/**
 * Mapper for converting RoomListFilter to Rust SDK filter kinds.
 */
internal object RoomListFilterMapper {

    /**
     * Base rust filters to always apply across all room lists.
     * These filters ensure we show:
     * - Non-space, non-left rooms (regular rooms user is part of)
     * - OR space invites (pending space invitations)
     * - With version deduplication enabled
     */
    private val RUST_BASE_FILTERS = listOf(
        Any(
            listOf(
                All(listOf(NonSpace, NonLeft)),
                All(listOf(Space, Invite)),
            )
        ),
        DeduplicateVersions
    )

    /**
     * Converts a RoomListFilter to a Rust SDK RoomListEntriesDynamicFilterKind.
     * Applies base filters along with the provided filter.
     */
    fun toRustFilter(filter: RoomListFilter): RoomListEntriesDynamicFilterKind {
        return All(RUST_BASE_FILTERS + mapFilter(filter))
    }

    /**
     * Maps a RoomListFilter to its Rust SDK equivalent.
     * This replaces the previous RoomListFilter.into() extension function.
     */
    private fun mapFilter(filter: RoomListFilter): RoomListEntriesDynamicFilterKind {
        return when (filter) {
            is RoomListFilter.All -> All(filters = filter.filters.map { mapFilter(it) })
            is RoomListFilter.Any -> Any(filters = filter.filters.map { mapFilter(it) })
            RoomListFilter.None -> None
            RoomListFilter.Category.Group -> Category(RoomListFilterCategory.GROUP)
            RoomListFilter.Category.People -> Category(RoomListFilterCategory.PEOPLE)
            RoomListFilter.Category.Space -> Space
            RoomListFilter.Favorite -> Favourite
            RoomListFilter.Unread -> Unread
            is RoomListFilter.NormalizedMatchRoomName -> NormalizedMatchRoomName(
                pattern = filter.pattern
            )
            RoomListFilter.Invite -> Invite
        }
    }
}
