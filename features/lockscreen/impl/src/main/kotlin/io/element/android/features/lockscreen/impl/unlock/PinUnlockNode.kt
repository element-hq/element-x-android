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

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.logout.api.util.onSuccessLogout
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
class PinUnlockNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: PinUnlockPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onUnlock()
    }

    private fun onUnlock() {
        plugins<Callback>().forEach {
            it.onUnlock()
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = LocalContext.current as Activity
        LaunchedEffect(state.isUnlocked) {
            if (state.isUnlocked) {
                onUnlock()
            }
        }
        PinUnlockView(
            state = state,
            // UnlockNode is only used for in-app unlock, so we can safely set isInAppUnlock to true.
            // It's set to false in PinUnlockActivity.
            isInAppUnlock = true,
            onSuccessLogout = { onSuccessLogout(activity, it) },
            modifier = modifier
        )
    }
}
