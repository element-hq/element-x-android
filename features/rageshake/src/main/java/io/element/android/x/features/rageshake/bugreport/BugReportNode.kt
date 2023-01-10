package io.element.android.x.features.rageshake.bugreport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
class BugReportNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenter: BugReportPresenter,
) : Node(buildContext, plugins = plugins) {

    private val presenterConnector = presenterConnector(presenter)

    interface Callback : Plugin {
        fun onBugReportSent()
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state by presenterConnector.stateFlow.collectAsState()
        BugReportView(
            state = state,
            modifier = modifier,
            onDescriptionChanged = this::onDescriptionChanged,
            onSetSendLog = this::onSetSendLog,
            onSetSendCrashLog = this::onSetSendCrashLog,
            onSetCanContact = this::onSetCanContact,
            onSetSendScreenshot = this::onSetSendScreenshot,
            onSubmit = this::onSubmit,
            onDone = this::onDone
        )
    }

    private fun onDone() {
        presenterConnector.emitEvent(BugReportEvents.ResetAll)
        plugins<Callback>().forEach { it.onBugReportSent() }
    }

    private fun onSubmit() {
        presenterConnector.emitEvent(BugReportEvents.SendBugReport)
    }

    private fun onSetSendLog(sendLog: Boolean) {
        presenterConnector.emitEvent(BugReportEvents.SetSendLog(sendLog))
    }

    private fun onSetSendCrashLog(sendCrashLog: Boolean) {
        presenterConnector.emitEvent(BugReportEvents.SetSendCrashLog(sendCrashLog))
    }

    private fun onSetSendScreenshot(sendScreenshot: Boolean) {
        presenterConnector.emitEvent(BugReportEvents.SetSendScreenshot(sendScreenshot))
    }

    private fun onSetCanContact(canContact: Boolean) {
        presenterConnector.emitEvent(BugReportEvents.SetCanContact(canContact))
    }

    private fun onDescriptionChanged(description: String) {
        presenterConnector.emitEvent(BugReportEvents.SetDescription(description))
    }
}

