package io.element.android.x.features.rageshake.crash.ui

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesViewModel
import io.element.android.x.core.di.daggerMavericksViewModelFactory
import io.element.android.x.di.AppScope
import io.element.android.x.features.rageshake.crash.CrashDataStore
import kotlinx.coroutines.launch

@ContributesViewModel(AppScope::class)
class CrashDetectionViewModel @AssistedInject constructor(
    @Assisted initialState: CrashDetectionViewState,
    private val crashDataStore: CrashDataStore,
) : MavericksViewModel<CrashDetectionViewState>(initialState) {

    companion object :
        MavericksViewModelFactory<CrashDetectionViewModel, CrashDetectionViewState> by daggerMavericksViewModelFactory()

    init {
        observeDataStore()
    }

    private fun observeDataStore() {
        viewModelScope.launch {
            crashDataStore.appHasCrashed().collect { appHasCrashed ->
                setState {
                    copy(
                        crashDetected = appHasCrashed
                    )
                }
            }
        }
    }

    fun onYes() {
        viewModelScope.launch {
            crashDataStore.resetAppHasCrashed()
        }
    }

    fun onPopupDismissed() {
        viewModelScope.launch {
            crashDataStore.reset()
        }
    }
}
