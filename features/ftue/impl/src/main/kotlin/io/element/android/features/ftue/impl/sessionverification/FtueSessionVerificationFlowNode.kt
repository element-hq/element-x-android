/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.ftue.impl.sessionverification

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.securebackup.api.SecureBackupEntryPoint
import io.element.android.features.verifysession.api.VerifySessionEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.di.SessionScope
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class FtueSessionVerificationFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val verifySessionEntryPoint: VerifySessionEntryPoint,
    private val secureBackupEntryPoint: SecureBackupEntryPoint,
) : BaseFlowNode<FtueSessionVerificationFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object EnterRecoveryKey : NavTarget
    }

    interface Callback : Plugin {
        fun onDone()
    }

    private val callback = plugins<Callback>().first()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Root -> {
                verifySessionEntryPoint.nodeBuilder(this, buildContext)
                    .callback(object : VerifySessionEntryPoint.Callback {
                        override fun onEnterRecoveryKey() {
                            backstack.push(NavTarget.EnterRecoveryKey)
                        }

                        override fun onDone() {
                            callback.onDone()
                        }
                    })
                    .build()
            }
            is NavTarget.EnterRecoveryKey -> {
                secureBackupEntryPoint.nodeBuilder(this, buildContext)
                    .params(SecureBackupEntryPoint.Params(SecureBackupEntryPoint.InitialTarget.EnterRecoveryKey))
                    .callback(object : SecureBackupEntryPoint.Callback {
                        override fun onDone() {
                            callback.onDone()
                        }
                    })
                    .build()
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
