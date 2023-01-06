package io.element.android.x.node

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.airbnb.android.showkase.models.Showkase
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
import com.bumble.appyx.navmodel.backstack.operation.replace
import io.element.android.x.BuildConfig
import io.element.android.x.component.ShowkaseButton
import io.element.android.x.core.di.DaggerComponentOwner
import io.element.android.x.di.SessionComponentsOwner
import io.element.android.x.features.rageshake.bugreport.BugReportScreen
import io.element.android.x.features.rageshake.crash.ui.CrashDetectionScreen
import io.element.android.x.features.rageshake.detection.RageshakeDetectionScreen
import io.element.android.x.getBrowserIntent
import io.element.android.x.matrix.Matrix
import io.element.android.x.matrix.core.SessionId
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
) :
    ParentNode<RootFlowNode.NavTarget>(
        navModel = backstack,
        buildContext = buildContext,
        plugins = listOf(SessionComponentsOwnerInteractor(sessionComponentsOwner)),
    ),

    DaggerComponentOwner by appComponentOwner {

    init {
        Timber.v("Init")
        lifecycle.subscribe(
            onCreate = { Timber.v("OnCreate") },
            onDestroy = { Timber.v("OnDestroy") }
        )
    }

    init {
        matrix.isLoggedIn()
            .distinctUntilChanged()
            .onEach { isLoggedIn ->
                Timber.v("IsLoggedIn")
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

    @Composable
    override fun View(modifier: Modifier) {
        var isShowkaseButtonVisible by remember { mutableStateOf(BuildConfig.DEBUG) }
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Children(navModel = backstack)
            val context = LocalContext.current
            ShowkaseButton(
                isVisible = isShowkaseButtonVisible,
                onCloseClicked = { isShowkaseButtonVisible = false },
                onClick = { startActivity(context, Showkase.getBrowserIntent(context), null) }
            )

            /*
            var isBugReportVisible by rememberSaveable {
                mutableStateOf(false)
            }
            RageshakeDetectionScreen(
                onOpenBugReport = {
                    isBugReportVisible = true
                }
            )
            CrashDetectionScreen(
                onOpenBugReport = {
                    isBugReportVisible = true
                }
            )
            if (isBugReportVisible) {
                // TODO Improve the navigation, when pressing back here, it closes the app.
                BugReportScreen(
                    onDone = { isBugReportVisible = false }
                )
            }
             */
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object SplashScreen : NavTarget

        @Parcelize
        object NotLoggedInFlow : NavTarget

        @Parcelize
        data class LoggedInFlow(val sessionId: SessionId) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.LoggedInFlow -> {
                LoggedInFlowNode(buildContext, navTarget.sessionId)
            }
            NavTarget.NotLoggedInFlow -> NotLoggedInFlowNode(buildContext)
            NavTarget.SplashScreen -> node(buildContext) {
                Box(modifier = it.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
