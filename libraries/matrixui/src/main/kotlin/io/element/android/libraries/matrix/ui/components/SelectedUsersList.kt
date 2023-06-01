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

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.floor

@Composable
fun SelectedUsersList(
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

    val rowWidth by remember {
        derivedStateOf {
            lazyListState.layoutInfo.viewportSize.width - lazyListState.layoutInfo.beforeContentPadding
        }
    }

    LazyRow(
        state = lazyListState,
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = contentPadding,
    ) {
        itemsIndexed(selectedUsers.toList()) { index, matrixUser ->
            Layout(
                content = {
                    SelectedUser(
                        matrixUser = matrixUser,
                        onUserRemoved = onUserRemoved,
                    )
                },
                measurePolicy = { measurables, constraints ->
                    // Measures out extra space between each user to ensure that the last visible user is exactly half visible, giving an affordance that
                    // the user can scroll to see more.

                    val placeable = measurables.first().measure(constraints)
                    val isLastItem = index == selectedUsers.lastIndex
                    val width = if (isLastItem) {
                        // Don't add any padding on the final item
                        placeable.width
                    } else {
                        val minimumSpacing = 24.dp.toPx()
                        val userWidth = placeable.width + minimumSpacing
                        val maxVisibleUsers = rowWidth / userWidth
                        if (maxVisibleUsers >= selectedUsers.size) {
                            // If we can fit all the users in, don't do anything fancy
                            (placeable.width + minimumSpacing).toInt()
                        } else {
                            // Round down the number of visible users to end with a state where one is half visible
                            val targetFraction = (placeable.width / 2) / userWidth
                            val targetUsers = floor(maxVisibleUsers - targetFraction) + targetFraction

                            // Work out how much extra spacing we need to reduce the number of users that much, then split it evenly amongst the visible users
                            val extraSpacing = (maxVisibleUsers - targetUsers) * userWidth
                            val extraSpacingPerUser = (extraSpacing) / floor(targetUsers)

                            (placeable.width + minimumSpacing + extraSpacingPerUser).toInt()
                        }
                    }


                    layout(width, placeable.height) {
                        placeable.place(0, 0)
                    }
                }
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Two users that will be visible with no scrolling
        SelectedUsersList(
            selectedUsers = aMatrixUserList().take(2).toImmutableList(),
            modifier = Modifier
                .width(200.dp)
                .border(1.dp, Color.Red)
        )

        // Multiple users that don't fit, so will be spaced out per the measure policy
        for (i in 0..5) {
            SelectedUsersList(
                selectedUsers = aMatrixUserList().take(6).toImmutableList(),
                modifier = Modifier
                    .width((200 + (i * 20)).dp)
                    .border(1.dp, Color.Red)
            )
        }
    }
}
