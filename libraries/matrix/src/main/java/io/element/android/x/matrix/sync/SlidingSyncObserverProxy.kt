package io.element.android.x.matrix.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.SlidingSyncObserver
import org.matrix.rustcomponents.sdk.UpdateSummary

class SlidingSyncObserverProxy(private val coroutineScope: CoroutineScope) : SlidingSyncObserver {

    private val updateSummaryMutableFlow = MutableSharedFlow<UpdateSummary>()
    val updateSummaryFlow: Flow<UpdateSummary> = updateSummaryMutableFlow

    override fun didReceiveSyncUpdate(summary: UpdateSummary) {
        coroutineScope.launch {
            updateSummaryMutableFlow.emit(summary)
        }
    }
}