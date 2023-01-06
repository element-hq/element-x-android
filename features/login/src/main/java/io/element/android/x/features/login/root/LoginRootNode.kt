package io.element.android.x.features.login.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesNode
import io.element.android.x.architecture.presenterConnector
import io.element.android.x.di.AppScope

@ContributesNode(AppScope::class)
class LoginRootNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: LoginRootPresenter,
) : Node(buildContext, plugins = plugins) {

    private val presenterConnector = presenterConnector(presenter)

    init {
        lifecycle.subscribe(
            onResume = { presenterConnector.emitEvent(LoginRootEvents.RefreshHomeServer) }
        )
    }

    interface Callback : Plugin {
        fun onChangeHomeServer()
    }

    private fun onChangeHomeServer() {
        plugins<Callback>().forEach { it.onChangeHomeServer() }
    }

    private fun onLoginChanged(login: String) {
        presenterConnector.emitEvent(LoginRootEvents.SetLogin(login))
    }

    private fun onPasswordChanged(password: String) {
        presenterConnector.emitEvent(LoginRootEvents.SetPassword(password))
    }

    private fun onSubmit() {
        presenterConnector.emitEvent(LoginRootEvents.Submit)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state by presenterConnector.stateFlow.collectAsState()
        LoginRootScreen(
            state = state,
            onChangeServer = this::onChangeHomeServer,
            onLoginChanged = this::onLoginChanged,
            onPasswordChanged = this::onPasswordChanged,
            onSubmitClicked = this::onSubmit
        )
    }
}
