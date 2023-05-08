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

package io.element.android.features.roomdetails.impl.members.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.roomdetails.impl.members.RoomMemberListEvents
import io.element.android.features.roomdetails.impl.members.RoomMemberListState
import io.element.android.features.roomdetails.impl.members.RoomMemberListStateProvider
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight

@Composable
fun RoomMemberListView(
    state: RoomMemberListState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        RoomMemberSearchBar(
            modifier = Modifier.fillMaxWidth(),
            query = state.searchQuery,
            state = state.searchResults,
            active = state.isSearchActive,
            onActiveChanged = { state.eventSink(RoomMemberListEvents.OnSearchActiveChanged(it)) },
            onTextChanged = { state.eventSink(RoomMemberListEvents.UpdateSearchQuery(it)) },
        )
    }
}

@Preview
@Composable
internal fun UserListViewLightPreview(@PreviewParameter(RoomMemberListStateProvider::class) state: RoomMemberListState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun UserListViewDarkPreview(@PreviewParameter(RoomMemberListStateProvider::class) state: RoomMemberListState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: RoomMemberListState) {
    RoomMemberListView(state = state)
}
