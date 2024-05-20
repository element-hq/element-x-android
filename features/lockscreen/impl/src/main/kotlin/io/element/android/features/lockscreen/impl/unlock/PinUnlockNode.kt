/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.lockscreen.impl.unlock

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import java.util.UUID

@ContributesNode(SessionScope::class)
class PinUnlockNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: PinUnlockPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onUnlock()
    }

    data class Inputs(
        val isInAppUnlock: Boolean
    ) : NodeInputs

    private val inputs: Inputs = inputs()

    private fun onUnlock() {
        plugins<Callback>().forEach {
            it.onUnlock()
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        LaunchedEffect(state.isUnlocked) {
            if (state.isUnlocked) {
                onUnlock()
            }
        }
        if (!state.isUnlocked) {
            FullScreenPopUp {
                PinUnlockView(
                    state = state,
                    isInAppUnlock = inputs.isInAppUnlock,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun FullScreenPopUp(
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val parentComposition = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)
    val popupId = rememberSaveable { UUID.randomUUID() }

    val popUpLayout = remember {
        FullScreenPopUpLayout(view, popupId).apply {
            setContent(parentComposition, currentContent)
        }
    }

    DisposableEffect(Unit) {
        popUpLayout.show()
        onDispose {
            popUpLayout.hide()
        }
    }
}

@SuppressLint("ViewConstructor")
class FullScreenPopUpLayout(
    composeView: View,
    popupId: UUID,
) : AbstractComposeView(composeView.context) {
    private val windowManager =
        composeView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val layoutParams = WindowManager.LayoutParams().apply {
        gravity = Gravity.FILL
        flags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            fitInsetsTypes = 0
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
    }
    private var content: @Composable () -> Unit by mutableStateOf({})

    init {
        setViewTreeLifecycleOwner(composeView.findViewTreeLifecycleOwner())
        setViewTreeSavedStateRegistryOwner(composeView.findViewTreeSavedStateRegistryOwner())

        // Set unique id for AbstractComposeView. This allows state restoration for the state
        // defined inside the Popup via rememberSaveable()
        setTag(androidx.compose.ui.R.id.compose_view_saveable_id_tag, "Popup:$popupId")
    }

    fun show() {
        windowManager.addView(this, layoutParams)
    }

    fun setContent(parent: CompositionContext, content: @Composable () -> Unit) {
        setParentCompositionContext(parent)
        this.content = content
    }

    fun hide() {
        windowManager.removeView(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        windowManager.updateViewLayout(this, layoutParams)
    }

    @Composable
    override fun Content() {
        content()
    }
}
