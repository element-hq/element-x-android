@file:OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialNavigationApi::class
)

package io.element.android.x

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.spec.Route
import io.element.android.x.core.compose.OnLifecycleEvent
import io.element.android.x.designsystem.ElementXTheme
import io.element.android.x.destinations.OnBoardingScreenNavigationDestination
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ElementXTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun MainScreen(viewModel: MainViewModel) {
    val startRoute = runBlocking {
        if (!viewModel.isLoggedIn()) {
            OnBoardingScreenNavigationDestination
        } else {
            viewModel.restoreSession()
            NavGraphs.root.startRoute
        }
    }

    MainContent(
        startRoute = startRoute
    )

    OnLifecycleEvent { _, event ->
        Timber.v("OnLifecycleEvent: $event")
    }
}

private const val transitionAnimationDuration = 500
@Composable
private fun MainContent(startRoute: Route) {
    val engine = rememberAnimatedNavHostEngine(
        rootDefaultAnimations = RootNavGraphDefaultAnimations(
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Left,
                    animationSpec = tween(transitionAnimationDuration)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Left,
                    animationSpec = tween(transitionAnimationDuration)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(transitionAnimationDuration)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(transitionAnimationDuration)
                )
            }
        )
    )
    val navController = engine.rememberNavController()

    DestinationsNavHost(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        engine = engine,
        navController = navController,
        navGraph = NavGraphs.root,
        startRoute = startRoute
    )
}

@Composable
@Preview
private fun MainContentPreview() {
    MainContent(startRoute = OnBoardingScreenNavigationDestination)
}