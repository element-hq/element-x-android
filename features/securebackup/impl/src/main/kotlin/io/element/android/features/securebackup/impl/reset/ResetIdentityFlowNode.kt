/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.securebackup.impl.reset

import android.app.Activity
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.securebackup.impl.reset.password.ResetIdentityPasswordNode
import io.element.android.features.securebackup.impl.reset.root.ResetIdentityRootNode
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.encryption.IdentityOidcResetHandle
import io.element.android.libraries.matrix.api.encryption.IdentityPasswordResetHandle
import io.element.android.libraries.oidc.api.OidcEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(SessionScope::class)
class ResetIdentityFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val resetIdentityFlowManager: ResetIdentityFlowManager,
    private val coroutineScope: CoroutineScope,
    private val oidcEntryPoint: OidcEntryPoint,
) : BaseFlowNode<ResetIdentityFlowNode.NavTarget>(
    backstack = BackStack(initialElement = NavTarget.Root, savedStateMap = buildContext.savedStateMap),
    buildContext = buildContext,
    plugins = plugins,
) {
    interface Callback : Plugin {
        fun onDone()
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object ResetPassword : NavTarget

        @Parcelize
        data class ResetOidc(val url: String) : NavTarget
    }

    private lateinit var activity: Activity
    private var resetJob: Job? = null

    override fun onBuilt() {
        super.onBuilt()

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                // If the custom tab was opened, we need to cancel the reset job
                // when we come back to the node if the reset wasn't successful
                coroutineScope.launch {
                    cancelResetJob()

                    resetIdentityFlowManager.whenResetIsDone {
                        plugins<Callback>().forEach { it.onDone() }
                    }
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                // Make sure we cancel the reset job when the node is destroyed, just in case
                coroutineScope.launch { cancelResetJob() }
            }
        })
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Root -> {
                val callback = object : ResetIdentityRootNode.Callback {
                    override fun onContinue() {
                        coroutineScope.startReset()
                    }
                }
                createNode<ResetIdentityRootNode>(buildContext, listOf(callback))
            }
            is NavTarget.ResetPassword -> {
                val handle = resetIdentityFlowManager.currentHandleFlow.value.dataOrNull() as? IdentityPasswordResetHandle ?: error("No password handle found")
                createNode<ResetIdentityPasswordNode>(
                    buildContext,
                    listOf(ResetIdentityPasswordNode.Inputs(handle))
                )
            }
            is NavTarget.ResetOidc -> {
                oidcEntryPoint.createFallbackWebViewNode(this, buildContext, navTarget.url)
            }
        }
    }

    private fun CoroutineScope.startReset() = launch {
        resetIdentityFlowManager.getResetHandle()
            .collectLatest { state ->
                when (state) {
                    is AsyncData.Failure -> {
                        cancelResetJob()
                        Timber.e(state.error, "Could not load the reset identity handle.")
                    }
                    is AsyncData.Success -> {
                        when (val handle = state.data) {
                            is IdentityOidcResetHandle -> {
                                if (oidcEntryPoint.canUseCustomTab()) {
                                    activity.openUrlInChromeCustomTab(null, false, handle.url)
                                } else {
                                    backstack.push(NavTarget.ResetOidc(handle.url))
                                }
                                resetJob = launch { handle.resetOidc() }
                            }
                            is IdentityPasswordResetHandle -> backstack.push(NavTarget.ResetPassword)
                        }
                    }
                    else -> Unit
                }
            }
    }

    private suspend fun cancelResetJob() {
        resetJob?.cancel()
        resetJob = null
        resetIdentityFlowManager.cancel()
    }

    @Composable
    override fun View(modifier: Modifier) {
        // Workaround to get the current activity
        if (!this::activity.isInitialized) {
            activity = LocalContext.current as Activity
        }

        val startResetState by resetIdentityFlowManager.currentHandleFlow.collectAsState()
        if (startResetState.isLoading()) {
            ProgressDialog(
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
                onDismissRequest = { coroutineScope.launch { cancelResetJob() } }
            )
        }

        BackstackView(modifier)
    }
}
