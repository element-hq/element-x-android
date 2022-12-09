package io.element.android.x.matrix.room

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineListener

fun Room.timelineDiff(scope: CoroutineScope): Flow<TimelineDiff> = callbackFlow {
    val listener = object : TimelineListener {
        override fun onUpdate(update: TimelineDiff) {
            scope.launch {
                send(update)
            }
        }
    }
    addTimelineListener(listener)
    awaitClose {
        removeTimeline()
    }

}

