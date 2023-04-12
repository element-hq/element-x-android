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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.services.appnavstate.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SPACE_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.services.appnavstate.api.AppNavigationState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test

private const val aSessionOwner = "aSessionOwner"
private const val aSpaceOwner = "aSpaceOwner"
private const val aRoomOwner = "aRoomOwner"
private const val aThreadOwner = "aThreadOwner"

class DefaultAppNavigationStateServiceTest {

    @Test
    fun testNavigation() = runTest {
        val service = DefaultAppNavigationStateService()
        service.onNavigateToSession(aSessionOwner, A_SESSION_ID)
        service.onNavigateToSpace(aSpaceOwner, A_SPACE_ID)
        service.onNavigateToRoom(aRoomOwner, A_ROOM_ID)
        service.onNavigateToThread(aThreadOwner, A_THREAD_ID)
        assertThat(service.appNavigationStateFlow.first()).isEqualTo(
            AppNavigationState.Thread(
                aThreadOwner, A_THREAD_ID,
                AppNavigationState.Room(
                    aRoomOwner,
                    A_ROOM_ID,
                    AppNavigationState.Space(
                        aSpaceOwner,
                        A_SPACE_ID,
                        AppNavigationState.Session(
                            aSessionOwner,
                            A_SESSION_ID
                        )
                    )
                )
            )
        )
    }

    @Test
    fun testFailure() = runTest {
        val service = DefaultAppNavigationStateService()
        assertThrows(IllegalStateException::class.java) { service.onNavigateToSpace(aSpaceOwner, A_SPACE_ID) }
    }
}
