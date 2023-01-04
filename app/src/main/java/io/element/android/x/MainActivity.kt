@file:OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialNavigationApi::class
)

package io.element.android.x

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.bumble.appyx.core.integration.NodeHost
import com.bumble.appyx.core.integrationpoint.NodeComponentActivity
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import io.element.android.x.architecture.bindings
import io.element.android.x.core.di.DaggerComponentOwner
import io.element.android.x.designsystem.ElementXTheme
import io.element.android.x.di.AppBindings
import io.element.android.x.node.RootFlowNode

class MainActivity : NodeComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appBindings = bindings<AppBindings>()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ElementXTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NodeHost(integrationPoint = appyxIntegrationPoint) {
                        RootFlowNode(
                            buildContext = it,
                            appComponentOwner = applicationContext as DaggerComponentOwner,
                            matrix = appBindings.matrix(),
                            sessionComponentsOwner = appBindings.sessionComponentsOwner()
                        )
                    }
                }
            }
        }
    }
}
