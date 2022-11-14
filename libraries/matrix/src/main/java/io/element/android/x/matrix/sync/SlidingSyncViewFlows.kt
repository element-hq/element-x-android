package io.element.android.x.matrix.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import mxCallbackFlow
import org.matrix.rustcomponents.sdk.*

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
