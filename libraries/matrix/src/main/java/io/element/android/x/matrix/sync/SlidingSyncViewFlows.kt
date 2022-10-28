package io.element.android.x.matrix.sync

import kotlinx.coroutines.flow.Flow
import mxCallbackFlow
import org.matrix.rustcomponents.sdk.*

fun SlidingSyncView.roomListDiff(): Flow<SlidingSyncViewRoomsListDiff> = mxCallbackFlow {
    val observer = object : SlidingSyncViewRoomListObserver {
        override fun didReceiveUpdate(diff: SlidingSyncViewRoomsListDiff) {
            trySend(diff)
        }
    }
    observeRoomList(observer)
}

fun SlidingSyncView.state(): Flow<SlidingSyncState> = mxCallbackFlow {
    val observer = object : SlidingSyncViewStateObserver {
        override fun didReceiveUpdate(newState: SlidingSyncState) {
            trySend(newState)
        }
    }
    observeState(observer)
}

fun SlidingSyncView.roomsCount(): Flow<UInt> = mxCallbackFlow {
    val observer = object : SlidingSyncViewRoomsCountObserver {
        override fun didReceiveUpdate(count: UInt) {
            trySend(count)
        }
    }
    observeRoomsCount(observer)
}
