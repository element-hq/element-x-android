package io.element.android.x.features.rageshake.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.x.architecture.Presenter
import io.element.android.x.features.rageshake.rageshake.RageShake
import io.element.android.x.features.rageshake.rageshake.RageshakeDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class RageshakePreferencesPresenter @Inject constructor(
    private val rageshake: RageShake,
    private val rageshakeDataStore: RageshakeDataStore,

    ) : Presenter<RageshakePreferencesState, RageshakePreferencesEvents> {

    @Composable
    override fun present(events: Flow<RageshakePreferencesEvents>): RageshakePreferencesState {
        val isSupported: MutableState<Boolean> = rememberSaveable {
            mutableStateOf(rageshake.isAvailable())
        }
        val isEnabled = rageshakeDataStore
            .isEnabled()
            .collectAsState(initial = false)

        val sensitivity = rageshakeDataStore
            .sensitivity()
            .collectAsState(initial = 0f)

        LaunchedEffect(Unit) {
            events.collect { event ->
                when (event) {
                    is RageshakePreferencesEvents.SetIsEnabled -> setIsEnabled(event.isEnabled)
                    is RageshakePreferencesEvents.SetSensitivity -> setSensitivity(event.sensitivity)
                }
            }
        }

        return RageshakePreferencesState(
            isEnabled = isEnabled.value,
            isSupported = isSupported.value,
            sensitivity = sensitivity.value
        )
    }

    private fun CoroutineScope.setSensitivity(sensitivity: Float) = launch {
        rageshakeDataStore.setSensitivity(sensitivity)
    }

    private fun CoroutineScope.setIsEnabled(enabled: Boolean) = launch {
        rageshakeDataStore.setIsEnabled(enabled)
    }
}
