package io.element.android.x.features.rageshake.crash.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.x.architecture.Presenter
import io.element.android.x.features.rageshake.crash.CrashDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CrashDetectionPresenter @Inject constructor(private val crashDataStore: CrashDataStore) : Presenter<CrashDetectionState> {

    @Composable
    override fun present(): CrashDetectionState {
        val localCoroutineScope = rememberCoroutineScope()
        val crashDetected = crashDataStore.appHasCrashed().collectAsState(initial = false)

        fun handleEvents(event: CrashDetectionEvents) {
            when (event) {
                CrashDetectionEvents.ResetAllCrashData -> localCoroutineScope.resetAll()
                CrashDetectionEvents.ResetAppHasCrashed -> localCoroutineScope.resetAppHasCrashed()
            }
        }

        return CrashDetectionState(
            crashDetected = crashDetected.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.resetAppHasCrashed() = launch {
        crashDataStore.resetAppHasCrashed()
    }

    private fun CoroutineScope.resetAll() = launch {
        crashDataStore.reset()
    }
}
