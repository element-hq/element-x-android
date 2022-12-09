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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import com.airbnb.android.showkase.models.Showkase
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

private const val transitionAnimationDuration = 500

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ElementXTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    @Composable
    private fun ShowkaseButton(
        isVisible: Boolean,
        onClick: () -> Unit,
        onCloseClicked: () -> Unit
    ) {
        if (isVisible) {
            Button(
                modifier = Modifier
                    .padding(top = 32.dp, start = 16.dp),
                onClick = onClick
            ) {
                Text(text = "Showkase Browser")
                IconButton(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(16.dp),
                    onClick = onCloseClicked,
                ) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "")
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

        var isShowkaseButtonVisible by remember { mutableStateOf(BuildConfig.DEBUG) }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            MainContent(
                startRoute = startRoute
            )
            ShowkaseButton(
                isVisible = isShowkaseButtonVisible,
                onCloseClicked = { isShowkaseButtonVisible = false },
                onClick = { startActivity(Showkase.getBrowserIntent(this@MainActivity)) }
            )
        }
        OnLifecycleEvent { _, event ->
            Timber.v("OnLifecycleEvent: $event")
        }
    }

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
        LogNavigation(navController)

        DestinationsNavHost(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            engine = engine,
            navController = navController,
            navGraph = NavGraphs.root,
            startRoute = startRoute
        )
    }

    @Composable
    private fun LogNavigation(navController: NavHostController) {
        LaunchedEffect(key1 = navController) {
            navController.appCurrentDestinationFlow.collect {
                Timber.d("Navigating to ${it.route}")
            }
        }
    }

    @Composable
    @Preview
    fun MainContentPreview() {
        MainContent(startRoute = OnBoardingScreenNavigationDestination)
    }
}
