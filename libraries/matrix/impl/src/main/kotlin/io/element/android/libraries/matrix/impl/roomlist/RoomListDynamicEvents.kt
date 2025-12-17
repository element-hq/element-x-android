/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind

internal sealed interface RoomListDynamicEvents {
    data object Reset : RoomListDynamicEvents
    data object LoadMore : RoomListDynamicEvents
    data class SetFilter(val filter: RoomListEntriesDynamicFilterKind) : RoomListDynamicEvents
}
