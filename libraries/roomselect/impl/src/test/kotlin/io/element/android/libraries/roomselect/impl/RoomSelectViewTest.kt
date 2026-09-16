/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.libraries.roomselect.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.components.aSelectRoomInfo
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.robolectric.annotation.Config

class RoomSelectViewTest : RobolectricTest() {
    @Config(qualifiers = "h1024dp")
    @Test
    fun `a room with a canonical alias renders the alias`() = runAndroidComposeUiTest {
        setRoomSelectView(
            aRoomSelectState(
                resultState = SearchBarResultState.Results(aRoomSelectRoomList()),
            ),
        )
        onNodeWithText("Room with alias").assertIsDisplayed()
        onNodeWithText("#alias:example.org").assertIsDisplayed()
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `a direct message without alias renders the matrix id of the other user`() = runAndroidComposeUiTest {
        setRoomSelectView(
            aRoomSelectState(
                resultState = SearchBarResultState.Results(aRoomSelectRoomList()),
            ),
        )
        onNodeWithText("Alice").assertIsDisplayed()
        onNodeWithText("@alice:example.org").assertIsDisplayed()
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `a room which is not a direct message does not render the matrix id of its hero`() = runAndroidComposeUiTest {
        setRoomSelectView(
            aRoomSelectState(
                resultState = SearchBarResultState.Results(
                    persistentListOf(
                        aSelectRoomInfo(
                            roomId = RoomId("!room:domain"),
                            name = "Room with a single hero",
                            heroes = persistentListOf(
                                aMatrixUser(id = "@alice:example.org", displayName = "Alice"),
                            ),
                        ),
                    )
                ),
            ),
        )
        onNodeWithText("Room with a single hero").assertIsDisplayed()
        onNodeWithText("@alice:example.org").assertDoesNotExist()
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setRoomSelectView(
    state: RoomSelectState,
    onDismiss: () -> Unit = EnsureNeverCalled(),
    onSubmit: (List<RoomId>) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        RoomSelectView(
            state = state,
            onDismiss = onDismiss,
            onSubmit = onSubmit,
        )
    }
}
