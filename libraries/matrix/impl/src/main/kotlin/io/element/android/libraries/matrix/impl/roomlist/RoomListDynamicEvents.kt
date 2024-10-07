/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind

internal sealed interface RoomListDynamicEvents {
    data object Reset : RoomListDynamicEvents
    data object LoadMore : RoomListDynamicEvents
    data class SetFilter(val filter: RoomListEntriesDynamicFilterKind) : RoomListDynamicEvents
}
