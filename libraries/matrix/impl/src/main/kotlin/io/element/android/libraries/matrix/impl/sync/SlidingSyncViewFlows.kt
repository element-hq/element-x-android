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
import org.matrix.rustcomponents.sdk.SlidingSyncState
import org.matrix.rustcomponents.sdk.SlidingSyncView
import org.matrix.rustcomponents.sdk.SlidingSyncViewRoomListObserver
import org.matrix.rustcomponents.sdk.SlidingSyncViewRoomsCountObserver
import org.matrix.rustcomponents.sdk.SlidingSyncViewRoomsListDiff
import org.matrix.rustcomponents.sdk.SlidingSyncViewStateObserver

fun SlidingSyncView.roomListDiff(scope: CoroutineScope): Flow<SlidingSyncViewRoomsListDiff> =
    mxCallbackFlow {
        val observer = object : SlidingSyncViewRoomListObserver {
            override fun didReceiveUpdate(diff: SlidingSyncViewRoomsListDiff) {
                scope.launch {
                    send(diff)
                }
            }
        }
        observeRoomList(observer)
    }

fun SlidingSyncView.state(scope: CoroutineScope): Flow<SlidingSyncState> = mxCallbackFlow {
    val observer = object : SlidingSyncViewStateObserver {
        override fun didReceiveUpdate(newState: SlidingSyncState) {
            scope.launch {
                send(newState)
            }
        }
    }
    observeState(observer)
}

fun SlidingSyncView.roomsCount(scope: CoroutineScope): Flow<UInt> = mxCallbackFlow {
    val observer = object : SlidingSyncViewRoomsCountObserver {
        override fun didReceiveUpdate(count: UInt) {
            scope.launch {
                send(count)
            }
        }
    }
    observeRoomsCount(observer)
}
