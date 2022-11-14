package io.element.android.x.matrix.sync

import io.element.android.x.core.coroutine.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.SlidingSyncObserver
import org.matrix.rustcomponents.sdk.UpdateSummary

// Sounds like a reasonable buffer size before it suspends emitting new items.
private const val BUFFER_SIZE = 64
class SlidingSyncObserverProxy(
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers
) : SlidingSyncObserver {

    private val updateSummaryMutableFlow =
        MutableSharedFlow<UpdateSummary>(extraBufferCapacity = BUFFER_SIZE)
    val updateSummaryFlow: SharedFlow<UpdateSummary> = updateSummaryMutableFlow.asSharedFlow()

    override fun didReceiveSyncUpdate(summary: UpdateSummary) {
        if (summary.rooms.isEmpty()) return
        coroutineScope.launch(coroutineDispatchers.io) {
            updateSummaryMutableFlow.emit(summary)
        }
    }

}