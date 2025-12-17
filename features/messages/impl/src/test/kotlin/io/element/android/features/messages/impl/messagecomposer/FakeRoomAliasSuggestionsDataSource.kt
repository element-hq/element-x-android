/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import io.element.android.features.messages.impl.messagecomposer.suggestions.RoomAliasSuggestion
import io.element.android.features.messages.impl.messagecomposer.suggestions.RoomAliasSuggestionsDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRoomAliasSuggestionsDataSource(
    initialData: List<RoomAliasSuggestion> = emptyList()
) : RoomAliasSuggestionsDataSource {
    private val roomAliasSuggestions = MutableStateFlow(initialData)

    override fun getAllRoomAliasSuggestions(): Flow<List<RoomAliasSuggestion>> {
        return roomAliasSuggestions
    }

    fun emitRoomAliasSuggestions(newData: List<RoomAliasSuggestion>) {
        roomAliasSuggestions.value = newData
    }
}
