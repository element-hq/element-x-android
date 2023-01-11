package io.element.android.x.features.login.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesNode
import io.element.android.x.architecture.presenterConnector
import io.element.android.x.core.compose.OnLifecycleEvent
import io.element.android.x.di.AppScope

@ContributesNode(AppScope::class)
class LoginRootNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: LoginRootPresenter,
) : Node(buildContext, plugins = plugins) {

    private val presenterConnector = presenterConnector(presenter)

    interface Callback : Plugin {
        fun onChangeHomeServer()
    }

    private fun onChangeHomeServer() {
        plugins<Callback>().forEach { it.onChangeHomeServer() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state by presenterConnector.stateFlow.collectAsState()
        OnLifecycleEvent { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> state.eventSink(LoginRootEvents.RefreshHomeServer)
                else -> Unit
            }
        }
        LoginRootScreen(
            state = state,
            onChangeServer = this::onChangeHomeServer,
        )
    }
}
