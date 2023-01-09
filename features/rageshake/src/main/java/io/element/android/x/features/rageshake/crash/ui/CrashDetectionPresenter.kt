package io.element.android.x.features.rageshake.crash.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import io.element.android.x.architecture.Presenter
import io.element.android.x.features.rageshake.crash.CrashDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class CrashDetectionPresenter @Inject constructor(private val crashDataStore: CrashDataStore) : Presenter<CrashDetectionState, CrashDetectionEvents> {

    @Composable
    override fun present(events: Flow<CrashDetectionEvents>): CrashDetectionState {
        val crashDetected = crashDataStore.appHasCrashed().collectAsState(initial = false)
        LaunchedEffect(Unit) {
            events.collect { event ->
                when (event) {
                    CrashDetectionEvents.ResetAll -> resetAll()
                    CrashDetectionEvents.ResetAppHasCrashed -> resetAppHasCrashed()
                }
            }
        }
        return CrashDetectionState(
            crashDetected = crashDetected.value
        )
    }

    private fun CoroutineScope.resetAppHasCrashed() = launch {
        crashDataStore.resetAppHasCrashed()
    }

    fun CoroutineScope.resetAll() = launch {
        crashDataStore.reset()
    }
}
