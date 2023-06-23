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

package io.element.android.libraries.matrix.impl.sync

import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.SlidingSyncList
import org.matrix.rustcomponents.sdk.SlidingSyncListLoadingState
import org.matrix.rustcomponents.sdk.SlidingSyncListRoomListObserver
import org.matrix.rustcomponents.sdk.SlidingSyncListRoomsCountObserver
import org.matrix.rustcomponents.sdk.SlidingSyncListRoomsListDiff
import org.matrix.rustcomponents.sdk.SlidingSyncListStateObserver

fun SlidingSyncList.roomListDiff(scope: CoroutineScope): Flow<SlidingSyncListRoomsListDiff> =
    mxCallbackFlow {
        val observer = object : SlidingSyncListRoomListObserver {
            override fun didReceiveUpdate(diff: SlidingSyncListRoomsListDiff) {
                scope.launch {
                    send(diff)
                }
            }
        }
        observeRoomList(observer)
    }

fun SlidingSyncList.state(scope: CoroutineScope): Flow<SlidingSyncListLoadingState> = mxCallbackFlow {
    val observer = object : SlidingSyncListStateObserver {
        override fun didReceiveUpdate(newState: SlidingSyncListLoadingState) {
            scope.launch {
                send(newState)
            }
        }
    }
    observeState(observer)
}

fun SlidingSyncList.roomsCount(scope: CoroutineScope): Flow<UInt> = mxCallbackFlow {
    val observer = object : SlidingSyncListRoomsCountObserver {
        override fun didReceiveUpdate(count: UInt) {
            scope.launch {
                send(count)
            }
        }
    }
    observeRoomsCount(observer)
}
