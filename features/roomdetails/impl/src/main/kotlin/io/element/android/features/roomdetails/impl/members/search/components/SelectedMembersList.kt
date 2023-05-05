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

package io.element.android.features.roomdetails.impl.members.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.features.roomdetails.impl.members.search.aListOfSelectedUsers
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SelectedMembersList(
    selectedUsers: ImmutableList<MatrixUser>,
    modifier: Modifier = Modifier,
    autoScroll: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onUserRemoved: (MatrixUser) -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    if (autoScroll) {
        var currentSize by rememberSaveable { mutableStateOf(selectedUsers.size) }
        LaunchedEffect(selectedUsers.size) {
            val isItemAdded = selectedUsers.size > currentSize
            if (isItemAdded) {
                lazyListState.animateScrollToItem(selectedUsers.lastIndex)
            }
            currentSize = selectedUsers.size
        }
    }

    LazyRow(
        state = lazyListState,
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        items(selectedUsers.toList()) { matrixUser ->
            SelectedMember(
                matrixUser = matrixUser,
                onUserRemoved = onUserRemoved,
            )
        }
    }
}

@Preview
@Composable
internal fun SelectedUsersListLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun SelectedUsersListDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    SelectedMembersList(
        selectedUsers = aListOfSelectedUsers(),
    )
}
