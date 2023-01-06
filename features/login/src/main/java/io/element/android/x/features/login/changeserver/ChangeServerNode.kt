package io.element.android.x.features.login.changeserver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesNode
import io.element.android.x.architecture.presenterConnector
import io.element.android.x.di.AppScope

@ContributesNode(AppScope::class)
class ChangeServerNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: ChangeServerPresenter,
) : Node(buildContext, plugins = plugins) {

    private val presenterConnector = presenterConnector(presenter)

    private fun onChangeServer(server: String) {
        presenterConnector.emitEvent(ChangeServerEvents.SetServer(server))
    }

    private fun onSubmit() {
        presenterConnector.emitEvent(ChangeServerEvents.Submit)
    }

    private fun onSuccess() {
        navigateUp()
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state by presenterConnector.stateFlow.collectAsState()
        ChangeServerView(
            state = state,
            onChangeServer = this::onChangeServer,
            onChangeServerSubmit = this::onSubmit,
            onChangeServerSuccess = this::onSuccess,
        )
    }
}
