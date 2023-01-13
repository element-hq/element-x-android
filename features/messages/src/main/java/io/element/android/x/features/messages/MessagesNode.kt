package io.element.android.x.features.messages

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
import io.element.android.x.di.RoomScope

@ContributesNode(RoomScope::class)
class MessagesNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenter: MessagesPresenter,
) : Node(buildContext, plugins = plugins) {

    private val connector = presenterConnector(presenter)

    @Composable
    override fun View(modifier: Modifier) {
        val state by connector.stateFlow.collectAsState()
        MessagesView(
            state = state,
            onBackPressed = this::navigateUp,
            modifier = modifier
        )
    }
}
