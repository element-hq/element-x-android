package io.element.android.x.features.preferences.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.element.android.x.architecture.Async
import io.element.android.x.architecture.Presenter
import io.element.android.x.architecture.SharedFlowHolder
import io.element.android.x.features.logout.LogoutPreferenceEvents
import io.element.android.x.features.logout.LogoutPreferencePresenter
import io.element.android.x.features.rageshake.preferences.RageshakePreferencesEvents
import io.element.android.x.features.rageshake.preferences.RageshakePreferencesPresenter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PreferencesRootPresenter @Inject constructor(
    private val logoutPresenter: LogoutPreferencePresenter,
    private val rageshakePresenter: RageshakePreferencesPresenter,
) : Presenter<PreferencesRootState, PreferencesRootEvents> {

    private val logoutEventsFlow = SharedFlowHolder<LogoutPreferenceEvents>()
    private val rageshakeEventsFlow = SharedFlowHolder<RageshakePreferencesEvents>()

    @Composable
    override fun present(events: Flow<PreferencesRootEvents>): PreferencesRootState {
        val logoutState = logoutPresenter.present(events = logoutEventsFlow.asSharedFlow())
        val rageshakeState = rageshakePresenter.present(events = rageshakeEventsFlow.asSharedFlow())
        LaunchedEffect(Unit) {
            events.collect { event ->
                when (event) {
                    PreferencesRootEvents.Logout -> logoutEventsFlow.emit(LogoutPreferenceEvents.Logout)
                    is PreferencesRootEvents.SetRageshakeEnabled -> rageshakeEventsFlow.emit(RageshakePreferencesEvents.SetIsEnabled(event.enabled))
                    is PreferencesRootEvents.SetRageshakeSensitivity -> rageshakeEventsFlow.emit(RageshakePreferencesEvents.SetSensitivity(event.sensitivity))
                }
            }
        }
        return PreferencesRootState(
            logoutState = logoutState,
            rageshakeState = rageshakeState,
            myUser = Async.Uninitialized
        )
    }
}
