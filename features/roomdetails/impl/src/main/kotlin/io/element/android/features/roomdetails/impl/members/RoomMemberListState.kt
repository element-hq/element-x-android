/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.roomdetails.impl.members

import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.ImmutableList

data class RoomMemberListState(
    val roomMembers: Async<RoomMembers>,
    val searchQuery: String,
    val searchResults: RoomMemberSearchResultState,
    val isSearchActive: Boolean,
    val eventSink: (RoomMemberListEvents) -> Unit,
)

data class RoomMembers(
    val invited: ImmutableList<RoomMember>,
    val joined: ImmutableList<RoomMember>
)

sealed interface RoomMemberSearchResultState {
    /** No search results are available yet (e.g. because the user hasn't entered a (long enough) search term). */
    object NotSearching : RoomMemberSearchResultState

    /** The search has completed, but no results were found. */
    object NoResults : RoomMemberSearchResultState

    /** The search has completed, and some matching users were found. */
    data class Results(val results: ImmutableList<RoomMember>) : RoomMemberSearchResultState
}
