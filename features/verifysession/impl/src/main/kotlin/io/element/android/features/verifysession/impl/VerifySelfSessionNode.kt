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

package io.element.android.features.verifysession.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.verifysession.api.VerifySessionEntryPoint
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
class VerifySelfSessionNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: VerifySelfSessionPresenter,
) : Node(buildContext, plugins = plugins) {
    private fun onEnterRecoveryKey() {
        plugins<VerifySessionEntryPoint.Callback>().forEach {
            it.onEnterRecoveryKey()
        }
    }

    private fun onDone() {
        plugins<VerifySessionEntryPoint.Callback>().forEach {
            it.onDone()
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        VerifySelfSessionView(
            state = state,
            modifier = modifier,
            onEnterRecoveryKey = ::onEnterRecoveryKey,
            onFinished = ::onDone,
        )
    }
}
