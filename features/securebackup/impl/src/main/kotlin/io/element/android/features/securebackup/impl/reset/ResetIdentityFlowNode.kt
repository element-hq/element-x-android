/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset

import android.app.Activity
import android.os.Parcelable
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.securebackup.impl.reset.password.ResetIdentityPasswordNode
import io.element.android.features.securebackup.impl.reset.root.ResetIdentityRootNode
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.encryption.IdentityOidcResetHandle
import io.element.android.libraries.matrix.api.encryption.IdentityPasswordResetHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(SessionScope::class)
@AssistedInject
class ResetIdentityFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val resetIdentityFlowManager: ResetIdentityFlowManager,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : BaseFlowNode<ResetIdentityFlowNode.NavTarget>(
    backstack = BackStack(initialElement = NavTarget.Root, savedStateMap = buildContext.savedStateMap),
    buildContext = buildContext,
    plugins = plugins,
) {
    interface Callback : Plugin {
        fun onDone()
    }

    private val callback: Callback = callback()

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object ResetPassword : NavTarget
    }

    private lateinit var activity: Activity
    private var darkTheme: Boolean = false
    private var resetJob: Job? = null

    override fun onBuilt() {
        super.onBuilt()

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                // If the custom tab / Web browser was opened, we need to cancel the reset job
                // when we come back to the node if the reset wasn't successful
                sessionCoroutineScope.launch {
                    cancelResetJob()

                    resetIdentityFlowManager.whenResetIsDone {
                        callback.onDone()
                    }
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                // Make sure we cancel the reset job when the node is destroyed, just in case
                sessionCoroutineScope.launch { cancelResetJob() }
            }
        })
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Root -> {
                val callback = object : ResetIdentityRootNode.Callback {
                    override fun onContinue() {
                        sessionCoroutineScope.startReset()
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
                            null -> {
                                Timber.d("No reset handle return, the reset is done.")
                            }
                            is IdentityOidcResetHandle -> {
                                activity.openUrlInChromeCustomTab(null, darkTheme, handle.url)
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
            activity = requireNotNull(LocalActivity.current)
        }
        darkTheme = !ElementTheme.isLightTheme
        val startResetState by resetIdentityFlowManager.currentHandleFlow.collectAsState()
        if (startResetState.isLoading()) {
            ProgressDialog(
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
                onDismissRequest = { sessionCoroutineScope.launch { cancelResetJob() } }
            )
        }

        BackstackView(modifier)
    }
}
