package io.element.android.x.features.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesNode
import io.element.android.x.di.SessionScope

@ContributesNode(SessionScope::class)
class MessagesNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    //presenter: MessagesPresenter,
) : Node(buildContext, plugins = plugins) {

    @Composable
    override fun View(modifier: Modifier) {
        /*
        val state by connector.stateFlow.collectAsState()
        MessagesView(
            state = state,
            onBackPressed = this::navigateUp,
        )
         */
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "MESSAGES NODE")
        }
    }
}
