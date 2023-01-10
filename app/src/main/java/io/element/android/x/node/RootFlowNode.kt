package io.element.android.x.node

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.children.whenChildAttached
import com.bumble.appyx.core.clienthelper.interactor.Interactor
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.node.node
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import io.element.android.x.architecture.createNode
import io.element.android.x.architecture.presenterConnector
import io.element.android.x.core.di.DaggerComponentOwner
import io.element.android.x.core.screenshot.ImageResult
import io.element.android.x.di.SessionComponentsOwner
import io.element.android.x.features.rageshake.bugreport.BugReportNode
import io.element.android.x.matrix.Matrix
import io.element.android.x.matrix.core.SessionId
import io.element.android.x.root.RootEvents
import io.element.android.x.root.RootPresenter
import io.element.android.x.root.RootView
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import timber.log.Timber

class SessionComponentsOwnerInteractor(private val sessionComponentsOwner: SessionComponentsOwner) : Interactor<RootFlowNode>() {
    override fun onCreate(lifecycle: Lifecycle) {
        lifecycle.subscribe(onCreate = {
            whenChildAttached { commonLifecycle: Lifecycle, child: LoggedInFlowNode ->
                Timber.v("LoggedInFlowNode attached: ${child.sessionId} ")
                commonLifecycle.subscribe(
                    onDestroy = {
                        Timber.v("LoggedInFlowNode destroyed: ${child.sessionId}")
                        sessionComponentsOwner.release(child.sessionId)
                    }
                )
            }
        })
    }
}

class RootFlowNode(
    buildContext: BuildContext,
    private val backstack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.SplashScreen,
        savedStateMap = buildContext.savedStateMap,
    ),
    private val appComponentOwner: DaggerComponentOwner,
    private val matrix: Matrix,
    private val sessionComponentsOwner: SessionComponentsOwner,
    rootPresenter: RootPresenter
) :
    ParentNode<RootFlowNode.NavTarget>(
        navModel = backstack,
        buildContext = buildContext,
        plugins = listOf(SessionComponentsOwnerInteractor(sessionComponentsOwner)),
    ),

    DaggerComponentOwner by appComponentOwner {

    private val presenterConnector = presenterConnector(rootPresenter)

    init {
        Timber.v("Init")
        lifecycle.subscribe(
            onCreate = { Timber.v("OnCreate") },
            onResume = {
                Timber.v("OnResume")
                presenterConnector.emitEvent(RootEvents.StartRageshakeDetection)
            },
            onPause = {
                Timber.v("OnPause")
                presenterConnector.emitEvent(RootEvents.StopRageshakeDetection)
            },
            onDestroy = { Timber.v("OnDestroy") }
        )
    }

    init {
        matrix.isLoggedIn()
            .distinctUntilChanged()
            .onEach { isLoggedIn ->
                Timber.v("isLoggedIn=$isLoggedIn")
                if (isLoggedIn) {
                    val matrixClient = matrix.restoreSession()
                    if (matrixClient == null) {
                        backstack.newRoot(NavTarget.NotLoggedInFlow)
                    } else {
                        matrixClient.startSync()
                        sessionComponentsOwner.create(matrixClient)
                        backstack.newRoot(NavTarget.LoggedInFlow(matrixClient.sessionId))
                    }
                } else {
                    backstack.newRoot(NavTarget.NotLoggedInFlow)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun hideShowkaseButton() {
        presenterConnector.emitEvent(RootEvents.HideShowkaseButton)
    }

    private fun onOpenBugReport() {
        presenterConnector.emitEvent(RootEvents.ResetAppHasCrashed)
        backstack.push(NavTarget.BugReport)
    }

    private fun onCrashDetectedDismissed() {
        presenterConnector.emitEvent(RootEvents.ResetAllCrashData)
    }

    private fun onDismissRageshake() {
        presenterConnector.emitEvent(RootEvents.DismissRageshake)
    }

    private fun onDisableRageshake() {
        presenterConnector.emitEvent(RootEvents.DisableRageshake)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state by presenterConnector.stateFlow.collectAsState()
        RootView(
            state = state,
            onHideShowkaseClicked = this::hideShowkaseButton,
            onOpenBugReport = this::onOpenBugReport,
            onCrashDetectedDismissed = this::onCrashDetectedDismissed,
            onDisableRageshake = this::onDisableRageshake,
            onDismissRageshake = this::onDismissRageshake,
            onScreenshotTaken = this::onScreenshotTaken
        ) {
            Children(navModel = backstack)
        }
    }

    private fun onScreenshotTaken(imageResult: ImageResult) {
        presenterConnector.emitEvent(RootEvents.ProcessScreenshot(imageResult))
    }

    private val bugReportNodeCallback = object : BugReportNode.Callback {
        override fun onBugReportSent() {
            backstack.pop()
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object SplashScreen : NavTarget

        @Parcelize
        object NotLoggedInFlow : NavTarget

        @Parcelize
        data class LoggedInFlow(val sessionId: SessionId) : NavTarget

        @Parcelize
        object BugReport : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.LoggedInFlow -> {
                LoggedInFlowNode(
                    buildContext = buildContext,
                    sessionId = navTarget.sessionId,
                    onOpenBugReport = this::onOpenBugReport
                )
            }
            NavTarget.NotLoggedInFlow -> NotLoggedInFlowNode(buildContext)
            NavTarget.SplashScreen -> node(buildContext) {
                Box(modifier = it.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            NavTarget.BugReport -> createNode<BugReportNode>(buildContext, plugins = listOf(bugReportNodeCallback))
        }
    }
}
