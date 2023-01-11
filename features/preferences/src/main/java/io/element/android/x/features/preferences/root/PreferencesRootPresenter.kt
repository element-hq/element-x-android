package io.element.android.x.features.preferences.root

import androidx.compose.runtime.Composable
import io.element.android.x.architecture.Async
import io.element.android.x.architecture.Presenter
import io.element.android.x.features.logout.LogoutPreferencePresenter
import io.element.android.x.features.rageshake.preferences.RageshakePreferencesPresenter
import javax.inject.Inject

class PreferencesRootPresenter @Inject constructor(
    private val logoutPresenter: LogoutPreferencePresenter,
    private val rageshakePresenter: RageshakePreferencesPresenter,
) : Presenter<PreferencesRootState> {

    @Composable
    override fun present(): PreferencesRootState {
        val logoutState = logoutPresenter.present()
        val rageshakeState = rageshakePresenter.present()

        return PreferencesRootState(
            logoutState = logoutState,
            rageshakeState = rageshakeState,
            myUser = Async.Uninitialized,
        )
    }
}
