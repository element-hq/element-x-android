@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.rememberNavHostEngine
import io.element.android.x.designsystem.ElementXTheme
import io.element.android.x.destinations.LoginScreenNavigationDestination
import kotlinx.coroutines.runBlocking

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
    val engine = rememberNavHostEngine()
    val navController = engine.rememberNavController()
    val startRoute = runBlocking {
        if (!viewModel.hasSession()) LoginScreenNavigationDestination else NavGraphs.root.startRoute
    }

    DestinationsNavHost(
        engine = engine,
        navController = navController,
        navGraph = NavGraphs.root,
        startRoute = startRoute
    )
}
